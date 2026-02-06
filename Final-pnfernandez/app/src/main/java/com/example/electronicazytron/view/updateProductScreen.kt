package com.example.electronicazytron.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import java.io.File
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
import com.example.electronicazytron.utils.NetworkConnection

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
    val context = LocalContext.current
    val isOnline = remember { NetworkConnection(context).isNetworkAvailable() }
    val PLACEHOLDER_IMAGE_URL = "https://via.placeholder.com/600x400.png?text=Sin+Internet"
    // Observamos estado global de imagen (se actualiza tras subir)
    val imagenUriFromVM = productoViewModel.imagenUriState

    var capturedFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedFile != null) {
            productoViewModel.uploadImage(capturedFile!!)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = com.example.electronicazytron.utils.CameraUtils.createTempImageFile(context)
            capturedFile = file
            val uri: Uri = com.example.electronicazytron.utils.CameraUtils.getUriForFile(context, file)
            cameraLauncher.launch(uri)
        }
    }

    // Inicializamos el estado global de imagen con la imagen actual del producto
    LaunchedEffect(prod) {
        productoViewModel.updateImagenUri(prod.imagenUri)
    }

    // Si no hay internet, fijar URL placeholder para que el usuario la cambie luego
    LaunchedEffect(isOnline) {
        if (!isOnline) {
            productoViewModel.updateImagenUri(PLACEHOLDER_IMAGE_URL)
        }
    }

    // Si la VM cambia la URI (tras subir), actualizamos la URI local para mostrarla
    LaunchedEffect(imagenUriFromVM) {
        if (imagenUriFromVM.isNotEmpty()) {
            imagenUri = imagenUriFromVM
        }
    }

    var showConfirmDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val frDescripcion = remember { FocusRequester() }
    val frCosto = remember { FocusRequester() }
    val frDispon = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.Top,
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

                // Se muestra la imagen actual; si no hay, mostramos botón para tomar foto
                if (imagenUri.isNotEmpty()) {
                    AsyncImage(
                        model = imagenUri,
                        contentDescription = "Imagen del producto",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isOnline) {
                            IconButton(onClick = {
                                val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                    val file = com.example.electronicazytron.utils.CameraUtils.createTempImageFile(context)
                                    capturedFile = file
                                    val uri: Uri = com.example.electronicazytron.utils.CameraUtils.getUriForFile(context, file)
                                    cameraLauncher.launch(uri)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Tomar Foto", modifier = Modifier.size(48.dp))
                            }
                        } else {
                            LaunchedEffect(Unit) {
                                productoViewModel.updateImagenUri(PLACEHOLDER_IMAGE_URL)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudOff, contentDescription = "Sin Internet", modifier = Modifier.size(48.dp))
                                Text("Sin conexión: cámara deshabilitada", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

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

                // Botón para cambiar foto si ya existe imagen y hay internet
                if (imagenUri.isNotEmpty() && isOnline) {
                    TextButton(
                        onClick = {
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                val file = com.example.electronicazytron.utils.CameraUtils.createTempImageFile(context)
                                capturedFile = file
                                val uri: Uri = com.example.electronicazytron.utils.CameraUtils.getUriForFile(context, file)
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) { Text("Cambiar Foto") }
                }

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
                        onClick = { showConfirmDialog = true },
                        enabled = !productoViewModel.isUploading
                    ) {
                        if (productoViewModel.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Actualizar")
                        }
                    }
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
                    // Si el ViewModel tiene una imagen cargada (remota o local), la usamos;
                    // en caso contrario usamos el valor actual del campo `imagenUri`.
                    val finalImagen = productoViewModel.imagenUriState.takeIf { it.isNotEmpty() } ?: imagenUri.trim()

                    productoViewModel.update(
                        Producto(
                            codigo = prod.codigo,
                            descripcion = descripcion.trim(),
                            fecha_fab = fechaFab,
                            costo = costo.toDoubleOrNull() ?: 0.0,
                            disponibilidad = disponibilidad.toIntOrNull() ?: 0,
                            imagenUri = finalImagen,
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
