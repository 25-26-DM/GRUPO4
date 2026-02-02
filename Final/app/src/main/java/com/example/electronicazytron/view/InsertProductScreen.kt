package com.example.electronicazytron.vista

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.electronicazytron.model.entities.Producto
import com.example.electronicazytron.ui.components.DatePickerField
import com.example.electronicazytron.viewModel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertProductScreen(
    productoViewModel: ProductViewModel,
    navController: NavController
) {
    var codigo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaFab by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var disponibilidad by remember { mutableStateOf("") }
    var imagenUri by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val frDescripcion = remember { FocusRequester() }
    val frCosto = remember { FocusRequester() }
    val frDispon = remember { FocusRequester() }
    val frImagen = remember { FocusRequester() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Text("Datos del producto", fontSize = 20.sp)

                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it },
                        label = { Text("Código") },
                        leadingIcon = { Icon(Icons.Default.QrCode, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Text
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
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { frCosto.requestFocus() }
                        )
                    )

                    DatePickerField(
                        label = "Fecha fabricación (yyyy-MM-dd)",
                        value = fechaFab,
                        onDateSelected = { fechaFab = it },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = costo,
                        onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) costo = it },
                        label = { Text("Costo") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(frCosto),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Decimal
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { frDispon.requestFocus() }
                        )
                    )

                    OutlinedTextField(
                        value = disponibilidad,
                        onValueChange = { if (it.matches(Regex("^\\d*$"))) disponibilidad = it },
                        label = { Text("Disponibilidad") },
                        leadingIcon = { Icon(Icons.Default.Inventory, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(frDispon),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { frImagen.requestFocus() }
                        )
                    )

                    OutlinedTextField(
                        value = imagenUri,
                        onValueChange = { imagenUri = it },
                        label = { Text("URL Imagen (opcional)") },
                        leadingIcon = { Icon(Icons.Default.Link, null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(frImagen),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Uri
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
                            onClick = { navController.popBackStack() }
                        ) { Text("Cancelar") }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { showDialog = true },
                            enabled = codigo.isNotBlank()
                                    && descripcion.isNotBlank()
                                    && fechaFab.isNotBlank()
                        ) { Text("Guardar") }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar producto") },
            text = { Text("¿Deseas guardar este producto?") },
            confirmButton = {
                TextButton(onClick = {
                    val urlFinal =
                        if (imagenUri.isBlank()) "https://picsum.photos/seed/${codigo.trim()}/400/400"
                        else imagenUri.trim()

                    productoViewModel.insert(
                        Producto(
                            codigo = codigo.trim(),
                            descripcion = descripcion.trim(),
                            fecha_fab = fechaFab.trim(),
                            costo = costo.toDoubleOrNull() ?: 0.0,
                            disponibilidad = disponibilidad.toIntOrNull() ?: 0,
                            imagenUri = urlFinal,
                            eliminado = false
                        )
                    )

                    showDialog = false
                    navController.navigate("productos") {
                        popUpTo("insertProduct") { inclusive = true }
                    }
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
