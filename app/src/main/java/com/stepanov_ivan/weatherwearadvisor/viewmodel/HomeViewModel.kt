package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _weatherInfo = MutableLiveData<String>()
    val weatherInfo: LiveData<String> = _weatherInfo

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName

    init {
        _userName.value = "Алекс"
        _weatherInfo.value = "Облачно, 18°C"
    }
}