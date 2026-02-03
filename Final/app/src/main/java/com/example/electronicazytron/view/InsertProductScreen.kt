package com.example.electronicazytron.view

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.electronicazytron.model.entities.Producto
import com.example.electronicazytron.ui.components.DatePickerField
import com.example.electronicazytron.utils.CameraUtils
import com.example.electronicazytron.viewModel.ProductViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertProductScreen(
    productoViewModel: ProductViewModel,
    navController: NavController
) {
    val context = LocalContext.current
    var codigo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaFab by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var disponibilidad by remember { mutableStateOf("") }
    
    // Obtenemos el estado de la imagen desde el ViewModel
    val imagenUriFromVM = productoViewModel.imagenUriState

    var showDialog by remember { mutableStateOf(false) }
    var capturedFile by remember { mutableStateOf<File?>(null) }

    val focusManager = LocalFocusManager.current
    val frDescripcion = remember { FocusRequester() }
    val frCosto = remember { FocusRequester() }
    val frDispon = remember { FocusRequester() }

    // Launcher para tomar la foto
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && capturedFile != null) {
            // Inicia la simulación de subida
            productoViewModel.uploadImage(capturedFile!!)
        }
    }

    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = CameraUtils.createTempImageFile(context)
            capturedFile = file
            val uri = CameraUtils.getUriForFile(context, file)
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
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

                    // Sección de Imagen
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imagenUriFromVM.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(imagenUriFromVM),
                                contentDescription = "Imagen del producto",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                        val file = CameraUtils.createTempImageFile(context)
                                        capturedFile = file
                                        val uri = CameraUtils.getUriForFile(context, file)
                                        cameraLauncher.launch(uri)
                                    } else {
                                        permissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier.size(80.dp)
                            ) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = "Tomar Foto", modifier = Modifier.size(48.dp))
                            }
                        }
                        
                        if (productoViewModel.isUploading) {
                            CircularProgressIndicator()
                        }
                    }

                    if (imagenUriFromVM.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                    val file = CameraUtils.createTempImageFile(context)
                                    capturedFile = file
                                    val uri = CameraUtils.getUriForFile(context, file)
                                    cameraLauncher.launch(uri)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Cambiar Foto")
                        }
                    }

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
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Number
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
                                    && !productoViewModel.isUploading
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
                    productoViewModel.insert(
                        Producto(
                            codigo = codigo.trim(),
                            descripcion = descripcion.trim(),
                            fecha_fab = fechaFab.trim(),
                            costo = costo.toDoubleOrNull() ?: 0.0,
                            disponibilidad = disponibilidad.toIntOrNull() ?: 0,
                            imagenUri = imagenUriFromVM, // Se usa la URI guardada en el VM
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
