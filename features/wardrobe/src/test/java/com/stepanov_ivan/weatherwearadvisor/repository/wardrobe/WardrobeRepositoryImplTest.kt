package com.stepanov_ivan.weatherwearadvisor.repository.wardrobe

import com.stepanov_ivan.weatherwearadvisor.data.WardrobeDao
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem
import com.stepanov_ivan.weatherwearadvisor.repository.auth.AuthRepository
import com.stepanov_ivan.weatherwearadvisor.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WardrobeRepositoryImplTest {
    @Test
    fun addItem_rejectsForeignUserItem() = runBlocking {
        val dao = FakeWardrobeDao()
        val repository = WardrobeRepositoryImpl(
            wardrobeDao = dao,
            authRepository = FakeAuthRepository(currentUserId = "current-user")
        )

        val result = repository.addItem(
            ClothingItem(
                name = "Coat",
                photoUri = "file://coat.jpg",
                minTemp = -10,
                maxTemp = 5,
                userId = "other-user"
            )
        )

        assertTrue(result is Resource.Error)
        assertEquals(0, dao.insertCalls)
    }

    @Test
    fun addItem_allowsCurrentUserItem() = runBlocking {
        val dao = FakeWardrobeDao()
        val repository = WardrobeRepositoryImpl(
            wardrobeDao = dao,
            authRepository = FakeAuthRepository(currentUserId = "current-user")
        )

        val result = repository.addItem(
            ClothingItem(
                name = "Coat",
                photoUri = "file://coat.jpg",
                minTemp = -10,
                maxTemp = 5,
                userId = "current-user"
            )
        )

        assertTrue(result is Resource.Success)
        assertEquals(1, dao.insertCalls)
    }

    private class FakeWardrobeDao : WardrobeDao {
        var insertCalls = 0

        override fun getItemsByUserId(userId: String): Flow<List<ClothingItem>> {
            return emptyFlow()
        }

        override suspend fun insertItem(item: ClothingItem) {
            insertCalls++
        }

        override suspend fun updateItem(item: ClothingItem) = Unit

        override suspend fun deleteItem(item: ClothingItem) = Unit
    }

    private class FakeAuthRepository(
        private val currentUserId: String?
    ) : AuthRepository {
        override suspend fun login(email: String, password: String): Resource<Boolean> = Resource.Success(true)
        override suspend fun register(name: String, email: String, password: String): Resource<Boolean> = Resource.Success(true)
        override fun isUserLoggedIn(): Boolean = currentUserId != null
        override fun getCurrentUserId(): String? = currentUserId
        override fun getCurrentUserName(): String? = null
        override fun getCurrentUserEmail(): String? = null
        override fun logout() = Unit
    }
}
