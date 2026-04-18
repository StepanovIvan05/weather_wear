package com.stepanov_ivan.weatherwearadvisor.weather.repository

import android.util.Log
import com.stepanov_ivan.weatherwearadvisor.weather.api.OpenWeatherMapApi
import com.stepanov_ivan.weatherwearadvisor.weather.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Интерфейс репозитория для получения данных о погоде
 */
interface WeatherRepository {
    suspend fun getWeatherByCity(city: String): Result<WeatherData>
    suspend fun getWeatherByCoordinates(latitude: Double, longitude: Double): Result<WeatherData>
}

/**
 * Реализация репозитория с использованием OpenWeatherMap API
 */
class WeatherRepositoryImpl(
    private val api: OpenWeatherMapApi,
    private val apiKey: String
) : WeatherRepository {

    companion object {
        private const val TAG = "WeatherRepository"
    }

    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
    }

    override suspend fun getWeatherByCity(city: String): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching weather for city: $city")
                val response = api.getWeatherByCity(
                    city = city,
                    apiKey = apiKey
                )
                val weatherData = response.toWeatherData()
                Log.d(TAG, "Successfully loaded weather for $city: ${weatherData.temperature}°")
                Result.success(weatherData)
            } catch (e: retrofit2.HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "❌ API ключ некорректен. Проверьте app/build.gradle.kts → buildConfigField"
                    404 -> "❌ Город не найден: $city"
                    429 -> "⚠️ Слишком много запросов"
                    else -> "HTTP ${e.code()}: ${e.message()}"
                }
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            } catch (e: SocketTimeoutException) {
                val errorMsg = "Время ожидания ответа истекло. Проверьте интернет-соединение."
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            } catch (e: UnknownHostException) {
                val errorMsg = "Нет соединения с интернетом. Проверьте WiFi или мобильные данные."
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            } catch (e: IllegalArgumentException) {
                val errorMsg = "Некорректные данные от сервера погоды: ${e.message}"
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            } catch (e: Exception) {
                val errorMsg = "Ошибка при получении погоды: ${e.localizedMessage ?: e.message}"
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            }
        }
    }

    override suspend fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Fetching weather for coordinates: $latitude, $longitude")
                val response = api.getWeatherByCoordinates(
                    latitude = latitude,
                    longitude = longitude,
                    apiKey = apiKey
                )
                val weatherData = response.toWeatherData()
                Log.d(TAG, "Successfully loaded weather: ${weatherData.temperature}°")
                Result.success(weatherData)
            } catch (e: retrofit2.HttpException) {
                val errorMsg = when (e.code()) {
                    401 -> "❌ API ключ некорректен. Проверьте app/build.gradle.kts → buildConfigField"
                    404 -> "❌ Координаты не найдены"
                    429 -> "⚠️ Слишком много запросов"
                    else -> "HTTP ${e.code()}: ${e.message()}"
                }
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            } catch (e: SocketTimeoutException) {
                val errorMsg = "Время ожидания ответа истекло. Проверьте интернет-соединение."
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            } catch (e: UnknownHostException) {
                val errorMsg = "Нет соединения с интернетом. Проверьте WiFi или мобильные данные."
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            } catch (e: IllegalArgumentException) {
                val errorMsg = "Некорректные данные от сервера погоды: ${e.message}"
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            } catch (e: Exception) {
                val errorMsg = "Ошибка при получении погоды: ${e.localizedMessage ?: e.message}"
                Log.e(TAG, errorMsg, e)
                Result.failure(Exception(errorMsg))
            }
        }
    }
}
