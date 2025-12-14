package com.example.electronicazytron.vista

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.electronicazytron.modelo.Producto
import com.example.electronicazytron.modelo.ProductoViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InsertProducScreen(productoViewModel: ProductoViewModel,
                       navController: NavController
) {
    Scaffold {
        BodyContent(productoViewModel,navController)
    }
}

@Composable
fun BodyContent(productoViewModel: ProductoViewModel,
                navController: NavController) {
    var codigo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha_fab by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var disponibilidad by remember { mutableStateOf("") }


    Column(modifier = Modifier.fillMaxSize().padding(top = 200.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Código de Producto:")
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            value = codigo,
            onValueChange = { codigo = it },
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Fecha de Fabricación:")
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            value = fecha_fab,
            onValueChange = { fecha_fab = it },
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Costo:")
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            value = costo,
            onValueChange = {newValue ->
                if (newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                    costo = newValue
                }
            },
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Disponibididad de Producto:")
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            value = disponibilidad,
            onValueChange = {newValue ->
                if (newValue.matches(Regex("^\\d*\$"))) {
                    disponibilidad = newValue
                }
            },
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Descripción de Producto:")
        Spacer(modifier = Modifier.height(15.dp))
        TextField(
            value = descripcion,
            onValueChange = { descripcion = it },
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = {
            productoViewModel.insert(Producto(codigo,descripcion,fecha_fab,costo.toDouble(),disponibilidad.toInt()))

            navController.navigate("productos"){
                popUpTo("insertProduct") {
                    inclusive = true
                }}

        }) {
            Text("Ingresar")
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
        Text("Código de Producto:")
        Spacer(modifier = Modifier.height(8.dp))
        TextField(value = "", onValueChange = {})

        Spacer(modifier = Modifier.height(8.dp))
        Text("Fecha de Fabricación:")
        TextField(value = "", onValueChange = {})

        Spacer(modifier = Modifier.height(8.dp))
        Text("Costo:")
        TextField(value = "", onValueChange = {})

        Spacer(modifier = Modifier.height(8.dp))
        Text("Disponibilidad:")
        TextField(value = "", onValueChange = {})

        Spacer(modifier = Modifier.height(8.dp))
        Text("Descripción:")
        TextField(value = "", onValueChange = {})

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {}) {
            Text("Ingresar")
        }
    }
}
