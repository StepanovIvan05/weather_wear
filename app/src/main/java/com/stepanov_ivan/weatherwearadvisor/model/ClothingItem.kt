package com.stepanov_ivan.weatherwearadvisor.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wardrobe_items")
data class ClothingItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val photoUri: String,
    val minTemp: Int,
    val maxTemp: Int,
    val userId: String
)
