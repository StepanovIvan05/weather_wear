package com.stepanov_ivan.weatherwearadvisor.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stepanov_ivan.weatherwearadvisor.repository.location.LocationRepository
import com.stepanov_ivan.weatherwearadvisor.viewmodel.LocationViewModel

class LocationViewModelFactory(
    private val repository: LocationRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
