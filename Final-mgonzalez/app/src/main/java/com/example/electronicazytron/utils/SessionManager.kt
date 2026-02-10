package com.example.electronicazytron.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    // 1. TIEMPO DE INACTIVIDAD (5 Minutos)
    // Si el usuario no toca la pantalla en este tiempo, se cierra.
    private const val TIEMPO_INACTIVIDAD = 5 * 60 * 1000L

    // 2. DURACIÓN MÁXIMA DE SESIÓN (15 Minutos)
    // Aunque el usuario esté activo, a los 15 min se le fuerza a salir.
    private const val DURACION_MAXIMA_SESION = 15 * 60 * 1000L

    private var horaInicioSesion: Long = 0
    private var ultimaInteraccion: Long = 0
    private var nombreUsuarioActual: String? = null

    // Estado observable para cerrar la sesión
    private val _sesionExpirada = MutableStateFlow(false)
    val sesionExpirada = _sesionExpirada.asStateFlow()

    // Estado observable para el usuario actualmente logeado
    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser = _currentUser.asStateFlow()

    // Se llama SOLO cuando el Login es exitoso
    fun iniciarSesion() {
        val tiempoActual = System.currentTimeMillis()
        horaInicioSesion = tiempoActual
        ultimaInteraccion = tiempoActual
        _sesionExpirada.value = false
    }

    fun setCurrentUser(nombre: String) {
        nombreUsuarioActual = nombre
        _currentUser.value = nombre
    }

    // Se llama cada vez que el usuario cambia de pantalla o toca algo
    fun tocarPantalla() {
        ultimaInteraccion = System.currentTimeMillis()
    }

    // Esta función se ejecutará en bucle en AppNavigation
    fun verificarSesion() {
        // Si ya está expirada, no hacemos nada
        if (_sesionExpirada.value) return

        val tiempoActual = System.currentTimeMillis()

        // REGLA 1: Verificar Inactividad (5 min)
        if (tiempoActual - ultimaInteraccion > TIEMPO_INACTIVIDAD) {
            _sesionExpirada.value = true
            return
        }

        // REGLA 2: Verificar Duración Máxima (15 min)
        // (Solo si la hora de inicio es válida, es decir > 0)
        if (horaInicioSesion > 0 && (tiempoActual - horaInicioSesion > DURACION_MAXIMA_SESION)) {
            _sesionExpirada.value = true
        }
    }

    // Para cerrar sesión manual o forzada
    fun cerrarSesion() {
        _sesionExpirada.value = true
        horaInicioSesion = 0
        ultimaInteraccion = 0
        nombreUsuarioActual = null
        _currentUser.value = null
    }
}