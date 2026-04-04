package com.stepanov_ivan.weatherwearadvisor.repository.wardrobe

import com.stepanov_ivan.weatherwearadvisor.data.WardrobeDao
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem
import com.stepanov_ivan.weatherwearadvisor.utils.Resource
import kotlinx.coroutines.flow.*

class WardrobeRepository(private val wardrobeDao: WardrobeDao) {

    fun getItems(userId: String): Flow<Resource<List<ClothingItem>>> = 
        wardrobeDao.getItemsByUserId(userId)
            .map { Resource.Success(it) as Resource<List<ClothingItem>> }
            .onStart { emit(Resource.Loading()) }
            .catch { emit(Resource.Error(it.message ?: "Unknown error")) }

    suspend fun addItem(item: ClothingItem): Resource<Unit> {
        return try {
            wardrobeDao.insertItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add item")
        }
    }

    suspend fun updateItem(item: ClothingItem): Resource<Unit> {
        return try {
            wardrobeDao.updateItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update item")
        }
    }

    suspend fun deleteItem(item: ClothingItem): Resource<Unit> {
        return try {
            wardrobeDao.deleteItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete item")
        }
    }
}
