package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem
import com.stepanov_ivan.weatherwearadvisor.repository.wardrobe.WardrobeRepository
import com.stepanov_ivan.weatherwearadvisor.utils.Resource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class WardrobeViewModel(private val repository: WardrobeRepository) : ViewModel() {

    private val _itemsState = MutableStateFlow<Resource<List<ClothingItem>>>(Resource.Loading())
    val itemsState: StateFlow<Resource<List<ClothingItem>>> = _itemsState.asStateFlow()

    private val _operationResult = MutableStateFlow<Resource<Unit>?>(null)
    val operationResult: StateFlow<Resource<Unit>?> = _operationResult.asStateFlow()

    fun getItems(userId: String) {
        viewModelScope.launch {
            repository.getItems(userId).collect {
                _itemsState.value = it
            }
        }
    }

    fun addItem(name: String, photoUri: String, minTemp: Int, maxTemp: Int, userId: String) {
        if (name.isBlank() || photoUri.isBlank()) {
            _operationResult.value = Resource.Error("Name and photo are required")
            return
        }

        viewModelScope.launch {
            _operationResult.value = Resource.Loading()
            val newItem = ClothingItem(
                name = name,
                photoUri = photoUri,
                minTemp = minTemp,
                maxTemp = maxTemp,
                userId = userId
            )
            _operationResult.value = repository.addItem(newItem)
        }
    }

    fun updateItem(item: ClothingItem) {
        viewModelScope.launch {
            _operationResult.value = Resource.Loading()
            _operationResult.value = repository.updateItem(item)
        }
    }

    fun deleteItem(item: ClothingItem) {
        viewModelScope.launch {
            _operationResult.value = Resource.Loading()
            _operationResult.value = repository.deleteItem(item)
        }
    }
}
