package com.example.cameraxapp.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userDao: UserDao) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.LoggedOut)
    val authState: StateFlow<AuthState> = _authState

    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.InvalidInput
            return
        }

        viewModelScope.launch {
            val user = userDao.getUserByUsername(username)
            if (user == null) {
                val passwordHash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                userDao.insert(User(username = username, passwordHash = passwordHash))
                _authState.value = AuthState.LoggedIn
            } else {
                _authState.value = AuthState.UserAlreadyExists
            }
        }
    }

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _authState.value = AuthState.InvalidInput
            return
        }

        viewModelScope.launch {
            val user = userDao.getUserByUsername(username)
            if (user != null) {
                val result = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash)
                if (result.verified) {
                    _authState.value = AuthState.LoggedIn
                } else {
                    _authState.value = AuthState.WrongCredentials
                }
            } else {
                _authState.value = AuthState.WrongCredentials
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.LoggedOut
    }
}

sealed class AuthState {
    object LoggedIn : AuthState()
    object LoggedOut : AuthState()
    object InvalidInput : AuthState()
    object WrongCredentials : AuthState()
    object UserAlreadyExists : AuthState()
}