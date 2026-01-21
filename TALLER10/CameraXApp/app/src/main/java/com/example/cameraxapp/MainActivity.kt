package com.example.cameraxapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cameraxapp.auth.AppDatabase
import com.example.cameraxapp.auth.AuthViewModel
import com.example.cameraxapp.auth.AuthViewModelFactory
import com.example.cameraxapp.ui.auth.LoginScreen
import com.example.cameraxapp.ui.auth.RegisterScreen
import com.example.cameraxapp.ui.camera.CameraScreen
import com.example.cameraxapp.ui.gallery.GalleryScreen
import com.example.cameraxapp.ui.theme.CameraXAppTheme

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Camera : Screen("camera")
    object Gallery : Screen("gallery")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraXAppTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val userDao = AppDatabase.getDatabase(context).userDao()
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(userDao))

    Scaffold {
         padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLogin = {
                        navController.navigate(Screen.Camera.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegister = {
                        navController.navigate(Screen.Camera.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigateUp() }
                )
            }
            composable(Screen.Camera.route) {
                CameraScreen(navController)
            }
            composable(Screen.Gallery.route) {
                GalleryScreen()
            }
        }
    }
}
