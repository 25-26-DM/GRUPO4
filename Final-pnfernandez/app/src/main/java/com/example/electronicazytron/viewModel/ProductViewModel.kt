package com.example.electronicazytron.viewModel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.electronicazytron.model.entities.Producto
import com.example.electronicazytron.model.repository.ProductoRepository
import com.example.electronicazytron.auth.AppDatabase
import com.example.electronicazytron.utils.CameraUtils
import android.content.Intent
import com.example.electronicazytron.services.SyncService
import kotlinx.coroutines.launch
import java.io.File

class ProductViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ProductoRepository(
        AppDatabase.getDatabase(application).productoDao()
    )

    var productos = mutableStateListOf<Producto>()
        private set

    // Tarea 9: Estado para la imagen capturada en el formulario
    var imagenUriState by mutableStateOf("")
        private set

    // Tarea 4: Estado de carga para feedback visual en la UI
    var isUploading by mutableStateOf(false)
        private set

    fun updateImagenUri(uri: String) {
        imagenUriState = uri
    }

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
            cargarProductos()
        }
    }

    private suspend fun refrescar() {
        productos.clear()
        productos.addAll(repository.listar())
    }

    fun cargarProductos() {
        viewModelScope.launch {
            refrescar()
        }
    }

    /**
     * Tarea 4: Procesa la foto capturada.
     * Priorizamos la ruta local para que la imagen sea visible en la App inmediatamente.
     */
    fun uploadImage(file: File) {
        viewModelScope.launch {
            isUploading = true
            try {
                // Mostrar inmediatamente la ruta local para previsualizar mientras sube
                updateImagenUri("file://${file.absolutePath}")

                // Intentamos subir a Cloudinary (si CloudinaryConfig está configurado)
                val uploadedUrl = try {
                    CameraUtils.uploadImageToCloudinary(file)
                } catch (e: Exception) {
                    // Si falla la subida remota, usamos el fallback local
                    CameraUtils.uploadImageSimulated(file)
                }

                // Reemplazamos la URI local por la URL remota (si corresponde)
                updateImagenUri(uploadedUrl)

            } catch (e: Exception) {
                // Fallback en caso de error
                updateImagenUri(file.absolutePath)
            } finally {
                isUploading = false
            }
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
            // Intentar disparar sincronización en segundo plano
            try {
                val intent = Intent(getApplication(), SyncService::class.java)
                getApplication<Application>().startService(intent)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun deleteVisual(codigo: String) {
        viewModelScope.launch {
            repository.softDelete(codigo)
            refrescar()
            try {
                val intent = Intent(getApplication(), SyncService::class.java)
                getApplication<Application>().startService(intent)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    /**
     * Modificado para asegurar que el producto guardado incluya la imagenUri actual.
     */
    fun insert(producto: Producto) {
        viewModelScope.launch {
            // Aseguramos que el producto lleve la URI que capturamos con la cámara
            val productoConImagen = producto.copy(imagenUri = imagenUriState)
            repository.agregar(productoConImagen)

            // Limpiamos el estado después de una inserción exitosa
            imagenUriState = ""
            refrescar()
            try {
                val intent = Intent(getApplication(), SyncService::class.java)
                getApplication<Application>().startService(intent)
            } catch (e: Exception) {
            }
        }
    }
}