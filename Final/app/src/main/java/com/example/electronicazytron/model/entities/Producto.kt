package com.example.electronicazytron.model.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName= "productos")
//clase principal del objeto producto
data class Producto(

    @PrimaryKey val codigo: String,
    val descripcion: String,
    var fecha_fab: String,
    var costo: Double,
    var disponibilidad: Int,
    var imagenUri: String = "",
    var eliminado: Boolean = false
)