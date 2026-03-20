package com.stepanov_ivan.weatherwearadvisor

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        bottomNavigation.selectedItemId = R.id.navigation_home
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_recommendations -> {
                    startActivity(Intent(this, RecommendationsActivity::class.java))
                    false
                }
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