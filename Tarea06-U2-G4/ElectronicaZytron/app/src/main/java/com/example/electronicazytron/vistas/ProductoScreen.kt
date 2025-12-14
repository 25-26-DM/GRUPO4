package com.example.electronicazytron.vistas

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.electronicazytron.modelo.ProductoViewModel
import com.example.electronicazytron.modelo.Producto

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProductScreen(productoViewModel: ProductoViewModel = viewModel(),
                  navController: NavController) {
    // Cargar productos al entrar a la pantalla
    LaunchedEffect(Unit) {
        productoViewModel.cargarProductos()
    }

    Scaffold {
        BodyContent(productoViewModel,navController)
    }
}

@Composable
fun BodyContent(productoViewModel: ProductoViewModel,navController: NavController) {
    val productos = productoViewModel.productos
    ProductList(productos,navController,productoViewModel)
}

@Composable
fun ProductList(productos: List<Producto>,navController: NavController,productoViewModel: ProductoViewModel) {

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(onClick = {navController.navigate("insertProduct")}) { Text("Ingresar") }
            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true } // borra todo el back stack
                    launchSingleTop = true          // evita duplicar login
                }
            }) { Text("Salir") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(productos) { producto ->
                var expanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "${producto.codigo}")
                    Text(text = "${producto.descripcion}")

                    if (expanded) {
                        Text(text = "$${producto.costo}")
                        Text(text = "Stock: ${producto.disponibilidad}")
                        Text(text = "Fecha de Fabricación: ${producto.fecha_fab}")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            navController.navigate("updateProduct/${producto.codigo}") }) { Text("Modificar") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            productoViewModel.delete(producto.codigo)
                        }) { Text("Eliminar")}
                    }
                }
            }
        }
    }

}



@Preview(showSystemUi = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun DefaultPreview() {
    val productosFake = listOf(
        Producto("P001", "Laptop Lenovo IdeaPad 3", "2024-01-15", 750.0, 10),
        Producto("P002", "Mouse Logitech M185", "2023-11-20", 18.5, 45)
    )
    BodyContentPreview(productosFake)
}

@Composable
fun BodyContentPreview(productos: List<Producto>) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // Primera columna: botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = {}) {
                Text("Ingresar")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = {}) {
                Text("Salir")
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // espacio entre columnas

        // Segunda columna: productos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (producto in productos) {
                var expanded by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .clickable { expanded = !expanded }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Siempre mostrar código y nombre
                    Text(text = "${producto.codigo}")
                    Text(text = "${producto.descripcion}")

                    // Mostrar detalles solo si está expandido
                    if (expanded) {
                        Text(text = "$${producto.costo}")
                        Text(text = "Stock: ${producto.disponibilidad}")
                        Text(text = "Fecha: ${producto.fecha_fab}")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center

                    ) {
                        Button(onClick = {}) {
                            Text("Modificar")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {}) {
                            Text("Eliminar")
                        }
                    }
                }
            }
        }
    }

}
