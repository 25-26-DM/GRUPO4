package com.example.electronicazytron.model.entities

import androidx.room.*

@Dao
interface ProductoDao{

    @Query("SELECT * FROM productos WHERE eliminado=0 ORDER BY codigo ASC")
    suspend fun listar(): List<Producto>

    @Query("SELECT * FROM productos WHERE codigo = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): Producto?

    //para que la descarga de la nube actualice los datos existentes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto)

    @Update
    suspend fun actualizar(producto: Producto)

    @Query("UPDATE productos SET eliminado = 1 WHERE codigo = :codigo")
    suspend fun marcarEliminado(codigo: String)

    // Marcar eliminado y asegurarse que quede como no sincronizado para que el servicio lo procese
    @Query("UPDATE productos SET eliminado = 1, isSynced = 0 WHERE codigo = :codigo")
    suspend fun marcarEliminadoNoSincronizado(codigo: String)

    @Query("SELECT COUNT(*) FROM productos WHERE eliminado=0")
    suspend fun count(): Int

    // --- Nuevo metodo para la sincronizacion
    // este metodo devuelve todos los productos que no han sido sincronizados
    @Query("SELECT * FROM productos WHERE isSynced = 0")
    suspend fun getUnsynced(): List<Producto>

    //Obtener los que están marcados para borrar en la nube
    @Query("SELECT * FROM productos WHERE eliminado = 1")
    suspend fun getProductosMarcadosComoEliminados(): List<Producto>

    //Borrado físico (solo se usa DESPUÉS de confirmar que se borró en AWS)
    @Delete
    suspend fun deletePhysical(producto: Producto)
}