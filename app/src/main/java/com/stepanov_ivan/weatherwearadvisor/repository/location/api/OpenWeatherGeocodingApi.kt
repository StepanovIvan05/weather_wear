package com.stepanov_ivan.weatherwearadvisor.repository.location.api

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherGeocodingApi {
    @GET("geo/1.0/direct")
    suspend fun searchCities(
        @Query("q") query: String,
        @Query("limit") limit: Int = 30,
        @Query("appid") apiKey: String
    ): List<OpenWeatherGeoCityResponse>

    @GET("geo/1.0/reverse")
    suspend fun reverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<OpenWeatherGeoCityResponse>
}

data class OpenWeatherGeoCityResponse(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
)
