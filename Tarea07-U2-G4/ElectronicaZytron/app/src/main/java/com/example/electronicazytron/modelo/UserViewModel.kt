package com.example.electronicazytron.modelo

import android.util.Log
import androidx.lifecycle.ViewModel

class UsuarioViewModel : ViewModel() {

    private val repository = UsuarioRepository()

    fun insert(usuario: Usuario){
        repository.agregar(usuario)
    }
    fun validar(usuario: Usuario): Boolean{
        return repository.validar(usuario)
    }

}