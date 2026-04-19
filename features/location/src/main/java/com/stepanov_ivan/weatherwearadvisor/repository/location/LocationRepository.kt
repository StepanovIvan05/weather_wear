package com.stepanov_ivan.weatherwearadvisor.repository.location

import com.stepanov_ivan.weatherwearadvisor.model.City
import com.stepanov_ivan.weatherwearadvisor.model.Location

interface LocationRepository {
    fun getCities(): List<City>
    suspend fun getCurrentLocation(): Result<Location>
    suspend fun searchCities(query: String): Result<List<City>>
    suspend fun findNearestCity(latitude: Double, longitude: Double): Result<City>
    fun getSelectedCity(): City?
    fun saveSelectedCity(city: City)
}
