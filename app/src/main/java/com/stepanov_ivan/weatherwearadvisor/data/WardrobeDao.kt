package com.stepanov_ivan.weatherwearadvisor.data

import androidx.room.*
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface WardrobeDao {
    @Query("SELECT * FROM wardrobe_items WHERE userId = :userId")
    fun getItemsByUserId(userId: String): Flow<List<ClothingItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ClothingItem)

    @Update
    suspend fun updateItem(item: ClothingItem)

    @Delete
    suspend fun deleteItem(item: ClothingItem)
}
