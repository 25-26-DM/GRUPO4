package com.example.electronicazytron.model.entities


data class Usuario(
    var nombre: String,
    var apellido: String,
    var password: String = ""
)
