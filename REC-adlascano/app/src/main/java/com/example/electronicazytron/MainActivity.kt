package com.example.electronicazytron

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.electronicazytron.Controller.AppNavigation
import com.example.electronicazytron.services.SyncService
import com.example.electronicazytron.ui.theme.ElectronicaZytronTheme

class MainActivity : ComponentActivity() {
    
    // Registrar el contrato para solicitar permiso de notificaciones
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // El permiso se ha manejado, la notificación se enviará o no según el permiso
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permiso de notificaciones en Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // 1. Iniciar el servicio de sincronización
        val syncIntent = Intent(this, SyncService::class.java)
        startService(syncIntent)

        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            ElectronicaZytronTheme {
                AppNavigation()
            }
        }
    }
}