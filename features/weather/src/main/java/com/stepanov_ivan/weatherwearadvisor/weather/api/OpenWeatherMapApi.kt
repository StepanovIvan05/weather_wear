package com.stepanov_ivan.weatherwearadvisor.weather.api

import com.stepanov_ivan.weatherwearadvisor.weather.model.OpenWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API интерфейс для OpenWeatherMap
 * Документация: https://openweathermap.org/api
 */
interface OpenWeatherMapApi {
    @GET("weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ru"
    ): OpenWeatherResponse

    @GET("weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ru"
    ): OpenWeatherResponse
}
