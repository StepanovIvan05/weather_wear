package com.stepanov_ivan.weatherwearadvisor.repository.wardrobe

import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem
import com.stepanov_ivan.weatherwearadvisor.utils.Resource
import kotlinx.coroutines.flow.Flow

interface WardrobeRepository {
    fun getItems(userId: String): Flow<Resource<List<ClothingItem>>>
    suspend fun addItem(item: ClothingItem): Resource<Unit>
    suspend fun updateItem(item: ClothingItem): Resource<Unit>
    suspend fun deleteItem(item: ClothingItem): Resource<Unit>
}
