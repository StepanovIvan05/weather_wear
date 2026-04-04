package com.stepanov_ivan.weatherwearadvisor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.stepanov_ivan.weatherwearadvisor.databinding.ActivityMainBinding
import com.stepanov_ivan.weatherwearadvisor.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = navHostFragment.navController
            
            binding.bottomNavigation.setupWithNavController(navController)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
