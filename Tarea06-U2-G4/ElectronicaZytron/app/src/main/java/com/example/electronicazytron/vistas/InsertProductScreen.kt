package com.example.electronicazytron.vista

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
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
fun InsertProductScreen(
    productoViewModel: ProductoViewModel,
    navController: NavController
) {
    var codigo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fechaFab by remember { mutableStateOf("") } // yyyyMMdd
    var costo by remember { mutableStateOf("") }
    var disponibilidad by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
                    text = "Ingresar Producto",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = codigo,
                    onValueChange = { codigo = it },
                    label = { Text("Código") },
                    leadingIcon = {
                        Icon(Icons.Default.QrCode, null)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                OutlinedTextField(
                    value = fechaFab,
                    onValueChange = { newValue ->
                        val digits = newValue.filter { it.isDigit() }
                        if (digits.length <= 8) fechaFab = digits
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
                    label = { Text("Disponibilidad") },
                    leadingIcon = {
                        Icon(Icons.Default.Inventory, null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
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

                        navController.navigate("productos") {
                            popUpTo("insertProduct") { inclusive = true }
                        }
                    }
                ) {
                    Text("Guardar Producto")
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun InsertProductScreenPreview() {
    val navController = rememberNavController()
    val viewModel = ProductoViewModel()

    MaterialTheme {
        InsertProductScreen(viewModel, navController)
    }
}