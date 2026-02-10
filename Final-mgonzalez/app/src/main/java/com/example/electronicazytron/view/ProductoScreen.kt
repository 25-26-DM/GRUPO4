package com.example.electronicazytron.view
import coil.compose.AsyncImage
import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.electronicazytron.model.entities.Producto
import com.example.electronicazytron.viewModel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    productoViewModel: ProductViewModel = viewModel(),
    navController: NavController,
    nombreUsuario: String = "",
    password: String = "",
    hash: String = ""
) {
    val context = LocalContext.current

    // Request POST_NOTIFICATIONS permission on Android 13+ once
    var askedForNotifications by remember { mutableStateOf(false) }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        Log.i("ProductScreen", "POST_NOTIFICATIONS granted: $isGranted")
    }

    LaunchedEffect(Unit) {
        productoViewModel.cargarProductos()

        if (!askedForNotifications && android.os.Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                // launch permission prompt
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                askedForNotifications = true
            }
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                actions = {
                    IconButton(onClick = { navController.navigate("insertProduct") }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {

            if (nombreUsuario.isNotEmpty() || password.isNotEmpty() || hash.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sesión Activa",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (nombreUsuario.isNotEmpty()) {
                            OutlinedTextField(
                                value = nombreUsuario,
                                onValueChange = {},
                                label = { Text("Usuario") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (password.isNotEmpty()) {
                            Text("Contraseña: $password", style = MaterialTheme.typography.bodyMedium)
                        }
                        if (hash.isNotEmpty()) {
                            Text("Hash: $hash", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            ProductContent(
                productos = productoViewModel.productos,
                modifier = Modifier.fillMaxSize(),
                onUpdate = { navController.navigate("updateProduct/$it") },
                onDelete = { codigo -> productoViewModel.deleteVisual(codigo) } // ✅ SOLO 1 eliminar
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión", style = MaterialTheme.typography.titleLarge) },
            text = { Text("¿Estás seguro de que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        // Cerrar sesión y limpiar estado de LastLog mostrado
                        try {
                            com.example.electronicazytron.utils.SessionManager.cerrarSesion()
                        } catch (_: Exception) {}
                        try {
                            com.example.electronicazytron.utils.LastLogRepository.setFromDb(null)
                        } catch (_: Exception) {}

                        navController.navigate("login") {
                            popUpTo("productos") { inclusive = true }
                        }
                    }
                ) { Text("Sí, salir") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ProductContent(
    productos: List<Producto>,
    modifier: Modifier = Modifier,
    onUpdate: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val pageSize = 5
    val startIndex = currentPage * pageSize
    val endIndex = minOf(startIndex + pageSize, productos.size)

    val paginatedProducts =
        if (startIndex < endIndex) productos.subList(startIndex, endIndex) else emptyList()

    Column(modifier = modifier) {

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(paginatedProducts) { producto ->
                ProductCard(
                    producto = producto,
                    onUpdate = onUpdate,
                    onDelete = onDelete
                )
            }
        }

        PaginationControls(
            currentPage = currentPage,
            canGoNext = endIndex < productos.size,
            onPrevious = { currentPage-- },
            onNext = { currentPage++ }
        )
    }
}

@Composable
fun ProductCard(
    producto: Producto,
    onUpdate: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            AsyncImage(
                model = producto.imagenUri,
                contentDescription = "Imagen del producto",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = producto.descripcion,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text("Código: ${producto.codigo}")

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Costo: $${producto.costo}")
                Text("Stock: ${producto.disponibilidad}")
                Text("Fecha fabricación: ${producto.fecha_fab}")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { onUpdate(producto.codigo) }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar producto") },
            text = { Text("¿Deseas eliminar visualmente el producto ${producto.codigo}?") },
            confirmButton = {
                Button(onClick = {
                    onDelete(producto.codigo)
                    showDeleteDialog = false
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PaginationControls(
    currentPage: Int,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onPrevious, enabled = currentPage > 0) { Text("Anterior") }
        Spacer(modifier = Modifier.width(16.dp))
        Text("Página ${currentPage + 1}")
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = onNext, enabled = canGoNext) { Text("Siguiente") }
    }
}

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
fun ProductScreenPreview() {
    val productosFake = listOf(
        Producto("P001", "Laptop Lenovo", "2024-01-15", 750.0, 10),
        Producto("P002", "Mouse Logitech", "2023-11-20", 18.5, 45),
        Producto("P003", "Teclado Redragon", "2023-10-05", 50.0, 30)
    )

    MaterialTheme {
        ProductContent(
            productos = productosFake,
            onUpdate = {},
            onDelete = {}
        )
    }
}
