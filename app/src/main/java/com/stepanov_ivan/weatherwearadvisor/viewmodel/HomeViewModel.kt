package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class HomeState(
    val userName: String = "",
    val city: String = "",
    val temperature: String = "",
    val condition: String = "",
    val feelsLike: String = "",
    val windSpeed: String = "",
    val humidity: String = "",
    val wardrobeCount: String = "",
    val outfitsCount: String = ""
)

class HomeViewModel : ViewModel() {

    private val _state = MutableLiveData<HomeState>()
    val state: LiveData<HomeState> = _state

    private val _navigateTo = MutableLiveData<Int?>()
    val navigateTo: LiveData<Int?> = _navigateTo

    init {
        loadData()
    }

    private fun loadData() {
        // Имитация загрузки данных
        _state.value = HomeState(
            userName = "Алекс",
            city = "Москва",
            temperature = "18°",
            condition = "Облачно",
            feelsLike = "Ощущается как 16°",
            windSpeed = "💨 12 км/ч",
            humidity = "💧 45%",
            wardrobeCount = "42 вещи",
            outfitsCount = "8 образов"
        )
    }

    fun onNavigationClick(itemId: Int) {
        _navigateTo.value = itemId
    }

    fun onNavigationHandled() {
        _navigateTo.value = null
    }
}
