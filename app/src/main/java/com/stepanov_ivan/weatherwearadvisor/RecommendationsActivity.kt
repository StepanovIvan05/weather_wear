package com.stepanov_ivan.weatherwearadvisor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class RecommendationsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommendations)

        val recycler = findViewById<RecyclerView>(R.id.recycler)

        val outfits = listOf(
            Outfit(
                "Весенний город",
                "Хлопковая куртка, светлые брюки, белые кроссовки",
                "15-20°C",
                R.drawable.outfit_placeholder
            ),
            Outfit(
                "Прогулка в парке",
                "Худи, джинсовая куртка, чиносы",
                "10-15°C",
                R.drawable.outfit_placeholder
            ),
            Outfit(
                "Дождливый день",
                "Плащ, непромокаемые кроссовки",
                "12-18°C",
                R.drawable.outfit_placeholder
            )
        )

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = OutfitAdapter(outfits)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_recommendations

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    false
                }
                R.id.navigation_recommendations -> true
                R.id.navigation_wardrobe -> {
                    startActivity(Intent(this, WardrobeActivity::class.java))
                    false
                }
                R.id.navigation_location -> {
                    startActivity(Intent(this, LocationSelectorActivity::class.java))
                    false
                }
                R.id.navigation_profile -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }
}