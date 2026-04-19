package com.stepanov_ivan.weatherwearadvisor.model

data class City(
    val name: String,
    val region: String,
    val latitude: Double?,
    val longitude: Double?,
    val countryCode: String?,
    val isActive: Boolean = false
)
