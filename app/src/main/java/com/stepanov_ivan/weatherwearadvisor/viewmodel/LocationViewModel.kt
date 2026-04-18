package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepanov_ivan.weatherwearadvisor.model.City
import com.stepanov_ivan.weatherwearadvisor.repository.location.LocationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LocationViewModel(
    private val repository: LocationRepository
) : ViewModel() {

    private val _cities = MutableLiveData<List<City>>()
    val cities: LiveData<List<City>> = _cities

    private val _selectedCity = MutableLiveData<City?>()
    val selectedCity: LiveData<City?> = _selectedCity

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _statusMessage = MutableLiveData<String?>(null)
    val statusMessage: LiveData<String?> = _statusMessage

    private val _isMapMode = MutableLiveData(false)
    val isMapMode: LiveData<Boolean> = _isMapMode

    private var searchJob: Job? = null

    init {
        _selectedCity.value = repository.getSelectedCity()
        _cities.value = repository.getCities()
    }

    fun switchMode(isMap: Boolean) {
        _isMapMode.value = isMap
    }

    fun onSearchQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            searchCities(query)
        }
    }

    fun searchCities(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.searchCities(query)
                .onSuccess { cities ->
                    _cities.value = cities
                    _statusMessage.value = if (cities.isEmpty()) "No cities found for this query" else null
                }
                .onFailure { exception ->
                    _statusMessage.value = exception.message ?: "City search failed"
                }
            _isLoading.value = false
        }
    }

    fun selectCity(city: City) {
        repository.saveSelectedCity(city)
        _selectedCity.value = city.copy(isActive = true)
        _cities.value = _cities.value?.map {
            it.copy(isActive = it.name == city.name && it.countryCode == city.countryCode)
        }
        _statusMessage.value = "Selected city: ${city.name}"
    }

    fun detectCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCurrentLocation()
                .onSuccess { location ->
                    repository.findNearestCity(location.latitude, location.longitude)
                        .onSuccess { city -> selectCity(city) }
                        .onFailure { exception ->
                            _statusMessage.value = exception.message ?: "Could not find nearest city"
                        }
                }
                .onFailure { exception ->
                    _statusMessage.value = exception.message ?: "Could not get current location"
                }
            _isLoading.value = false
        }
    }

    fun selectLocationOnMap(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.findNearestCity(latitude, longitude)
                .onSuccess { city -> selectCity(city) }
                .onFailure { exception ->
                    _statusMessage.value = exception.message ?: "Could not resolve map point"
                }
            _isLoading.value = false
        }
    }

    fun consumeStatusMessage() {
        _statusMessage.value = null
    }
}
