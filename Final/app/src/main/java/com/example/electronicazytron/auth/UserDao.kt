package com.example.electronicazytron.auth

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.electronicazytron.model.entities.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    // ----------------------
    // R: READ (Leer / Consultar)
    // ----------------------
    @Query("SELECT * FROM users ORDER BY nombre ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    // ----------------------
    // U: UPDATE (Actualizar)
    // ----------------------
    // Room busca el usuario por su 'id' y actualiza los demás campos
    @Update
    suspend fun update(user: User)

    // ----------------------
    // D: DELETE (Eliminar)
    // ----------------------
    // Room busca el usuario por su 'id' y lo elimina de la tabla
    @Delete
    suspend fun delete(user: User)

    // Opción extra: Borrar directamente por ID (útil para listas)
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: Int)
}