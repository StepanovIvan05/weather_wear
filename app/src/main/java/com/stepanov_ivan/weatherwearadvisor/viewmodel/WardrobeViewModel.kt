package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem
import com.stepanov_ivan.weatherwearadvisor.R

class WardrobeViewModel : ViewModel() {
    private val _items = MutableLiveData<List<ClothingItem>>()
    val items: LiveData<List<ClothingItem>> = _items

    init {
        _items.value = listOf(
            ClothingItem("Джинсовая куртка", "Levi's", R.drawable.outfit_placeholder),
            ClothingItem("Белые кеды", "Converse", R.drawable.outfit_placeholder),
            ClothingItem("Худи", "Nike", R.drawable.outfit_placeholder)
        )
    }
}