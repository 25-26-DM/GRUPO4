/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.marsphotos.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.marsphotos.R
import com.example.marsphotos.ui.screens.HomeScreen
import com.example.marsphotos.ui.screens.LoginScreen
import com.example.marsphotos.ui.screens.MarsViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MarsPhotosApp() {
    // Estados para persistir la información del usuario logueado y la hora de inicio
    var loggedInUser by remember { mutableStateOf<String?>(null) }
    var loginTime by remember { mutableStateOf("") }

    // Si no hay un usuario logueado, mostramos la pantalla de Login
    if (loggedInUser == null) {
        LoginScreen(onLoginSuccess = { user ->
            loggedInUser = user
            // Registramos la hora actual formateada al momento de hacer login
            loginTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        })
    } else {
        // Una vez logueado, se muestra el contenido principal de la app
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                // Pasamos los datos del usuario a la barra superior para mostrarlos
                MarsTopAppBar(
                    scrollBehavior = scrollBehavior,
                    username = loggedInUser ?: "",
                    loginTime = loginTime
                )
            }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                val marsViewModel: MarsViewModel = viewModel()
                HomeScreen(
                    marsUiState = marsViewModel.marsUiState,
                    contentPadding = it
                )
            }
        }
    }
}

/**
 * Barra superior personalizada que muestra el título de la app y la info del login.
 */
@Composable
fun MarsTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    username: String, // Nombre del usuario a mostrar
    loginTime: String, // Hora de inicio de sesión
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Column {
                // Título principal
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                )
                // Subtítulo con la información de sesión requerida
                Text(
                    text = "Usuario: $username | Login: $loginTime",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        modifier = modifier
    )
}
