package com.stepanov_ivan.weatherwearadvisor.model

data class Location(
    val latitude: Double,
    val longitude: Double,
    val cityName: String? = null,
    val country: String? = null
)
