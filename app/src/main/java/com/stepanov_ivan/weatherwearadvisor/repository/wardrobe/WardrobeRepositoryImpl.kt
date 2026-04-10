package com.stepanov_ivan.weatherwearadvisor.repository.wardrobe

import com.stepanov_ivan.weatherwearadvisor.data.WardrobeDao
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem
import com.stepanov_ivan.weatherwearadvisor.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class WardrobeRepositoryImpl(
    private val wardrobeDao: WardrobeDao
) : WardrobeRepository {

    override fun getItems(userId: String): Flow<Resource<List<ClothingItem>>> =
        wardrobeDao.getItemsByUserId(userId)
            .map<List<ClothingItem>, Resource<List<ClothingItem>>> { Resource.Success(it) }
            .onStart { emit(Resource.Loading()) }
            .catch { emit(Resource.Error(it.message ?: "Unknown error")) }

    override suspend fun addItem(item: ClothingItem): Resource<Unit> {
        return try {
            wardrobeDao.insertItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add item")
        }
    }

    override suspend fun updateItem(item: ClothingItem): Resource<Unit> {
        return try {
            wardrobeDao.updateItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update item")
        }
    }

    override suspend fun deleteItem(item: ClothingItem): Resource<Unit> {
        return try {
            wardrobeDao.deleteItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete item")
        }
    }
}
