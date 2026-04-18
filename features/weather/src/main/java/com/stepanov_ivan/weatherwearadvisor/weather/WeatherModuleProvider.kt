package com.stepanov_ivan.weatherwearadvisor.weather

import android.content.Context
import android.util.Log
import com.stepanov_ivan.weatherwearadvisor.weather.api.OpenWeatherMapApi
import com.stepanov_ivan.weatherwearadvisor.weather.repository.WeatherRepository
import com.stepanov_ivan.weatherwearadvisor.weather.repository.WeatherRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Factory для создания WeatherRepository
 * Позволяет другим модулям использовать Weather функциональность
 */
object WeatherModuleProvider {
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val TAG = "WeatherModule"
    private var weatherRepository: WeatherRepository? = null

    /**
     * Инициализация модуля с API ключом
     */
    fun initialize(apiKey: String) {
        try {
            // Валидируем API ключ
            require(apiKey.isNotBlank()) { "API key cannot be blank" }
            require(!apiKey.contains("YOUR_API_KEY")) { "API key is not configured. Replace 'YOUR_API_KEY_HERE' with your real OpenWeatherMap API key" }
            
            Log.d(TAG, "Using API key: ${apiKey.take(5)}...${apiKey.takeLast(5)}")

            // Конфигурируем OkHttpClient с расширенными таймаутами
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(15, TimeUnit.SECONDS)      // Время подключения
                .readTimeout(20, TimeUnit.SECONDS)         // Время ожидания ответа
                .writeTimeout(20, TimeUnit.SECONDS)        // Время отправки запроса
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient)
                .build()

            val api = retrofit.create(OpenWeatherMapApi::class.java)
            weatherRepository = WeatherRepositoryImpl(api, apiKey)
            Log.d(TAG, "WeatherModule initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WeatherModule: ${e.message}", e)
            throw e
        }
    }

    /**
     * Получить репозиторий для использования в других модулях
     */
    fun getWeatherRepository(): WeatherRepository {
        return weatherRepository 
            ?: throw IllegalStateException("WeatherModule not initialized. Call initialize(apiKey) first")
    }
}
