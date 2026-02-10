package com.example.electronicazytron.auth

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val apellido: String,
    val password: String,
    var isSynced: Boolean = false, // Bandera para saber si está sincronizado con la nube
    var lastAccess: String? = null // Fecha/hora del último acceso (formato legible o null si nunca)
)