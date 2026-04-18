package com.stepanov_ivan.weatherwearadvisor.repository.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.stepanov_ivan.weatherwearadvisor.BuildConfig
import com.stepanov_ivan.weatherwearadvisor.model.City
import com.stepanov_ivan.weatherwearadvisor.model.Location
import com.stepanov_ivan.weatherwearadvisor.repository.location.api.OpenWeatherGeoCityResponse
import com.stepanov_ivan.weatherwearadvisor.repository.location.api.OpenWeatherGeocodingApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume

class LocationRepositoryImpl(
    private val context: Context
) : LocationRepository {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val geocodingApi: OpenWeatherGeocodingApi by lazy {
        Retrofit.Builder()
            .baseUrl(GEOCODING_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherGeocodingApi::class.java)
    }

    override fun getCities(): List<City> {
        val selected = getSelectedCity()
        return DEFAULT_CITIES.map { city ->
            city.copy(isActive = selected?.name == city.name && selected.countryCode == city.countryCode)
        }
    }

    override suspend fun getCurrentLocation(): Result<Location> {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasLocationPermission()) {
                    return@withContext Result.failure(Exception("Location permission is required"))
                }

                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                    !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ) {
                    return@withContext Result.failure(Exception("Enable location services to continue"))
                }

                val deviceLocation = suspendCancellableCoroutine<android.location.Location?> { continuation ->
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { continuation.resume(it) }
                        .addOnFailureListener {
                            Log.e(TAG, "Failed to get current location", it)
                            continuation.resume(null)
                        }
                }

                if (deviceLocation == null) {
                    return@withContext Result.failure(Exception("Could not get current location"))
                }

                val nearestCityResult = findNearestCity(deviceLocation.latitude, deviceLocation.longitude)
                val nearestCity = nearestCityResult.getOrNull()

                Result.success(
                    Location(
                        latitude = deviceLocation.latitude,
                        longitude = deviceLocation.longitude,
                        cityName = nearestCity?.name,
                        country = nearestCity?.countryCode
                    )
                )
            } catch (e: SecurityException) {
                Result.failure(Exception("Location permission is required"))
            } catch (e: Exception) {
                Result.failure(Exception("Location error: ${e.localizedMessage ?: e.message}"))
            }
        }
    }

    override suspend fun searchCities(query: String): Result<List<City>> {
        return withContext(Dispatchers.IO) {
            try {
                if (query.isBlank()) {
                    return@withContext Result.success(getCities())
                }

                val selected = getSelectedCity()
                val response = geocodingApi.searchCities(
                    query = query.trim(),
                    limit = SEARCH_LIMIT,
                    apiKey = BuildConfig.OPENWEATHERMAP_API_KEY
                )

                val result = response
                    .distinctBy { "${it.name.lowercase()}|${it.country}|${it.state}" }
                    .map { cityResponse -> cityResponse.toCity(selected) }

                Result.success(result)
            } catch (e: Exception) {
                Result.failure(Exception("City search failed: ${e.localizedMessage ?: e.message}"))
            }
        }
    }

    override suspend fun findNearestCity(latitude: Double, longitude: Double): Result<City> {
        return withContext(Dispatchers.IO) {
            try {
                val selected = getSelectedCity()
                val response = geocodingApi.reverseGeocode(
                    latitude = latitude,
                    longitude = longitude,
                    limit = 1,
                    apiKey = BuildConfig.OPENWEATHERMAP_API_KEY
                )
                val city = response.firstOrNull()?.toCity(selected)
                if (city == null) {
                    Result.failure(Exception("No nearby city was found for selected coordinates"))
                } else {
                    Result.success(city)
                }
            } catch (e: Exception) {
                Result.failure(Exception("Reverse geocoding failed: ${e.localizedMessage ?: e.message}"))
            }
        }
    }

    override fun getSelectedCity(): City? {
        val name = prefs.getString(KEY_CITY_NAME, null) ?: return null
        return City(
            name = name,
            region = prefs.getString(KEY_REGION, "") ?: "",
            latitude = prefs.getString(KEY_LATITUDE, null)?.toDoubleOrNull(),
            longitude = prefs.getString(KEY_LONGITUDE, null)?.toDoubleOrNull(),
            countryCode = prefs.getString(KEY_COUNTRY, null),
            isActive = true
        )
    }

    override fun saveSelectedCity(city: City) {
        prefs.edit()
            .putString(KEY_CITY_NAME, city.name)
            .putString(KEY_REGION, city.region)
            .putString(KEY_LATITUDE, city.latitude?.toString())
            .putString(KEY_LONGITUDE, city.longitude?.toString())
            .putString(KEY_COUNTRY, city.countryCode)
            .apply()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun OpenWeatherGeoCityResponse.toCity(selected: City?): City {
        val regionTitle = listOfNotNull(state, country).joinToString(", ")
        return City(
            name = name,
            region = regionTitle,
            latitude = lat,
            longitude = lon,
            countryCode = country,
            isActive = selected?.name == name && selected.countryCode == country
        )
    }

    companion object {
        private const val TAG = "LocationRepository"
        private const val GEOCODING_BASE_URL = "https://api.openweathermap.org/"
        private const val SEARCH_LIMIT = 30
        private const val PREFS_NAME = "location_preferences"
        private const val KEY_CITY_NAME = "city_name"
        private const val KEY_REGION = "city_region"
        private const val KEY_LATITUDE = "city_latitude"
        private const val KEY_LONGITUDE = "city_longitude"
        private const val KEY_COUNTRY = "city_country"

        private val DEFAULT_CITIES = listOf(
            City("Moscow", "RU", 55.7558, 37.6173, "RU"),
            City("Saint Petersburg", "RU", 59.9343, 30.3351, "RU"),
            City("Novosibirsk", "RU", 55.0084, 82.9357, "RU"),
            City("Krasnoyarsk", "RU", 56.0153, 92.8932, "RU"),
            City("Yekaterinburg", "RU", 56.8389, 60.6057, "RU"),
            City("Sochi", "RU", 43.5855, 39.7231, "RU")
        )
    }
}
