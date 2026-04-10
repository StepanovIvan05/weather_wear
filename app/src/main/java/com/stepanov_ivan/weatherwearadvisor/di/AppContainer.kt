package com.stepanov_ivan.weatherwearadvisor.di

import android.content.Context
import com.stepanov_ivan.weatherwearadvisor.data.AppDatabase
import com.stepanov_ivan.weatherwearadvisor.data.AuthManager
import com.stepanov_ivan.weatherwearadvisor.repository.auth.AuthRepository
import com.stepanov_ivan.weatherwearadvisor.repository.auth.AuthRepositoryImpl
import com.stepanov_ivan.weatherwearadvisor.repository.location.LocationRepository
import com.stepanov_ivan.weatherwearadvisor.repository.location.StaticLocationRepository
import com.stepanov_ivan.weatherwearadvisor.repository.wardrobe.WardrobeRepository
import com.stepanov_ivan.weatherwearadvisor.repository.wardrobe.WardrobeRepositoryImpl

object AppContainer {
    private val authManager by lazy { AuthManager() }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authManager)
    }

    val locationRepository: LocationRepository by lazy {
        StaticLocationRepository()
    }

    fun provideWardrobeRepository(context: Context): WardrobeRepository {
        return WardrobeRepositoryImpl(
            AppDatabase.getDatabase(context.applicationContext).wardrobeDao()
        )
    }
}
