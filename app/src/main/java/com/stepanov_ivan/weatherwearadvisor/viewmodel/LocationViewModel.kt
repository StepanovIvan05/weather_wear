package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stepanov_ivan.weatherwearadvisor.model.City
import com.stepanov_ivan.weatherwearadvisor.repository.location.LocationRepository

class LocationViewModel(
    private val repository: LocationRepository
) : ViewModel() {
    private val _cities = MutableLiveData<List<City>>()
    val cities: LiveData<List<City>> = _cities

    init {
        _cities.value = repository.getCities().ifEmpty { listOf(
            City("Москва", "Россия", true),
            City("Санкт-Петербург", "Россия", false),
            City("Сочи", "Россия", false)
        ) }
    }
}
