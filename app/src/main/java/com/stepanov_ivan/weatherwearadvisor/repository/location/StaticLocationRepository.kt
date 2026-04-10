package com.stepanov_ivan.weatherwearadvisor.repository.location

import com.stepanov_ivan.weatherwearadvisor.model.City

class StaticLocationRepository : LocationRepository {
    override fun getCities(): List<City> {
        return listOf(
            City("Москва", "Россия", true),
            City("Санкт-Петербург", "Россия", false),
            City("Сочи", "Россия", false)
        )
    }
}
