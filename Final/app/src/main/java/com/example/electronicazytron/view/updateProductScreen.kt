package com.example.electronicazytron.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.electronicazytron.model.entities.Producto
import com.example.electronicazytron.ui.components.DatePickerField
import com.example.electronicazytron.viewModel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(
    codigo: String,
    productoViewModel: ProductViewModel,
    navController: NavController
) {
    var producto by remember { mutableStateOf<Producto?>(null) }

    LaunchedEffect(codigo) {
        productoViewModel.find(codigo) { producto = it }
    }

    if (producto == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Producto no encontrado")
        }
        return
    }

    val prod = producto!!

    var descripcion by remember { mutableStateOf(prod.descripcion) }
    var fechaFab by remember { mutableStateOf(prod.fecha_fab) }
    var costo by remember { mutableStateOf(prod.costo.toString()) }
    var disponibilidad by remember { mutableStateOf(prod.disponibilidad.toString()) }
    var imagenUri by remember { mutableStateOf(prod.imagenUri) }

    var showConfirmDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val frDescripcion = remember { FocusRequester() }
    val frCosto = remember { FocusRequester() }
    val frDispon = remember { FocusRequester() }

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
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Actualizar Producto", fontSize = 24.sp)

                OutlinedTextField(
                    value = prod.codigo,
                    onValueChange = {},
                    label = { Text("Código") },
                    leadingIcon = { Icon(Icons.Default.QrCode, null) },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                AsyncImage(
                    model = imagenUri,
                    contentDescription = "Imagen del producto",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )

                OutlinedTextField(
                    value = imagenUri,
                    onValueChange = { imagenUri = it },
                    label = { Text("URL Imagen") },
                    leadingIcon = { Icon(Icons.Default.Link, null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { frDescripcion.requestFocus() }
                    )
                )

                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(frDescripcion),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { frCosto.requestFocus() }
                    )
                )

                // 📅 Fecha con calendario (no se escribe)
                DatePickerField(
                    label = "Fecha de Fabricación (yyyy-MM-dd)",
                    value = fechaFab,
                    onDateSelected = { fechaFab = it },
                    leadingIcon = { Icon(Icons.Default.DateRange, null) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = costo,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*\\.?\\d*$"))) costo = it
                    },
                    label = { Text("Costo") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(frCosto),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { frDispon.requestFocus() }
                    )
                )

                OutlinedTextField(
                    value = disponibilidad,
                    onValueChange = {
                        if (it.matches(Regex("^\\d*$"))) disponibilidad = it
                    },
                    label = { Text("Disponibilidad") },
                    leadingIcon = { Icon(Icons.Default.Inventory, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(frDispon),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            navController.navigate("productos") {
                                popUpTo("updateProduct/$codigo") { inclusive = true }
                            }
                        }
                    ) { Text("Cancelar") }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { showConfirmDialog = true }
                    ) { Text("Actualizar") }
                }
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar actualización") },
            text = { Text("¿Deseas guardar los cambios del producto?") },
            confirmButton = {
                Button(onClick = {
                    productoViewModel.update(
                        Producto(
                            codigo = prod.codigo,
                            descripcion = descripcion.trim(),
                            fecha_fab = fechaFab,
                            costo = costo.toDoubleOrNull() ?: 0.0,
                            disponibilidad = disponibilidad.toIntOrNull() ?: 0,
                            imagenUri = imagenUri.trim(),
                            eliminado = prod.eliminado
                        )
                    )

                    showConfirmDialog = false
                    navController.navigate("productos") {
                        popUpTo("updateProduct/$codigo") { inclusive = true }
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
