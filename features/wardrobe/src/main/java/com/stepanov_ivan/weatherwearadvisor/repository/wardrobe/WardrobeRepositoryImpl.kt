package com.stepanov_ivan.weatherwearadvisor.repository.wardrobe

import com.stepanov_ivan.weatherwearadvisor.data.WardrobeDao
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem
import com.stepanov_ivan.weatherwearadvisor.repository.auth.AuthRepository
import com.stepanov_ivan.weatherwearadvisor.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class WardrobeRepositoryImpl(
    private val wardrobeDao: WardrobeDao,
    private val authRepository: AuthRepository? = null
) : WardrobeRepository {

    override fun getItems(userId: String): Flow<Resource<List<ClothingItem>>> {
        val currentUserId = authRepository?.getCurrentUserId()

        return if (currentUserId != null && currentUserId == userId) {
            wardrobeDao.getItemsByUserId(userId)
                .map<List<ClothingItem>, Resource<List<ClothingItem>>> { Resource.Success(it) }
                .onStart { emit(Resource.Loading()) }
                .catch { emit(Resource.Error(it.message ?: "Unknown error")) }
        } else {
            flowOf(Resource.Error("Доступ запрещён: попытка доступа к чужим данным"))
        }
    }

    override suspend fun addItem(item: ClothingItem): Resource<Unit> {
        val currentUserId = authRepository?.getCurrentUserId()
        if (currentUserId == null || currentUserId != item.userId) {
            return Resource.Error("Доступ запрещён: невозможно добавить элемент в чужой гардероб")
        }

        return try {
            wardrobeDao.insertItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add item")
        }
    }

    override suspend fun updateItem(item: ClothingItem): Resource<Unit> {
        val currentUserId = authRepository?.getCurrentUserId()
        if (currentUserId == null || currentUserId != item.userId) {
            return Resource.Error("Доступ запрещён: невозможно изменить элемент в чужом гардеробе")
        }

        return try {
            wardrobeDao.updateItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update item")
        }
    }

    override suspend fun deleteItem(item: ClothingItem): Resource<Unit> {
        val currentUserId = authRepository?.getCurrentUserId()
        if (currentUserId == null || currentUserId != item.userId) {
            return Resource.Error("Доступ запрещён: невозможно удалить элемент из чужого гардероба")
        }

        return try {
            wardrobeDao.deleteItem(item)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete item")
        }
    }
}
