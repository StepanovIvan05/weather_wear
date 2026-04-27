package com.stepanov_ivan.weatherwearadvisor.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stepanov_ivan.weatherwearadvisor.model.WeatherData

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val cacheKey: String,
    val city: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val icon: String,
    val cachedAtMillis: Long
) {
    fun toWeatherData(): WeatherData {
        return WeatherData(
            city = city,
            temperature = temperature,
            feelsLike = feelsLike,
            humidity = humidity,
            windSpeed = windSpeed,
            description = description,
            icon = icon
        )
    }

    companion object {
        fun fromWeatherData(cacheKey: String, weatherData: WeatherData, cachedAtMillis: Long): WeatherCacheEntity {
            return WeatherCacheEntity(
                cacheKey = cacheKey,
                city = weatherData.city,
                temperature = weatherData.temperature,
                feelsLike = weatherData.feelsLike,
                humidity = weatherData.humidity,
                windSpeed = weatherData.windSpeed,
                description = weatherData.description,
                icon = weatherData.icon,
                cachedAtMillis = cachedAtMillis
            )
        }
    }
}
