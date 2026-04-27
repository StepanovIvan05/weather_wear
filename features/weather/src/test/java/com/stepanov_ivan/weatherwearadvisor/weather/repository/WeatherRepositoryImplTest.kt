package com.stepanov_ivan.weatherwearadvisor.weather.repository

import com.stepanov_ivan.weatherwearadvisor.data.WeatherCacheDao
import com.stepanov_ivan.weatherwearadvisor.data.WeatherCacheEntity
import com.stepanov_ivan.weatherwearadvisor.model.WeatherData
import com.stepanov_ivan.weatherwearadvisor.weather.api.OpenWeatherMapApi
import com.stepanov_ivan.weatherwearadvisor.weather.model.OpenWeatherResponse
import java.net.UnknownHostException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryImplTest {
    @Test
    fun getWeatherByCity_returnsFreshCacheWithoutRemoteCall() = runBlocking {
        val cache = FakeWeatherCacheDao().apply {
            upsert(
                WeatherCacheEntity.fromWeatherData(
                    cacheKey = "city:moscow",
                    weatherData = weatherData(city = "Moscow", temperature = 18.0),
                    cachedAtMillis = System.currentTimeMillis()
                )
            )
        }
        val api = FakeWeatherApi()
        val repository = WeatherRepositoryImpl(
            api = api,
            apiKey = "test-key",
            weatherCacheDao = cache,
            cacheTtlMillis = Long.MAX_VALUE
        )

        val result = repository.getWeatherByCity("Moscow")

        assertTrue(result.isSuccess)
        assertEquals(18.0, result.getOrThrow().temperature, 0.0)
        assertEquals(0, api.cityCalls)
    }

    @Test
    fun getWeatherByCity_returnsStaleCacheWhenRemoteFails() = runBlocking {
        val cache = FakeWeatherCacheDao().apply {
            upsert(
                WeatherCacheEntity.fromWeatherData(
                    cacheKey = "city:moscow",
                    weatherData = weatherData(city = "Moscow", temperature = 12.0),
                    cachedAtMillis = 0L
                )
            )
        }
        val repository = WeatherRepositoryImpl(
            api = FakeWeatherApi(failRequests = true),
            apiKey = "test-key",
            weatherCacheDao = cache,
            cacheTtlMillis = 1L
        )

        val result = repository.getWeatherByCity("Moscow")

        assertTrue(result.isSuccess)
        assertEquals(12.0, result.getOrThrow().temperature, 0.0)
    }

    @Test
    fun getWeatherByCity_updatesCacheAfterSuccessfulRemoteCall() = runBlocking {
        val cache = FakeWeatherCacheDao()
        val repository = WeatherRepositoryImpl(
            api = FakeWeatherApi(remoteTemperature = 21.0),
            apiKey = "test-key",
            weatherCacheDao = cache,
            cacheTtlMillis = 1L
        )

        val result = repository.getWeatherByCity("Moscow")

        assertTrue(result.isSuccess)
        assertEquals(21.0, cache.getByKey("city:moscow")?.temperature ?: 0.0, 0.0)
    }

    private class FakeWeatherCacheDao : WeatherCacheDao {
        private val values = mutableMapOf<String, WeatherCacheEntity>()

        override suspend fun getByKey(cacheKey: String): WeatherCacheEntity? {
            return values[cacheKey]
        }

        override suspend fun upsert(cache: WeatherCacheEntity) {
            values[cache.cacheKey] = cache
        }
    }

    private class FakeWeatherApi(
        private val failRequests: Boolean = false,
        private val remoteTemperature: Double = 20.0
    ) : OpenWeatherMapApi {
        var cityCalls = 0

        override suspend fun getWeatherByCity(
            city: String,
            apiKey: String,
            units: String,
            lang: String
        ): OpenWeatherResponse {
            cityCalls++
            if (failRequests) throw UnknownHostException("offline")
            return response(city = city, temperature = remoteTemperature)
        }

        override suspend fun getWeatherByCoordinates(
            latitude: Double,
            longitude: Double,
            apiKey: String,
            units: String,
            lang: String
        ): OpenWeatherResponse {
            if (failRequests) throw UnknownHostException("offline")
            return response(city = "Current location", temperature = remoteTemperature)
        }
    }

    companion object {
        private fun weatherData(city: String, temperature: Double): WeatherData {
            return WeatherData(
                city = city,
                temperature = temperature,
                feelsLike = temperature,
                humidity = 50,
                windSpeed = 3.0,
                description = "clear",
                icon = "01d"
            )
        }

        private fun response(city: String, temperature: Double): OpenWeatherResponse {
            return OpenWeatherResponse(
                main = OpenWeatherResponse.Main(
                    temp = temperature,
                    feels_like = temperature,
                    humidity = 50
                ),
                weather = listOf(
                    OpenWeatherResponse.Weather(
                        main = "Clear",
                        description = "clear",
                        icon = "01d"
                    )
                ),
                wind = OpenWeatherResponse.Wind(speed = 3.0),
                name = city
            )
        }
    }
}
