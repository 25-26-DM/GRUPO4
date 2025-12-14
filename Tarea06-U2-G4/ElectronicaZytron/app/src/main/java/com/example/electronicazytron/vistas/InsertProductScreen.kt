package com.example.electronicazytron.vista

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.electronicazytron.modelo.Producto
import com.example.electronicazytron.modelo.ProductoViewModel

/* -------------------------
   VisualTransformation Fecha
-------------------------- */
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertProductScreen(
    productoViewModel: ProductoViewModel,
    navController: NavController
) {
    var codigo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaFab by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var disponibilidad by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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

                    Text(
                        text = "Datos del producto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it },
                        label = { Text("Código") },
                        leadingIcon = { Icon(Icons.Default.QrCode, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = fechaFab,
                        onValueChange = {
                            val digits = it.filter { c -> c.isDigit() }
                            if (digits.length <= 8) fechaFab = digits
                        },
                        label = { Text("Fecha fabricación (yyyy-MM-dd)") },
                        leadingIcon = { Icon(Icons.Default.DateRange, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = DateVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

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

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Cancelar")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { showDialog = true },
                            enabled = codigo.isNotBlank() && descripcion.isNotBlank()
                        ) {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }

    /* -------------------------
       Diálogo de confirmación
    -------------------------- */
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val formattedDate =
                            DateVisualTransformation()
                                .filter(AnnotatedString(fechaFab))
                                .text
                                .toString()

                        productoViewModel.insert(
                            Producto(
                                codigo = codigo,
                                descripcion = descripcion,
                                fecha_fab = formattedDate,
                                costo = costo.toDoubleOrNull() ?: 0.0,
                                disponibilidad = disponibilidad.toIntOrNull() ?: 0
                            )
                        )

                        showDialog = false
                        navController.navigate("productos") {
                            popUpTo("insertProduct") { inclusive = true }
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            },
            title = { Text("Confirmar producto") },
            text = { Text("¿Deseas guardar este producto?") }
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun InsertProductScreenPreview() {
    InsertProductScreen(
        productoViewModel = ProductoViewModel(),
        navController = rememberNavController()
    )
}
