package com.example.electronicazytron.vistas

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.electronicazytron.modelo.Producto
import com.example.electronicazytron.modelo.ProductoViewModel

@Composable
fun ProductScreen(
    productoViewModel: ProductoViewModel = viewModel(),
    navController: NavController
) {
    LaunchedEffect(Unit) {
        productoViewModel.cargarProductos()
    }

    Scaffold { innerPadding ->
        BodyContent(
            productos = productoViewModel.productos,
            modifier = Modifier.padding(innerPadding),
            onIngresarClick = { navController.navigate("insertProduct") },
            onSalirClick = { navController.navigate("login") },
            onUpdateClick = { codigo -> navController.navigate("updateProduct/$codigo") },
            onDeleteClick = { codigo -> productoViewModel.delete(codigo) }
        )
    }
}

@Composable
fun BodyContent(
    productos: List<Producto>,
    modifier: Modifier = Modifier,
    onIngresarClick: () -> Unit,
    onSalirClick: () -> Unit,
    onUpdateClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 5
    val startIndex = currentPage * pageSize
    val endIndex = minOf((currentPage + 1) * pageSize, productos.size)
    val paginatedProducts = if (startIndex < endIndex) productos.subList(startIndex, endIndex) else emptyList()

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            Button(onClick = onIngresarClick) { Text("Ingresar") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onSalirClick) { Text("Salir") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProductList(
            productos = paginatedProducts,
            onUpdateClick = onUpdateClick,
            onDeleteClick = onDeleteClick
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { if (currentPage > 0) currentPage-- },
                enabled = currentPage > 0
            ) {
                Text("Anterior")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Página ${currentPage + 1}")
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { if (endIndex < productos.size) currentPage++ },
                enabled = endIndex < productos.size
            ) {
                Text("Siguiente")
            }
        }
    }
}

@Composable
fun ProductList(
    productos: List<Producto>,
    onUpdateClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp), // Altura fija para la lista
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(productos) { producto ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(vertical = 8.dp)
                    .clickable { expanded = !expanded },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Código: ${producto.codigo}")
                    Text(text = "Descripción: ${producto.descripcion}")

                    if (expanded) {
                        Text(text = "Costo: $${producto.costo}")
                        Text(text = "Disponibilidad: ${producto.disponibilidad}")
                        Text(text = "Fecha: ${producto.fecha_fab}")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            onUpdateClick(producto.codigo)
                        }) { Text("Modificar") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            onDeleteClick(producto.codigo)
                        }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun DefaultPreview() {
    val productosFake = listOf(
        Producto("P001", "Laptop Lenovo IdeaPad 3", "2024-01-15", 750.0, 10),
        Producto("P002", "Mouse Logitech M185", "2023-11-20", 18.5, 45),
        Producto("P003", "Teclado Redragon K552", "2023-10-05", 50.0, 30),
        Producto("P004", "Monitor Samsung Odyssey G5", "2024-02-28", 350.0, 15),
        Producto("P005", "Auriculares Sony WH-1000XM4", "2023-12-10", 300.0, 25),
        Producto("P006", "Webcam Logitech C920", "2024-03-12", 80.0, 40)
    )
    BodyContent(
        productos = productosFake,
        onIngresarClick = {},
        onSalirClick = {},
        onUpdateClick = {},
        onDeleteClick = {}
    )
}