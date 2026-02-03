package com.example.electronicazytron

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.electronicazytron.Controller.AppNavigation
import com.example.electronicazytron.services.SyncService
import com.example.electronicazytron.ui.theme.ElectronicaZytronTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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