package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepanov_ivan.weatherwearadvisor.repository.auth.AuthRepository
import com.stepanov_ivan.weatherwearadvisor.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<Boolean>?>(null)
    val authState: StateFlow<Resource<Boolean>?> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Поля не могут быть пустыми")
            return
        }

        viewModelScope.launch {
            _authState.value = Resource.Loading()
            _authState.value = repository.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String, confirmPass: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = Resource.Error("Поля не могут быть пустыми")
            return
        }
        if (password != confirmPass) {
            _authState.value = Resource.Error("Пароли не совпадают")
            return
        }
        if (password.length < 6) {
            _authState.value = Resource.Error("Пароль должен быть не менее 6 символов")
            return
        }

        viewModelScope.launch {
            _authState.value = Resource.Loading()
            _authState.value = repository.register(name, email, password)
        }
    }

    fun resetState() {
        _authState.value = null
    }
}
