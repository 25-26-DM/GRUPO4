package com.example.electronicazytron.controlador


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.electronicazytron.modelo.Usuario
import com.example.electronicazytron.modelo.UsuarioRepository
import com.example.electronicazytron.vistas.ProductScreen


import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.electronicazytron.modelo.ProductoViewModel
import com.example.electronicazytron.modelo.UsuarioViewModel
import com.example.electronicazytron.vista.InsertProducScreen

import com.example.electronicazytron.vista.LoginScreen
import com.example.electronicazytron.vistas.HomeScreen
import com.example.electronicazytron.vistas.RegistrarScreen
import com.example.electronicazytron.vistas.UpdateProductScreen

//encargado de la navegacion de la aplicacion, aqui se define las pantallas que existen y la pantalla inicial del programa
@Composable
fun AppNavigation() {
    val navController = rememberNavController() // rememberNavController() obligatorio para almacenar la ruta de navegacion de la aplicacion
    val productoViewModel: ProductoViewModel = viewModel() //instancia del modelo de productos
    //aqui se puede agregar mas modelos " val nombre_Variable : ClasedeReferenxia = viewModel() "
    val usuarioViewModel: UsuarioViewModel = viewModel() //instancia del modelo de usuarios

    NavHost(
        navController = navController,
        startDestination = "home" // <-- inicia desde HomeScreen
    ) {
        //se crean los metodos, aqui me dice que en la ruta home, existe un composable HomeScreen el cual tiene los metodos
        //onLogin la cual se va a redireccionar al login cuando sea ejecutada
        //onRegistrar que se ejecuta cuando es necesitada y redirige a la pantalla de registro
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
                val esValido = usuarioViewModel.validar(usuario)

                if (esValido) {
                    productoViewModel.cargarProductos()
                    navController.navigate("productos") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                esValido // retorna true/false para el popup
            }
        }


        //definicion de las rutas de la app, aqui se define la ruta productos la cual tiene un constructor
        //ProductScreen que consta de la vista del modelo y un controlador de navegacion
        //en caso de necesitar agregar mas rutas tomar en cuenta que el constructor debe de tener la mista tipologia debido a que android es sensible a
        //mayusculas y minuscaulas, admeas se pasa el modelo para poder agregar la funcionalidad antes de redirigir a otra pantalla
        composable("productos") {
            ProductScreen(productoViewModel, navController)
        }

        //ruta con parametros el cual tiene un codigo, este lo que hace es enviar el codigo a la pantalla updateProduct y prellenar los campos
        //en caso de pedir un caso similar copiar y pegar el metodo luego proceder a modificar las variables
        //en caso de requerir mas variables usar el siguiente codigo de referencia y modificar las variables
        /*
        composable("updateProduct/{codigo}/{nombre}") { backStackEntry ->
        val codigo = backStackEntry.arguments?.getString("codigo")
        val nombre = backStackEntry.arguments?.getString("nombre")
        if (codigo != null && nombre != null) {
        UpdateProductScreen(codigo, nombre, productoViewModel, navController)
        }
        }
        */
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