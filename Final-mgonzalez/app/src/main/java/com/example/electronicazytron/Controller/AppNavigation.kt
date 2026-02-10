package com.example.electronicazytron.Controller

import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
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
import com.example.electronicazytron.utils.LastLogRepository
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import com.example.electronicazytron.services.SyncService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Instancia de tus ViewModels
    val productoViewModel: ProductViewModel = viewModel()
    val usuarioViewModel: UserViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    // Observamos el último acceso (null = sin valor aun)
    val lastLog by LastLogRepository.lastLog.collectAsState()
    // Observamos el nombre del usuario logeado
    val currentUser by SessionManager.currentUser.collectAsState()

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
                // Limpiar lo mostrado en el TopBar (mostrar 'Bienvenido')
                try { LastLogRepository.setFromDb(null) } catch (_: Exception) {}
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
    // UI: Scaffold con TopAppBar que muestra el último acceso
    // -----------------------------------------------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Electronica Zytron") },
                actions = {
                    val display = if (!lastLog.isNullOrEmpty()) {
                        val namePart = if (!currentUser.isNullOrEmpty()) " — $currentUser" else ""
                        "Ultima conexión: ${lastLog}$namePart"
                    } else {
                        "Bienvenido"
                    }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = display,
                            modifier = Modifier.padding(end = 12.dp),
                            fontSize = 12.sp,
                            textAlign = TextAlign.End
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // -----------------------------------------------------------
        // DEFINICIÓN DE RUTAS
        // -----------------------------------------------------------
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
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
            val context = LocalContext.current
            LoginScreen(
                onValidar = { nombre, password, onResult ->
                    usuarioViewModel.validar(nombre, password) { esValido ->
                        onResult(esValido)
                        if (esValido) {
                            // IMPORTANTE: Aquí inicia el reloj de los 15 minutos
                            SessionManager.iniciarSesion()
                            // Registrar usuario actual para mostrar nombre en la UI
                            SessionManager.setCurrentUser(nombre)
                            productoViewModel.cargarProductos()
                            // 1) Obtener desde la BD el último acceso registrado para este usuario
                            usuarioViewModel.obtenerLastAccess(nombre) { dbValue ->
                                // Mostrar lo que ya está en la BD (null -> "Bienvenido")
                                LastLogRepository.setFromDb(dbValue)

                                // Consultar el servicio remoto y SIEMPRE guardar en la BD
                                // Si no se puede obtener la hora remota, usar la hora local formateada
                                val hadValue = dbValue != null
                                coroutineScope.launch {
                                    try {
                                        var fetched = LastLogRepository.fetchFromServer()
                                        if (fetched.isNullOrEmpty()) {
                                            // Generar hora local como fallback
                                            try {
                                                val odt = java.time.OffsetDateTime.now()
                                                val fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                                                fetched = odt.format(fmt)
                                            } catch (ex: Exception) {
                                                fetched = null
                                            }
                                        }

                                                if (!fetched.isNullOrEmpty()) {
                                                    // Guardar en la BD para la próxima vez
                                                    usuarioViewModel.actualizarLastAccess(nombre, fetched)
                                                    // NOTA: no actualizar lo mostrado ahora — mostrar el valor almacenado en BD hasta el próximo login
                                                }
                                    } catch (_: Exception) {
                                        // Ignorar fallos de red; ya mostramos el valor en BD (o "Bienvenido")
                                    }
                                }
                            }
                            // Iniciar servicio de sincronización (SyncService)
                            try {
                                val intent = Intent(context, SyncService::class.java)
                                context.startService(intent)
                            } catch (e: Exception) {
                                // Ignorar si no se puede iniciar el servicio en este entorno
                            }
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
}