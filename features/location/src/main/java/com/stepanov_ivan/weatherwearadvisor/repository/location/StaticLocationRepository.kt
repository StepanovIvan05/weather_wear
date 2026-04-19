package com.stepanov_ivan.weatherwearadvisor.repository.location

import com.stepanov_ivan.weatherwearadvisor.model.City
import com.stepanov_ivan.weatherwearadvisor.model.Location

class StaticLocationRepository : LocationRepository {
    private var selectedCity: City? = null

    override fun getCities(): List<City> {
        return listOf(
            City("Moscow", "Russia", 55.7558, 37.6173, "RU", true),
            City("Saint Petersburg", "Russia", 59.9343, 30.3351, "RU", false),
            City("Sochi", "Russia", 43.5855, 39.7231, "RU", false)
        )
    }

    override suspend fun getCurrentLocation(): Result<Location> {
        return Result.success(
            Location(
                latitude = 55.7558,
                longitude = 37.6173,
                cityName = "Moscow",
                country = "RU"
            )
        )
    }

    override suspend fun searchCities(query: String): Result<List<City>> {
        val cities = getCities()
        if (query.isBlank()) return Result.success(cities)
        return Result.success(
            cities.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.region.contains(query, ignoreCase = true)
            }
        )
    }

    override suspend fun findNearestCity(latitude: Double, longitude: Double): Result<City> {
        return Result.success(getCities().first())
    }

    override fun getSelectedCity(): City? = selectedCity

    override fun saveSelectedCity(city: City) {
        selectedCity = city
    }
}
