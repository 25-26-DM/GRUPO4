package com.example.electronicazytron.modelo

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
//clase intermedia entre la vista y la entidad usada para representar los diferentes metodos que se pueden usar
// viewModel() Un ViewModel es una clase donde manejas logica de negocio y estado de tu UI, separado de la UI misma.
class ProductoViewModel : ViewModel() {
    //inicializacion del repositorio de producto para acceder a sus metodos, envuelta en private para que no sea accedido
    private val repository = ProductoRepository()

    // Lista observable para la UI, es aquella lista que se muestra en la vista
    var productos = mutableStateListOf<Producto>()
        private set

    // Cargar productos desde el repositorio usando el metodo listar, ademas se encarga de refrescar la vista cuando detecta cambios
    fun cargarProductos() {
        productos.clear()
        productos.addAll(repository.listar())
    }

    fun find(codigo: String): Producto? {
        return repository.find(codigo)
    }

    // Actualizar un producto en el repositorio
    fun update(codigo: String, producto: Producto) {
        repository.update(codigo, producto)
        cargarProductos() // refrescar la lista observable
    }

    fun delete(codigo: String){
        repository.delete(codigo)
        cargarProductos()
    }

    fun insert(producto: Producto){
        repository.agregar(producto)
        cargarProductos()
    }
    //aqui se puede agregar la funcionalidad que se desea implementar en la vista o nuevos metodos que puedan complementar a la vista
}
