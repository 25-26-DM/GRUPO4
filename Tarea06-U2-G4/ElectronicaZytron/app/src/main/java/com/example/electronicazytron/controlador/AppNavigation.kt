package com.example.electronicazytron.controlador


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.electronicazytron.modelo.Usuario
import com.example.electronicazytron.modelo.UsuarioRepository
import com.example.electronicazytron.vista.LoginScreen
import com.example.electronicazytron.vistas.ProductScreen


import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.electronicazytron.modelo.ProductoViewModel
import com.example.electronicazytron.modelo.UsuarioViewModel

import com.example.electronicazytron.vista.InsertProducScreen
import com.example.electronicazytron.vistas.HomeScreen
import com.example.electronicazytron.vistas.RegistrarScreen
import com.example.electronicazytron.vistas.UpdateProductScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val productoViewModel: ProductoViewModel = viewModel()

    val usuarioViewModel: UsuarioViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home" // <-- inicia desde HomeScreen
    ) {
        composable("home") {
            HomeScreen(
                onLogin = {
                    navController.navigate("login") //<-- Redireccion de boton al login
                },
                onRegistrar={
                    navController.navigate("insertUser") //<-- Redirreccion del boton al registro de usuarios
                }
            )
        }

        composable("login") {
            LoginScreen { nombre, apellido ->
                val usuario = Usuario(nombre, apellido)
                if (usuarioViewModel.validar(usuario)) {
                    productoViewModel.cargarProductos() // cargar productos antes de navegar
                    navController.navigate("productos") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }

        composable("productos") {
            ProductScreen(productoViewModel, navController)
        }

        composable("updateProduct/{codigo}") { backStackEntry ->
            val codigo = backStackEntry.arguments?.getString("codigo")
            if (codigo != null) {
                UpdateProductScreen(codigo, productoViewModel, navController)
            }
        }

        composable("insertProduct") {
            InsertProducScreen(productoViewModel, navController)
        }

        composable("insertUser") {
            RegistrarScreen(usuarioViewModel, navController)
        }
    }
}



