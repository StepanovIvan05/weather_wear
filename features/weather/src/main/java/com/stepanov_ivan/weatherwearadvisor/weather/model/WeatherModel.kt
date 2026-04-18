package com.stepanov_ivan.weatherwearadvisor.weather.model

/**
 * Модель данных о погоде от OpenWeatherMap API
 */
data class WeatherData(
    val city: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val icon: String
)

/**
 * Ответ от OpenWeatherMap API
 */
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
