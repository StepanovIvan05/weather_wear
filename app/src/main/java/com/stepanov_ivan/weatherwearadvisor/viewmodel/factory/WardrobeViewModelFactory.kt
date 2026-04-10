package com.stepanov_ivan.weatherwearadvisor.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.stepanov_ivan.weatherwearadvisor.repository.wardrobe.WardrobeRepository
import com.stepanov_ivan.weatherwearadvisor.viewmodel.WardrobeViewModel

class WardrobeViewModelFactory(
    private val repository: WardrobeRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WardrobeViewModel::class.java)) {
            return WardrobeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
