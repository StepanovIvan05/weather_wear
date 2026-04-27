package com.stepanov_ivan.weatherwearadvisor.weather.repository

import android.util.Log
import com.stepanov_ivan.weatherwearadvisor.data.WeatherCacheDao
import com.stepanov_ivan.weatherwearadvisor.data.WeatherCacheEntity
import com.stepanov_ivan.weatherwearadvisor.model.WeatherData
import com.stepanov_ivan.weatherwearadvisor.weather.api.OpenWeatherMapApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Locale

interface WeatherRepository {
    suspend fun getWeatherByCity(city: String): Result<WeatherData>
    suspend fun getWeatherByCoordinates(latitude: Double, longitude: Double): Result<WeatherData>
}

class WeatherRepositoryImpl(
    private val api: OpenWeatherMapApi,
    private val apiKey: String,
    private val weatherCacheDao: WeatherCacheDao? = null,
    private val cacheTtlMillis: Long = CACHE_TTL_MILLIS
) : WeatherRepository {

    init {
        require(apiKey.isNotBlank()) { "API key cannot be blank" }
    }

    override suspend fun getWeatherByCity(city: String): Result<WeatherData> {
        val cacheKey = cityCacheKey(city)
        return getWeatherWithCache(cacheKey) {
            api.getWeatherByCity(
                city = city,
                apiKey = apiKey
            ).toWeatherData()
        }
    }

    override suspend fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double
    ): Result<WeatherData> {
        val cacheKey = coordinatesCacheKey(latitude, longitude)
        return getWeatherWithCache(cacheKey) {
            api.getWeatherByCoordinates(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).toWeatherData()
        }
    }

    private suspend fun getWeatherWithCache(
        cacheKey: String,
        remoteCall: suspend () -> WeatherData
    ): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            val cached = weatherCacheDao?.getByKey(cacheKey)
            if (cached != null && cached.isFresh()) {
                Log.d(TAG, "Using fresh weather cache for $cacheKey")
                return@withContext Result.success(cached.toWeatherData())
            }

            try {
                Log.d(TAG, "Fetching weather from remote for $cacheKey")
                val weatherData = remoteCall()
                weatherCacheDao?.upsert(
                    WeatherCacheEntity.fromWeatherData(
                        cacheKey = cacheKey,
                        weatherData = weatherData,
                        cachedAtMillis = System.currentTimeMillis()
                    )
                )
                Result.success(weatherData)
            } catch (e: Exception) {
                val cachedFallback = cached ?: weatherCacheDao?.getByKey(cacheKey)
                if (cachedFallback != null) {
                    Log.w(TAG, "Remote weather failed, using stale cache for $cacheKey", e)
                    Result.success(cachedFallback.toWeatherData())
                } else {
                    val errorMsg = e.toWeatherErrorMessage()
                    Log.e(TAG, errorMsg, e)
                    Result.failure(Exception(errorMsg))
                }
            }
        }
    }

    private fun WeatherCacheEntity.isFresh(): Boolean {
        return System.currentTimeMillis() - cachedAtMillis <= cacheTtlMillis
    }

    private fun Exception.toWeatherErrorMessage(): String {
        return when (this) {
            is HttpException -> when (code()) {
                401 -> "API ключ некорректен. Проверьте конфигурацию OpenWeatherMap."
                404 -> "Погода для выбранной локации не найдена."
                429 -> "Слишком много запросов к сервису погоды. Попробуйте позже."
                else -> "HTTP ${code()}: ${message()}"
            }
            is SocketTimeoutException -> "Время ожидания ответа истекло. Проверьте интернет-соединение."
            is UnknownHostException -> "Нет соединения с интернетом. Проверьте WiFi или мобильные данные."
            is IllegalArgumentException -> "Некорректные данные от сервера погоды: ${message}"
            else -> "Ошибка при получении погоды: ${localizedMessage ?: message}"
        }
    }

    private fun cityCacheKey(city: String): String {
        return "city:${city.trim().lowercase(Locale.ROOT)}"
    }

    private fun coordinatesCacheKey(latitude: Double, longitude: Double): String {
        return "coords:${"%.2f".format(Locale.US, latitude)},${"%.2f".format(Locale.US, longitude)}"
    }

    companion object {
        private const val TAG = "WeatherRepository"
        private const val CACHE_TTL_MILLIS = 30 * 60 * 1000L
    }
}
