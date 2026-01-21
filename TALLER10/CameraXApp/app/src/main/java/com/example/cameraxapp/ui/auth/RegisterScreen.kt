package com.example.cameraxapp.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.cameraxapp.R
import com.example.cameraxapp.auth.AuthState
import com.example.cameraxapp.auth.AuthViewModel

@Composable
fun RegisterScreen(viewModel: AuthViewModel, onRegister: () -> Unit, onNavigateToLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoggedIn -> onRegister()
            is AuthState.InvalidInput -> Toast.makeText(context, "Please enter username and password", Toast.LENGTH_SHORT).show()
            is AuthState.UserAlreadyExists -> Toast.makeText(context, "User already exists", Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logogrupo4),
            contentDescription = "Logo",
            modifier = Modifier.size(150.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            isError = authState is AuthState.InvalidInput || authState is AuthState.UserAlreadyExists
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = authState is AuthState.InvalidInput
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { viewModel.register(username, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
        TextButton(onClick = {
            viewModel.resetAuthState()
            onNavigateToLogin()
        }) {
            Text("Already have an account? Login")
        }
    }
}