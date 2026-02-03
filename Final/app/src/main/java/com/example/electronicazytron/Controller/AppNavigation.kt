package com.example.electronicazytron.Controller

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.electronicazytron.viewModel.*
import com.example.electronicazytron.utils.SessionManager
import com.example.electronicazytron.view.HomeScreen
import com.example.electronicazytron.view.ProductScreen
import com.example.electronicazytron.view.RegistrarScreen
import com.example.electronicazytron.view.UpdateProductScreen
import com.example.electronicazytron.view.InsertProductScreen
import com.example.electronicazytron.vistas.*
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Instancia de tus ViewModels
    val productoViewModel: ProductViewModel = viewModel()
    val usuarioViewModel: UserViewModel = viewModel()

    // Observamos si la sesión expiró
    val sesionExpirada by SessionManager.sesionExpirada.collectAsState()

    // -----------------------------------------------------------
    // BUCLE DE VERIFICACIÓN (El corazón del sistema)
    // -----------------------------------------------------------
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Revisar cada 1 segundo
            SessionManager.verificarSesion()
        }
    }

    // -----------------------------------------------------------
    // OBSERVADOR DE EXPIRACIÓN
    // -----------------------------------------------------------
    LaunchedEffect(sesionExpirada) {
        if (sesionExpirada) {
            val rutaActual = navController.currentDestination?.route

            // Rutas PÚBLICAS (donde no importa si la sesión expiró)
            val rutasPublicas = listOf("login", "insertUser", "home")

            // Si estamos en una ruta privada (como productos) y la sesión expiró:
            if (rutaActual !in rutasPublicas) {
                // Navegar al Login y borrar historial
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // -----------------------------------------------------------
    // DETECTOR DE INTERACCIÓN (Resetear inactividad al navegar)
    // -----------------------------------------------------------
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, _, _ ->
            // Cada vez que navegamos, cuenta como "tocar pantalla"
            // Esto resetea el contador de 5 minutos, PERO NO el de 15 minutos.
            SessionManager.tocarPantalla()
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    // -----------------------------------------------------------
    // DEFINICIÓN DE RUTAS
    // -----------------------------------------------------------
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            SessionManager.tocarPantalla()
            HomeScreen(
                onLogin = { navController.navigate("login") },
                onRegistrar = { navController.navigate("insertUser") }
            )
        }

        composable("login") {
            SessionManager.tocarPantalla()
            LoginScreen(
                onValidar = { nombre, password, onResult ->
                    usuarioViewModel.validar(nombre, password) { esValido ->
                        onResult(esValido)
                        if (esValido) {
                            // IMPORTANTE: Aquí inicia el reloj de los 15 minutos
                            SessionManager.iniciarSesion()
                            productoViewModel.cargarProductos()
                            navController.navigate("productos") {
                                popUpTo("home") { inclusive = false }
                            }
                        }
                    }
                },
                onVolver = { navController.popBackStack() }
            )
        }

        composable("productos") {
            SessionManager.tocarPantalla()
            ProductScreen(productoViewModel, navController)
        }

        composable("updateProduct/{codigo}") { backStackEntry ->
            SessionManager.tocarPantalla()
            val codigo = backStackEntry.arguments?.getString("codigo")
            if (codigo != null) {
                UpdateProductScreen(codigo, productoViewModel, navController)
            }
        }

        composable("insertProduct") {
            SessionManager.tocarPantalla()
            InsertProductScreen(productoViewModel, navController)
        }

        composable("insertUser") {
            SessionManager.tocarPantalla()
            RegistrarScreen(usuarioViewModel, navController)
        }
    }
}