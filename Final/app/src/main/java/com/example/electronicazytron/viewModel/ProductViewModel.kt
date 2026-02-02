package com.example.electronicazytron.viewModel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.electronicazytron.model.entities.Producto
import com.example.electronicazytron.model.repository.ProductoRepository
import com.example.electronicazytron.auth.AppDatabase
import kotlinx.coroutines.launch

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductoRepository(
        AppDatabase.getDatabase(application).productoDao()
    )
    var productos = mutableStateListOf<Producto>()
        private set

    init{
        viewModelScope.launch {
            repository.seedIfEmpty()
            cargarProductos()
        }
    }

    private suspend fun refrescar(){
        productos.clear()
        productos.addAll(repository.listar())
    }
     fun cargarProductos() {
        viewModelScope.launch {
            refrescar()
        }
     }

    fun find(codigo: String, onResult: (Producto?) -> Unit) {
        viewModelScope.launch {
            onResult(repository.find(codigo))
        }
    }
    fun update(producto: Producto) {
        viewModelScope.launch {
            repository.update(producto)
            refrescar()
        }
    }

    fun deleteVisual(codigo: String) {
        viewModelScope.launch {
            repository.softDelete(codigo)
            refrescar()
        }
    }
    fun insert(producto: Producto) {
        viewModelScope.launch {
            repository.agregar(producto)
            refrescar()
        }
    }
}
