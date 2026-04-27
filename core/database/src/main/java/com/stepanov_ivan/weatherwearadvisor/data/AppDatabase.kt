package com.stepanov_ivan.weatherwearadvisor.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem

@Database(
    entities = [
        ClothingItem::class,
        WeatherCacheEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wardrobeDao(): WardrobeDao
    abstract fun weatherCacheDao(): WeatherCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS weather_cache (
                        cacheKey TEXT NOT NULL PRIMARY KEY,
                        city TEXT NOT NULL,
                        temperature REAL NOT NULL,
                        feelsLike REAL NOT NULL,
                        humidity INTEGER NOT NULL,
                        windSpeed REAL NOT NULL,
                        description TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        cachedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weather_wear_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
