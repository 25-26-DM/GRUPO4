package com.example.marsphotos.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.marsphotos.R

/**
 * Modelo de datos para un Usuario
 */
data class User(val name: String, val surname: String)

/**
 * Lista de usuarios autorizados en memoria
 */
val authorizedUsers = listOf(
    User("Byron", "Condolo"),
    User("Pamela", "Fernandez"),
    User("Marielena", "Gonzalez"),
    User("Angelo", "Lascano"),
    User("Ruth", "Rosero"),
    User("Joan", "Santamaria"),
    User("Dennis", "Trujillo")
)

/**
 * Pantalla de inicio de sesión que solicita un nombre de usuario y contraseña (apellido).
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        // Campo de usuario (Nombre)
        TextField(
            value = username,
            onValueChange = { 
                username = it
                showError = false 
            },
            label = { Text("Usuario (Nombre)") },
            modifier = Modifier.fillMaxWidth(),
            isError = showError
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo de contraseña (Apellido)
        TextField(
            value = password,
            onValueChange = { 
                password = it
                showError = false 
            },
            label = { Text("Contraseña (Apellido)") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            isError = showError
        )
        
        if (showError) {
            Text(
                text = "Usuario o contraseña incorrectos",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { 
                // Verificamos si los datos coinciden con algún usuario de la lista
                val userFound = authorizedUsers.find { 
                    it.name.equals(username, ignoreCase = true) && 
                    it.surname.equals(password, ignoreCase = true) 
                }
                
                if (userFound != null) {
                    onLoginSuccess(userFound.name)
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Iniciar Sesión")
        }
    }
}
