package com.stepanov_ivan.weatherwearadvisor.repository.location

import com.stepanov_ivan.weatherwearadvisor.model.City
import com.stepanov_ivan.weatherwearadvisor.model.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class StaticLocationRepository : LocationRepository {
    private var selectedCity: City? = null

    override fun getCities(): List<City> {
        return CITIES.map { city ->
            city.copy(isActive = selectedCity?.let { selected ->
                selected.name == city.name && selected.countryCode == city.countryCode
            } ?: city.isActive)
        }
    }

    override suspend fun getCurrentLocation(): Result<Location> {
        val city = findNearestCity(55.7558, 37.6173).getOrNull() ?: CITIES.first()
        saveSelectedCity(city)
        return Result.success(
            Location(
                latitude = city.latitude ?: 55.7558,
                longitude = city.longitude ?: 37.6173,
                cityName = city.name,
                country = city.countryCode
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
        val nearest = getCities().minByOrNull { city ->
            val cityLat = city.latitude ?: return@minByOrNull Double.MAX_VALUE
            val cityLon = city.longitude ?: return@minByOrNull Double.MAX_VALUE
            distanceKm(latitude, longitude, cityLat, cityLon)
        }

        return if (nearest == null) {
            Result.failure(Exception("No nearby city was found for selected coordinates"))
        } else {
            Result.success(nearest)
        }
    }

    override fun getSelectedCity(): City? = selectedCity

    override fun saveSelectedCity(city: City) {
        selectedCity = city.copy(isActive = true)
    }

    private fun distanceKm(
        fromLat: Double,
        fromLon: Double,
        toLat: Double,
        toLon: Double
    ): Double {
        val dLat = Math.toRadians(toLat - fromLat)
        val dLon = Math.toRadians(toLon - fromLon)
        val lat1 = Math.toRadians(fromLat)
        val lat2 = Math.toRadians(toLat)

        val a = sin(dLat / 2).pow(2) + cos(lat1) * cos(lat2) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    private companion object {
        private const val EARTH_RADIUS_KM = 6371.0

        private val CITIES = listOf(
            City("Moscow", "Russia", 55.7558, 37.6173, "RU", true),
            City("Saint Petersburg", "Russia", 59.9343, 30.3351, "RU", false),
            City("Sochi", "Russia", 43.5855, 39.7231, "RU", false)
        )
    }
}
