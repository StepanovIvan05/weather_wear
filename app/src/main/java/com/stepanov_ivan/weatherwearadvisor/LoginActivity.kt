package com.stepanov_ivan.weatherwearadvisor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val root = findViewById<View>(R.id.rootLayout)
        root?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                insets
            }
        }

        val signUpLink = findViewById<TextView>(R.id.signUpLink)
        signUpLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_profile

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
                R.id.navigation_wardrobe -> {
                    startActivity(Intent(this, WardrobeActivity::class.java))
                    false
                }
                R.id.navigation_location -> {
                    startActivity(Intent(this, LocationSelectorActivity::class.java))
                    false
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }
}