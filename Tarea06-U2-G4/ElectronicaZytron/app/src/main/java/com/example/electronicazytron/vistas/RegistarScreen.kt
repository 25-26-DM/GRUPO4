package com.example.electronicazytron.vistas

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.example.electronicazytron.modelo.Usuario
import com.example.electronicazytron.modelo.UsuarioViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RegistrarScreen(userViewModel: UsuarioViewModel,
                    navController: NavController){
    Scaffold() {
        BodyContent(userViewModel,navController)
    }
}

@Composable
fun BodyContent(userViewModel: UsuarioViewModel,
                navController: NavController){

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(top = 200.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Nombre:")
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            value = nombre,
            onValueChange = { nombre = it },
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Apellido")
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            value = apellido,
            onValueChange = { apellido = it },
        )
        //************************************
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            userViewModel.insert(Usuario(nombre,apellido))
            navController.navigate("login"){
                popUpTo("insertUser") {
                    inclusive = true
                }}

        }) {
            Text("Registrar")
        }
    }
    }


// Preview para InsertProductScreen
@Preview(showSystemUi = true)
@Composable
fun InsertProductScreenEmptyPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Nombre:")
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = "", onValueChange = {})

        Spacer(modifier = Modifier.height(8.dp))
        Text("Apellido:")
        TextField(value = "", onValueChange = {})

    }
}
