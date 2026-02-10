package com.example.electronicazytron.model.repository

import com.example.electronicazytron.model.entities.Usuario
import com.example.electronicazytron.utils.SeguridadUtils
import com.example.electronicazytron.auth.User
import com.example.electronicazytron.auth.UserDao

class UserRepository(private val userDao: UserDao) {

    //Al momento de llamar la clase se verifica en la lista creada
    private var usuarios = mutableListOf<Usuario>(
        Usuario("Byron", "Condolo"),
        Usuario("Pamela", "Fernandez"),
        Usuario("Marielena", "Gonzalez"),
        Usuario("Angelo", "Lascano"),
        Usuario("Ruth", "Rosero"),
        Usuario("Joan", "Santamaria"),
        Usuario("Dennis", "Trujillo"),
        )
    // Registra encriptando la contraseña
    suspend fun registrar(nombre: String, apellido: String, passwordPlano: String) {
        val passwordHash = SeguridadUtils.encriptar(passwordPlano)

        val nuevoUsuario = User(
            nombre = nombre,
            apellido = apellido,
            password = passwordHash // Guardamos el hash
        )
        userDao.insert(nuevoUsuario)
    }

    // Valida encriptando la entrada y comparando hashes
    suspend fun login(nombre: String, passwordPlano: String): Boolean {
        val usuario = userDao.buscarPorNombre(nombre) ?: return false

        val hashInput = SeguridadUtils.encriptar(passwordPlano)
        return usuario.password == hashInput
    }
}