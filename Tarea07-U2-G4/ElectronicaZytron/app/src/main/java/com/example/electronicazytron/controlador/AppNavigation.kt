package com.example.electronicazytron.controlador


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.electronicazytron.modelo.Usuario
import com.example.electronicazytron.vistas.LoginScreen
import com.example.electronicazytron.vistas.ProductScreen


import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.electronicazytron.modelo.ProductoViewModel
import com.example.electronicazytron.modelo.UsuarioViewModel

import com.example.electronicazytron.vista.InsertProductScreen
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
                val valido = usuarioViewModel.validar(usuario)

                if (valido) {
                    productoViewModel.cargarProductos()
                    navController.navigate("productos") {
                        popUpTo("login") { inclusive = true }
                    }
                }

                valido
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
            InsertProductScreen(productoViewModel, navController)
        }

        composable("insertUser") {
            RegistrarScreen(usuarioViewModel, navController)
        }

    }
}