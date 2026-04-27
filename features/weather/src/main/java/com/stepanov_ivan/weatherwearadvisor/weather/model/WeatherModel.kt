package com.stepanov_ivan.weatherwearadvisor.weather.model

import com.stepanov_ivan.weatherwearadvisor.model.WeatherData

data class OpenWeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val name: String
) {
    data class Main(
        val temp: Double,
        val feels_like: Double,
        val humidity: Int
    )

    data class Weather(
        val main: String,
        val description: String,
        val icon: String
    )

    data class Wind(
        val speed: Double
    )

    fun toWeatherData(): WeatherData {
        require(weather.isNotEmpty()) { "Weather data is empty" }

        val weatherInfo = weather.first()

        return WeatherData(
            city = name,
            temperature = main.temp,
            feelsLike = main.feels_like,
            humidity = main.humidity,
            windSpeed = wind.speed,
            description = weatherInfo.description,
            icon = weatherInfo.icon
        )
    }
}
