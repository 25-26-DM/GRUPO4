package com.example.electronicazytron.vistas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.electronicazytron.modelo.Producto
import com.example.electronicazytron.modelo.ProductoViewModel

// ----------------------
// VisualTransformation Fecha
// ----------------------
private class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8)
        val out = buildString {
            for (i in trimmed.indices) {
                append(trimmed[i])
                if (i == 3 || i == 5) append("-")
            }
        }

        val offsetMapping = object : OffsetMapping {
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

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

@Composable
fun UpdateProductScreen(
    codigo: String,
    productoViewModel: ProductoViewModel,
    navController: NavController
) {
    val producto = productoViewModel.productos.find { it.codigo == codigo }

    if (producto == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Producto no encontrado")
        }
        return
    }

    var descripcion by remember { mutableStateOf(producto.descripcion) }
    var fechaFab by remember { mutableStateOf(producto.fecha_fab.filter { it.isDigit() }) }
    var costo by remember { mutableStateOf(producto.costo.toString()) }
    var disponibilidad by remember { mutableStateOf(producto.disponibilidad.toString()) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
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

                // Código (solo lectura)
                OutlinedTextField(
                    value = producto.codigo,
                    onValueChange = {},
                    label = { Text("Código") },
                    leadingIcon = { Icon(Icons.Default.QrCode, null) },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = fechaFab,
                    onValueChange = {
                        val digits = it.filter { c -> c.isDigit() }
                        if (digits.length <= 8) fechaFab = digits
                    },
                    label = { Text("Fecha de Fabricación (yyyy-MM-dd)") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = DateVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = costo,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d*$"))) costo = it
                    },
                    label = { Text("Costo") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = disponibilidad,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*$"))) disponibilidad = it
                    },
                    label = { Text("Disponibilidad") },
                    leadingIcon = { Icon(Icons.Default.Inventory, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate("productos") {
                                popUpTo("updateProduct/$codigo") { inclusive = true }
                            }
                        }
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { showConfirmDialog = true }
                    ) {
                        Text("Actualizar")
                    }
                }
            }
        }
    }

    // ----------------------
    // Diálogo de Confirmación
    // ----------------------
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar actualización") },
            text = { Text("¿Deseas guardar los cambios del producto?") },
            confirmButton = {
                Button(onClick = {
                    val formattedDate =
                        DateVisualTransformation()
                            .filter(AnnotatedString(fechaFab))
                            .text
                            .toString()

                    productoViewModel.update(
                        codigo,
                        Producto(
                            codigo = codigo,
                            descripcion = descripcion,
                            fecha_fab = formattedDate,
                            costo = costo.toDoubleOrNull() ?: 0.0,
                            disponibilidad = disponibilidad.toIntOrNull() ?: 0
                        )
                    )

                    showConfirmDialog = false

                    navController.navigate("productos") {
                        popUpTo("updateProduct/$codigo") { inclusive = true }
                    }
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
