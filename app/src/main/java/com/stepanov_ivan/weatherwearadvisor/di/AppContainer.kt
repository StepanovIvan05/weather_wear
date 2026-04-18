package com.stepanov_ivan.weatherwearadvisor.di

import android.content.Context
import com.stepanov_ivan.weatherwearadvisor.common.di.CommonModuleProvider
import com.stepanov_ivan.weatherwearadvisor.common.security.EncryptedPreferencesManager
import com.stepanov_ivan.weatherwearadvisor.data.AppDatabase
import com.stepanov_ivan.weatherwearadvisor.data.AuthManager
import com.stepanov_ivan.weatherwearadvisor.repository.auth.AuthRepository
import com.stepanov_ivan.weatherwearadvisor.repository.auth.AuthRepositoryImpl
import com.stepanov_ivan.weatherwearadvisor.repository.location.LocationRepository
import com.stepanov_ivan.weatherwearadvisor.repository.location.StaticLocationRepository
import com.stepanov_ivan.weatherwearadvisor.repository.wardrobe.WardrobeRepository
import com.stepanov_ivan.weatherwearadvisor.repository.wardrobe.WardrobeRepositoryImpl
import com.stepanov_ivan.weatherwearadvisor.weather.WeatherModuleProvider
import com.stepanov_ivan.weatherwearadvisor.weather.repository.WeatherRepository

/**
 * DI контейнер для главного приложения.
 * Объединяет компоненты из:
 * - :core:common → Security, EncryptedPreferences
 * - :features:weather → Weather API
 * - :app (локально) → Auth, Wardrobe, Location
 */
object AppContainer {
    private val authManager by lazy { AuthManager() }

    fun init(context: Context) {
        // Уже инициализировано, но подтверждение для безопасности
        CommonModuleProvider.initialize(context)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            authManager,
            CommonModuleProvider.getEncryptedPreferencesManager()
        )
    }

    val locationRepository: LocationRepository by lazy {
        StaticLocationRepository()
    }

    fun provideWardrobeRepository(context: Context): WardrobeRepository {
        return WardrobeRepositoryImpl(
            wardrobeDao = AppDatabase.getDatabase(context.applicationContext).wardrobeDao(),
            authRepository = authRepository
        )
    }

    /**
     * Получить Weather Repository из модуля :features:weather
     */
    fun provideWeatherRepository(): WeatherRepository {
        return WeatherModuleProvider.getWeatherRepository()
    }
}
