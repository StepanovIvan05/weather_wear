package com.stepanov_ivan.weatherwearadvisor.weather

import android.util.Log
import com.stepanov_ivan.weatherwearadvisor.data.WeatherCacheDao
import com.stepanov_ivan.weatherwearadvisor.network.NetworkClientFactory
import com.stepanov_ivan.weatherwearadvisor.weather.api.OpenWeatherMapApi
import com.stepanov_ivan.weatherwearadvisor.weather.repository.WeatherRepository
import com.stepanov_ivan.weatherwearadvisor.weather.repository.WeatherRepositoryImpl

object WeatherModuleProvider {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val TAG = "WeatherModule"
    private var weatherRepository: WeatherRepository? = null

    fun initialize(
        apiKey: String,
        weatherCacheDao: WeatherCacheDao? = null,
        baseUrl: String = BASE_URL
    ) {
        try {
            require(apiKey.isNotBlank()) { "API key cannot be blank" }
            require(!apiKey.contains("YOUR_API_KEY")) {
                "API key is not configured. Replace 'YOUR_API_KEY_HERE' with your real OpenWeatherMap API key"
            }

            Log.d(TAG, "Using API key: ${apiKey.take(5)}...${apiKey.takeLast(5)}")

            val retrofit = NetworkClientFactory.createRetrofit(baseUrl)
            val api = retrofit.create(OpenWeatherMapApi::class.java)
            weatherRepository = WeatherRepositoryImpl(
                api = api,
                apiKey = apiKey,
                weatherCacheDao = weatherCacheDao
            )
            Log.d(TAG, "WeatherModule initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WeatherModule: ${e.message}", e)
            throw e
        }
    }

    fun getWeatherRepository(): WeatherRepository {
        return weatherRepository
            ?: throw IllegalStateException("WeatherModule not initialized. Call initialize(apiKey) first")
    }
}
