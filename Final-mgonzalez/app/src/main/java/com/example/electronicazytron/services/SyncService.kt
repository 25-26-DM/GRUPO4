package com.example.electronicazytron.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SyncService : Service() {

    private val TAG = "SyncService_Debug"
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.IO)

    companion object {
        const val CHANNEL_ID = "sync_channel"
        const val CHANNEL_NAME = "Sincronización"
        const val NOTIFICATION_ID = 1001
        // Bandera para evitar notificaciones repetidas durante la misma ejecución de la app
        @Volatile
        var notificationShownForRun: Boolean = false
    }

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

                    // Contar productos locales y notificar al usuario
                    notificarSincronizacion(db.productoDao())
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

    /**
     * Notifica al usuario que la sincronización se completó con el número de productos
     */
    private suspend fun notificarSincronizacion(productoDao: ProductoDao) {
        // Mostrar la notificación sólo una vez por ejecución de la app
        if (notificationShownForRun) {
            Log.i(TAG, "Notificación de sincronización ya mostrada en esta ejecución. Omitiendo.")
            return
        }

        try {
            // Contar productos en la base de datos local
            val productCount = productoDao.count()
            Log.i(TAG, "Intentando notificar: $productCount productos en BD local")

            // Crear canal de notificación
            ensureNotificationChannel(applicationContext)

            // Construir la notificación
            val title = "Sincronización Completada"
            val message = "Se han sincronizado $productCount productos"
            
            Log.i(TAG, "Construyendo notificación: $title - $message")

            val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText("Base de datos local contiene $productCount productos sincronizados"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 500))

            // Verificar permisos antes de mostrar la notificación (Android 13+)
            var attempted = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(applicationContext)
                        .notify(NOTIFICATION_ID, builder.build())
                    Log.i(TAG, "Notificación enviada exitosamente con $productCount productos")
                    attempted = true
                } else {
                    Log.w(TAG, "Permiso POST_NOTIFICATIONS no concedido. Notificación no mostrada.")
                }
            } else {
                // Android < 13, no requiere permiso en tiempo de ejecución
                NotificationManagerCompat.from(applicationContext)
                    .notify(NOTIFICATION_ID, builder.build())
                Log.i(TAG, "Notificación enviada exitosamente (Android < 13) con $productCount productos")
                attempted = true
            }

            // Marcar que ya intentamos la notificación en esta ejecución para no repetirla
            notificationShownForRun = true
            if (!attempted) {
                Log.i(TAG, "Intento de notificación realizado pero no mostrado (permiso). Se omitirá en lo sucesivo.")
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error al notificar sincronización: ${ex.message}", ex)
            ex.printStackTrace()
            // Aun en caso de error, evitar múltiples intentos
            notificationShownForRun = true
        }
    }

    private fun ensureNotificationChannel(context: Context) {
        try {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                manager.createNotificationChannel(channel)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creando canal de notificación: ${e.message}")
        }
    }

    // --- OPERACIÓN: CREATE / UPDATE ---
    private suspend fun syncProductos(productoDao: ProductoDao, dynamoDbClient: DynamoDbClient) {
        // Subir TODOS los productos locales (no eliminados)
        val allProducts = productoDao.listar()
        Log.i(TAG, "Subiendo producto")
        if (allProducts.isEmpty()) return

        for (producto in allProducts) {
            try {
                Log.i(TAG, "PutItem")
                Log.i(TAG, "Insertando en DynamoDB: ${producto.codigo}")
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
                Log.i(TAG, "Upload finalizado: ${producto.codigo}")
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
        // Subir TODOS los usuarios locales
        val allUsers = userDao.listar()
        if (allUsers.isNotEmpty()) Log.i(TAG, "Subiendo usuario")
        for (user in allUsers) {
            try {
                Log.i(TAG, "PutItem")
                Log.i(TAG, "Insertando en DynamoDB: user id=${user.id}")
                val itemValues = mutableMapOf<String, AttributeValue>(
                    "id" to AttributeValue.N(user.id.toString()),
                    "nombre" to AttributeValue.S(user.nombre),
                    "apellido" to AttributeValue.S(user.apellido),
                    "password" to AttributeValue.S(user.password)
                )
                if (!user.lastAccess.isNullOrEmpty()) {
                    itemValues["lastAccess"] = AttributeValue.S(user.lastAccess!!)
                }

                dynamoDbClient.putItem {
                    tableName = "users"
                    item = itemValues
                }
                user.isSynced = true
                userDao.update(user)
                Log.i(TAG, "Upload finalizado: user id=${user.id}")
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
                    isSynced = true,
                    lastAccess = item["lastAccess"]?.asS()
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