package com.example.electronicazytron.vistas

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.electronicazytron.R


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(onLogin: () -> Unit,
               onRegistrar: () -> Unit) {
    Scaffold {
        BodyContent(onLogin,onRegistrar)
    }
}

@Composable
fun BodyContent(onLogin: () -> Unit,
                onRegistrar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 150.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Image(
            painter = painterResource(R.drawable.zytron),
            contentDescription = "Logo"
        )
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "ZytronCompany")
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { onLogin() }) {
            Text("Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { onRegistrar()}) {
            Text("Registrar")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun DefaultPreview4() {
    HomeScreen(onLogin = {}, onRegistrar = {})
}
