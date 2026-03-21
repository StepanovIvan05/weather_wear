package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    private val _loginResult = MutableLiveData<Boolean>()
    val loginResult: LiveData<Boolean> = _loginResult

    fun login(email: String, pass: String) {
        // Simple logic for now
        if (email.isNotEmpty() && pass.isNotEmpty()) {
            _loginResult.value = true
        } else {
            _loginResult.value = false
        }
    }
}