package com.stepanov_ivan.weatherwearadvisor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class WardrobeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wardrobe)

        val categoriesRecycler = findViewById<RecyclerView>(R.id.categoriesRecycler)
        val itemsRecycler = findViewById<RecyclerView>(R.id.itemsRecycler)

        categoriesRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        itemsRecycler.layoutManager = GridLayoutManager(this, 2)

        val items = listOf(
            ClothingItem("Джинсовая куртка", "Levi's", R.drawable.outfit_placeholder),
            ClothingItem("Белые кеды", "Converse", R.drawable.outfit_placeholder),
            ClothingItem("Худи", "Nike", R.drawable.outfit_placeholder)
        )

        itemsRecycler.adapter = ClothingAdapter(items)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_wardrobe

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    false
                }
                R.id.navigation_recommendations -> {
                    startActivity(Intent(this, RecommendationsActivity::class.java))
                    false
                }
                R.id.navigation_wardrobe -> true
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
