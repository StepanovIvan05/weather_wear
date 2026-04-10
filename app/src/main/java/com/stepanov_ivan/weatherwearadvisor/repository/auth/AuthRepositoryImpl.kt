package com.stepanov_ivan.weatherwearadvisor.repository.auth

import com.stepanov_ivan.weatherwearadvisor.data.AuthManager
import com.stepanov_ivan.weatherwearadvisor.utils.Resource

class AuthRepositoryImpl(
    private val authManager: AuthManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<Boolean> {
        return try {
            authManager.signIn(email, password)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Ошибка входа")
        }
    }

    override suspend fun register(name: String, email: String, password: String): Resource<Boolean> {
        return try {
            authManager.register(name, email, password)
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Ошибка регистрации")
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return authManager.isUserLoggedIn()
    }

    override fun getCurrentUserId(): String? {
        return authManager.currentUser?.uid
    }

    override fun getCurrentUserName(): String? {
        return authManager.currentUser?.displayName
    }

    override fun getCurrentUserEmail(): String? {
        return authManager.currentUser?.email
    }

    override fun logout() {
        authManager.signOut()
    }
}
