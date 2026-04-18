package com.stepanov_ivan.weatherwearadvisor.model

data class City(
    val name: String,
    val region: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val countryCode: String? = null,
    val isActive: Boolean = false
)
