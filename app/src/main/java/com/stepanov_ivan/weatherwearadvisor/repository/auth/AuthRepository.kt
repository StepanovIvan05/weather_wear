package com.stepanov_ivan.weatherwearadvisor.repository.auth

import com.stepanov_ivan.weatherwearadvisor.utils.Resource

interface AuthRepository {
    suspend fun login(email: String, password: String): Resource<Boolean>
    suspend fun register(name: String, email: String, password: String): Resource<Boolean>
    fun isUserLoggedIn(): Boolean
    fun logout()
}
