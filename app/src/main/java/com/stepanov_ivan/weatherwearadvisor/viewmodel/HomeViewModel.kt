package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepanov_ivan.weatherwearadvisor.di.AppContainer
import com.stepanov_ivan.weatherwearadvisor.model.WeatherData
import kotlinx.coroutines.launch

sealed class WeatherState {
    object Loading : WeatherState()
    data class Success(val weatherData: WeatherData) : WeatherState()
    data class Error(val message: String) : WeatherState()
}

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

    private val _weatherState = MutableLiveData<WeatherState>()
    val weatherState: LiveData<WeatherState> = _weatherState

    private val _navigateTo = MutableLiveData<Int?>()
    val navigateTo: LiveData<Int?> = _navigateTo

    private val weatherRepository = AppContainer.provideWeatherRepository()
    private val locationRepository = AppContainer.locationRepository

    init {
        loadData()
        loadWeather()
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

    private fun loadWeather() {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading

            try {
                // Получаем погоду для Москвы (можно заменить на текущий город пользователя)
                val selectedCity = locationRepository.getSelectedCity()
                val lat = selectedCity?.latitude
                val lon = selectedCity?.longitude
                val result = if (lat != null && lon != null) {
                    weatherRepository.getWeatherByCoordinates(lat, lon)
                } else {
                    weatherRepository.getWeatherByCity(selectedCity?.name ?: "Moscow")
                }

                result.onSuccess { weatherData ->
                    _weatherState.value = WeatherState.Success(weatherData)
                }.onFailure { exception ->
                    _weatherState.value = WeatherState.Error("Не удалось загрузить погоду: ${exception.message}")
                }
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error("Ошибка сети: ${e.localizedMessage}")
            }
        }
    }

    fun loadWeatherByLocation() {
        viewModelScope.launch {
            _weatherState.value = WeatherState.Loading

            try {
                // Сначала получаем текущее местоположение
                val locationResult = locationRepository.getCurrentLocation()

                locationResult.onSuccess { location ->
                    // Получаем погоду по координатам
                    val weatherResult = weatherRepository.getWeatherByCoordinates(
                        location.latitude,
                        location.longitude
                    )

                    weatherResult.onSuccess { weatherData ->
                        _weatherState.value = WeatherState.Success(weatherData)
                    }.onFailure { exception ->
                        _weatherState.value = WeatherState.Error("Не удалось загрузить погоду: ${exception.message}")
                    }
                }.onFailure { exception ->
                    _weatherState.value = WeatherState.Error("Не удалось определить местоположение: ${exception.message}")
                }
            } catch (e: Exception) {
                _weatherState.value = WeatherState.Error("Ошибка: ${e.localizedMessage}")
            }
        }
    }

    fun retryLoadWeather() {
        loadWeather()
    }

    fun onNavigationClick(itemId: Int) {
        _navigateTo.value = itemId
    }

    fun onNavigationHandled() {
        _navigateTo.value = null
    }
}
