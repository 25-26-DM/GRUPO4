package com.example.electronicazytron.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.sdk.kotlin.services.dynamodb.model.AttributeValue
import aws.sdk.kotlin.services.dynamodb.putItem
import aws.sdk.kotlin.services.dynamodb.scan
import aws.sdk.kotlin.services.dynamodb.deleteItem
import com.example.electronicazytron.auth.AppDatabase
import com.example.electronicazytron.auth.User
import com.example.electronicazytron.auth.UserDao
import com.example.electronicazytron.model.entities.Producto
import com.example.electronicazytron.model.entities.ProductoDao
import com.example.electronicazytron.utils.DynamoDbClientFactory
import com.example.electronicazytron.utils.NetworkConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SyncService : Service() {

    private val TAG = "SyncService_Debug"
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Servicio de sincronización iniciado.")

        serviceScope.launch {
            val networkConnection = NetworkConnection(applicationContext)
            if (networkConnection.isNetworkAvailable()) {
                Log.i(TAG, "Hay conexión. Iniciando proceso CRUD con DynamoDB.")
                try {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val dynamoDbClient = DynamoDbClientFactory.getClient()

                    // 1. DELETE: Eliminar lo que el usuario marcó en la App
                    eliminarProductosPendientes(db.productoDao(), dynamoDbClient)

                    // 2. CREATE / UPDATE: Subir productos nuevos o editados
                    syncProductos(db.productoDao(), dynamoDbClient)

                    // 3. SYNC USERS: Subir usuarios nuevos
                    syncUsers(db.userDao(), dynamoDbClient)

                    // 4. READ: Descargar cambios desde la nube (AWS -> Celular)
                    descargarDesdeNube(db.productoDao(), dynamoDbClient)

                    // 5. SYNC USERS: Descargar cambios desde la nube (AWS -> Celular)
                    syncUsers(db.userDao(), dynamoDbClient)
                    descargarUsuariosDesdeNube(db.userDao(), dynamoDbClient)

                    Log.i(TAG, "Proceso de sincronización completado con éxito.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error crítico en sincronización: ${e.message}", e)
                }
            } else {
                Log.w(TAG, "Sin conexión. Se pospone la sincronización.")
            }
            stopSelf()
        }
        return START_NOT_STICKY
    }

    // --- OPERACIÓN: CREATE / UPDATE ---
    private suspend fun syncProductos(productoDao: ProductoDao, dynamoDbClient: DynamoDbClient) {
        val unsyncedProducts = productoDao.getUnsynced()
        if (unsyncedProducts.isEmpty()) return

        for (producto in unsyncedProducts) {
            try {
                val itemValues = mapOf(
                    "codigo" to AttributeValue.S(producto.codigo),
                    "descripcion" to AttributeValue.S(producto.descripcion),
                    "fecha_fab" to AttributeValue.S(producto.fecha_fab),
                    "costo" to AttributeValue.N(producto.costo.toString()),
                    "disponibilidad" to AttributeValue.N(producto.disponibilidad.toString()),
                    "eliminado" to AttributeValue.Bool(producto.eliminado),
                    // Se envía el Link como String
                    "imagenUri" to AttributeValue.S(producto.imagenUri)
                )

                dynamoDbClient.putItem {
                    tableName = "productos"
                    item = itemValues
                }

                producto.isSynced = true
                productoDao.actualizar(producto)
                Log.i(TAG, "Producto '${producto.codigo}' sincronizado (Upsert).")
            } catch (e: Exception) {
                Log.e(TAG, "Error al subir '${producto.codigo}': ${e.message}")
            }
        }
    }

    // --- OPERACIÓN: READ (AWS a Local) ---
    private suspend fun descargarDesdeNube(productoDao: ProductoDao, client: DynamoDbClient) {
        try {
            val response = client.scan { tableName = "productos" }
            response.items?.forEach { item ->
                val p = Producto(
                    codigo = item["codigo"]?.asS() ?: "",
                    descripcion = item["descripcion"]?.asS() ?: "",
                    // Conversión segura de N (Número) a tipos de Kotlin
                    costo = item["costo"]?.asNOrNull()?.toDoubleOrNull() ?: 0.0,
                    disponibilidad = item["disponibilidad"]?.asNOrNull()?.toIntOrNull() ?: 0,
                    fecha_fab = item["fecha_fab"]?.asS() ?: "",
                    imagenUri = item["imagenUri"]?.asS() ?: "",
                    isSynced = true,
                    eliminado = item["eliminado"]?.asBool() ?: false


                )
                // IMPORTANTE: insertOrUpdate usa REPLACE para no duplicar datos
                productoDao.insertar(p)
            }
            Log.d(TAG, "Descarga desde AWS finalizada.")
        } catch (e: Exception) {
            Log.e(TAG, "Error en descarga: ${e.message}")
        }
    }

    // --- OPERACIÓN: DELETE (Borrado Físico) ---
    private suspend fun eliminarProductosPendientes(productoDao: ProductoDao, client: DynamoDbClient) {
        val eliminadosLocalmente = productoDao.getProductosMarcadosComoEliminados()

        for (producto in eliminadosLocalmente) {
            try {
                client.deleteItem {
                    tableName = "productos"
                    key = mapOf("codigo" to AttributeValue.S(producto.codigo))
                }
                // Limpieza física de SQLite tras confirmar borrado en AWS
                productoDao.deletePhysical(producto)
                Log.i(TAG, "Producto ${producto.codigo} eliminado definitivamente.")
            } catch (e: Exception) {
                Log.e(TAG, "Error al eliminar en AWS: ${e.message}")
            }
        }
    }

    private suspend fun syncUsers(userDao: UserDao, dynamoDbClient: DynamoDbClient) {
        val unsyncedUsers = userDao.getUnsynced()
        for (user in unsyncedUsers) {
            try {
                dynamoDbClient.putItem {
                    tableName = "users"
                    item = mapOf(
                        "id" to AttributeValue.N(user.id.toString()),
                        "nombre" to AttributeValue.S(user.nombre),
                        "apellido" to AttributeValue.S(user.apellido),
                        "password" to AttributeValue.S(user.password)
                    )
                }
                user.isSynced = true
                userDao.update(user)
            } catch (e: Exception) {
                Log.e(TAG, "Error al sincronizar usuario: ${e.message}")
            }
        }
    }
    private suspend fun descargarUsuariosDesdeNube(userDao: UserDao, client: DynamoDbClient) {
        try {
            val response = client.scan { tableName = "users" }
            response.items?.forEach { item ->
                val u = User(
                    id = item["id"]?.asNOrNull()?.toIntOrNull() ?: 0,
                    nombre = item["nombre"]?.asS() ?: "",
                    apellido = item["apellido"]?.asS() ?: "",
                    password = item["password"]?.asS() ?: "",
                    isSynced = true
                )
                userDao.insertOrUpdate(u)
            }
            Log.d(TAG, "Usuarios actualizados desde la nube.")
        } catch (e: Exception) {
            Log.e(TAG, "Error al descargar usuarios: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.i(TAG, "Servicio destruido.")
    }
}