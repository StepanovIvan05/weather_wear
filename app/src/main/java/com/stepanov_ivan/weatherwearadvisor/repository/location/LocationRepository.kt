package com.stepanov_ivan.weatherwearadvisor.repository.location

import com.stepanov_ivan.weatherwearadvisor.model.City

interface LocationRepository {
    fun getCities(): List<City>
}
