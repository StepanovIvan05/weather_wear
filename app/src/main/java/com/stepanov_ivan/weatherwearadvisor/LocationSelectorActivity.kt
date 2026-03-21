package com.stepanov_ivan.weatherwearadvisor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.stepanov_ivan.weatherwearadvisor.model.City


class LocationSelectorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_selector)

        val listView = findViewById<LinearLayout>(R.id.listView)
        val mapView = findViewById<FrameLayout>(R.id.mapView)

        val listBtn = findViewById<Button>(R.id.listBtn)
        val mapBtn = findViewById<Button>(R.id.mapBtn)

        val recycler = findViewById<RecyclerView>(R.id.citiesRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val cities = listOf(
            City("Москва", "Россия", true),
            City("Санкт-Петербург", "Россия", false),
            City("Сочи", "Россия", false)
        )

        recycler.adapter = CityAdapter(cities)

        listBtn.setOnClickListener {
            listView.visibility = View.VISIBLE
            mapView.visibility = View.GONE
        }

        mapBtn.setOnClickListener {
            listView.visibility = View.GONE
            mapView.visibility = View.VISIBLE
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.selectedItemId = R.id.navigation_location

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
                R.id.navigation_location -> true
                R.id.navigation_profile -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }
}
