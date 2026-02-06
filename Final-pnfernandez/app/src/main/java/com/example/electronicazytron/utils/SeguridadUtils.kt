package com.example.electronicazytron.utils

import java.security.MessageDigest

object SeguridadUtils {
    // Convierte texto plano a SHA-256
    fun encriptar(texto: String): String {
        val bytes = texto.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }
}