package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {
    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> = _registerResult

    fun register(name: String, email: String, pass: String, repeatPass: String) {
        if (name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && pass == repeatPass) {
            _registerResult.value = true
        } else {
            _registerResult.value = false
        }
    }
}