package com.example.electronicazytron.modelo

import android.util.Log

class UsuarioRepository {

    //Al momento de llamar la clase se verifica en la lista creada
    private var usuarios = mutableListOf<Usuario>(
        Usuario("Byron","Condolo"),
        Usuario("Pamela","Fernandez"),
        Usuario("Marielena","Gonzalez"),
        Usuario("Angelo","Lascano"),
        Usuario("Ruth","Rosero"),
        Usuario("Joan","Santamaria"),
        Usuario("Dennis","Trujillo"),
        )
    //funcion para agregar nuevos usuarios a la lista
    fun agregar(usuarioAppend: Usuario){
        usuarios.add(usuarioAppend)
    }
    //funcion para validar el usuario el cual recibe como parametro un usuario
    fun validar(usuarioVal: Usuario): Boolean{
        for (Usuario in usuarios){
            if (Usuario.nombre==usuarioVal.nombre && Usuario.apellido==usuarioVal.apellido){
                return true
            }
        }
        return false
    }

    fun listar(): List<Usuario>{
        return  usuarios;
    }



}