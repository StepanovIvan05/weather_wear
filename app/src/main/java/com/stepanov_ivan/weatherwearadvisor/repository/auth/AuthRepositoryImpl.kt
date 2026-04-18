package com.stepanov_ivan.weatherwearadvisor.repository.auth

import com.stepanov_ivan.weatherwearadvisor.common.security.EncryptedPreferencesManager
import com.stepanov_ivan.weatherwearadvisor.data.AuthManager
import com.stepanov_ivan.weatherwearadvisor.utils.Resource

/**
 * Реализация репозитория аутентификации с защитой данных.
 * Сохраняет userId в зашифрованном виде для проверки доступа.
 */
class AuthRepositoryImpl(
    private val authManager: AuthManager,
    private val encryptedPrefs: EncryptedPreferencesManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<Boolean> {
        return try {
            authManager.signIn(email, password)
            // Сохранить userId в зашифрованные preferences
            authManager.currentUser?.uid?.let { 
                encryptedPrefs.saveUserId(it)
                encryptedPrefs.saveUserEmail(email)
            }
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Ошибка входа")
        }
    }

    override suspend fun register(name: String, email: String, password: String): Resource<Boolean> {
        return try {
            authManager.register(name, email, password)
            // Сохранить userId в зашифрованные preferences
            authManager.currentUser?.uid?.let { 
                encryptedPrefs.saveUserId(it)
                encryptedPrefs.saveUserEmail(email)
            }
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
        encryptedPrefs.clearAll()
    }

    /**
     * Проверка: принадлежит ли данное itemId текущему пользователю (управление доступом)
     */
    fun verifyUserOwnership(itemUserId: String): Boolean {
        val currentUserId = getCurrentUserId() ?: return false
        return currentUserId == itemUserId
    }
}
