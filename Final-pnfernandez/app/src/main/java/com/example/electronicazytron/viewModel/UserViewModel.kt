package com.example.electronicazytron.viewModel // O tu paquete correspondiente

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.electronicazytron.auth.AppDatabase
import com.example.electronicazytron.model.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = UserRepository(db.userDao())
    }

    fun registrar(nombre: String, apellido: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.registrar(nombre, apellido, password)
        }
    }

    // LOGIN ASÍNCRONO: Recibe un callback (onResult) para avisar a la pantalla si fue exitoso
    fun validar(nombre: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val esValido = repository.login(nombre, password)
            // Volvemos al hilo principal para responder a la UI
            withContext(Dispatchers.Main) {
                onResult(esValido)
            }
        }
    }
}