package com.example.electronicazytron.vistas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.electronicazytron.modelo.Producto
import com.example.electronicazytron.modelo.ProductoViewModel

// ----------------------
// VisualTransformation
// ----------------------
private class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""

        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 3 || i == 5) {
                if (i < trimmed.length - 1) out += "-"
            }
        }

        val offsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                when {
                    offset <= 4 -> offset
                    offset <= 6 -> offset + 1
                    else -> offset + 2
                }

            override fun transformedToOriginal(offset: Int): Int =
                when {
                    offset <= 4 -> offset
                    offset <= 7 -> offset - 1
                    else -> offset - 2
                }
        }

        return TransformedText(AnnotatedString(out), offsetTranslator)
    }
}

@Composable
fun UpdateProductScreen(
    codigo: String,
    productoViewModel: ProductoViewModel,
    navController: NavController
) {
    val producto = productoViewModel.productos.find { it.codigo == codigo }

    if (producto != null) {
        var costo by remember { mutableStateOf(producto.costo.toString()) }
        var disponibilidad by remember { mutableStateOf(producto.disponibilidad.toString()) }
        var fechaFab by remember {
            mutableStateOf(producto.fecha_fab.filter { it.isDigit() })
        }
        var descripcion by remember { mutableStateOf(producto.descripcion) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Actualizar Producto",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Código: ${producto.codigo}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 🔹 CAMBIO IMPORTANTE AQUÍ
                    OutlinedTextField(
                        value = fechaFab,
                        onValueChange = { newValue ->
                            val digitsOnly = newValue.filter { it.isDigit() }
                            if (digitsOnly.length <= 8) {
                                fechaFab = digitsOnly
                            }
                        },
                        label = { Text("Fecha de Fabricación (yyyy-MM-dd)") },
                        leadingIcon = {
                            Icon(Icons.Default.DateRange, null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        visualTransformation = DateVisualTransformation()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = costo,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                costo = newValue
                            }
                        },
                        label = { Text("Costo") },
                        leadingIcon = {
                            Icon(Icons.Default.AttachMoney, null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = disponibilidad,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d*$"))) {
                                disponibilidad = newValue
                            }
                        },
                        label = { Text("Existencias") },
                        leadingIcon = {
                            Icon(Icons.Default.Inventory, null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                val formattedDate =
                                    DateVisualTransformation()
                                        .filter(AnnotatedString(fechaFab))
                                        .text
                                        .toString()

                                productoViewModel.update(
                                    codigo,
                                    Producto(
                                        codigo,
                                        descripcion,
                                        formattedDate,
                                        costo.toDoubleOrNull() ?: 0.0,
                                        disponibilidad.toIntOrNull() ?: 0
                                    )
                                )

                                navController.navigate("productos") {
                                    popUpTo("updateProduct/$codigo") {
                                        inclusive = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Modificar")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = {
                                navController.navigate("productos") {
                                    popUpTo("updateProduct/$codigo") {
                                        inclusive = true
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Regresar")
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Producto no encontrado")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DefaultPreview2() {
    val producto =
        Producto("P001", "Laptop Lenovo IdeaPad 3", "2024-01-15", 750.0, 10)
    val productoViewModel = ProductoViewModel()
    productoViewModel.insert(producto)
    val navController = rememberNavController()

    MaterialTheme {
        UpdateProductScreen(
            codigo = "P001",
            productoViewModel = productoViewModel,
            navController = navController
        )
    }
}
