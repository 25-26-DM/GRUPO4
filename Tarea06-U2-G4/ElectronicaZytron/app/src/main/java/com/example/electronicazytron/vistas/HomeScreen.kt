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

//Pantalla principal de la aplicacion, aqui se ubican
//Logo: Personalizado para la app
//Nombre de la organizacion
//Boton de login: para que el usuario inicie session
//Boton de Registo: Usado para que el usuario se registre y pueda acceder a la app
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable //metodo usado para utilizar los metodos de jetpackcompose
fun HomeScreen(onLogin: () -> Unit,
               onRegistrar: () -> Unit) {//como parametros de ingreso a home screen pasamos las dos funciones registradas en la appnavigator
                                            // se sigue con unit para inicializar, se usa onlogin para ingresar al metodo que redirige al login, y on register para
                                            // ingresar a la vista de Registro
    Scaffold { //metododo para seccionar el componente en boddy header etc
        BodyContent(onLogin,onRegistrar)  //contenido a graficar en la pantalla, los botones, imagenes, textos, etc
    }
}

@Composable
fun BodyContent(onLogin: () -> Unit,
                onRegistrar: () -> Unit) {
    //colum es usado para poner los elementos en columna, es posible usar columna y row dependiendo de lo que requiere la app
    //dentro del colum se puede ingresar los diferentes elementos, text, list, box, image, Textshow(Para inputs)
    //ademas se puede agregar a los elementos la mejora visual, recordar siempre usar modifier = Modifier.Atributo a dar al objeto
    //de igual manera es posible agregar mas elementos de modificacion visual segidos de la coma
    Column(
        modifier = Modifier
            .fillMaxSize() //Hace que la columna ocupe todo el espacio posible, usado para tener control sobre la columna
            .padding(top = 150.dp), // espacio entre el texto/elementos y el borde de la columna
        horizontalAlignment = Alignment.CenterHorizontally //centra horizontalmente los objetos
    ) {
        Spacer(modifier = Modifier.height(20.dp)) //produce un espacio entre dos elementos, en este caso entre el borde y la imagen
        Image(
            painter = painterResource(R.drawable.zytron),
            contentDescription = "Logo"
        )//image es un metodo de jetpackcompose usado para introducir imagenes, donde como requerimientos son painter y el painter Resourse que es la ruta de la imagen
        //a ingresar, de preferencia que se encuentren en la carpeta drawable
        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "ZytronCompany") //Text usado para representar cadenas de texto, va acomañado de text: y entre comillas el mensaje
                                        //en caso de querer usar variables externas en medio del texto usar ${Atributo}
        Spacer(modifier = Modifier.height(20.dp))
        //Button: elemento para representar un boton: tiene como atributos onClick el cual contiene el metodo a ejecutar al ser pulsado el boton
        //en este caso es redirigir a la pantalla de iniciar sesion, ademas se agrega el texto que tendra el boton con la etiqueta Text("TextoAIngresar")
        Button(onClick = { onLogin() }) {
            Text("Iniciar Sesión")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { onRegistrar()}) {
            Text("Registrar")
        }
    }
}

@Preview(showSystemUi = true) //metodo para usar el modo preview dentro del proyecto evitando tener que compilar el proyecto a todo momento
@Composable
fun DefaultPreview4() {
    HomeScreen(onLogin = {}, onRegistrar = {}) //representacion de la pantalla home para poder observar como puede quedar en la app
}
