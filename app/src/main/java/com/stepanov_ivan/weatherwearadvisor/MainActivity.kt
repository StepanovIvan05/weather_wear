package com.stepanov_ivan.weatherwearadvisor

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.stepanov_ivan.weatherwearadvisor.common.di.CommonModuleProvider
import com.stepanov_ivan.weatherwearadvisor.databinding.ActivityMainBinding
import com.stepanov_ivan.weatherwearadvisor.di.AppContainer
import com.stepanov_ivan.weatherwearadvisor.weather.WeatherModuleProvider
import com.stepanov_ivan.weatherwearadvisor.R

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1️⃣ Инициализировать core:common модуль
        CommonModuleProvider.initialize(this)
        
        // 2️⃣ Инициализировать features:weather модуль
        val apiKey = BuildConfig.OPENWEATHERMAP_API_KEY
        Log.d(TAG, "Initializing Weather with API key: ${apiKey.take(5)}...${apiKey.takeLast(5)} (length=${apiKey.length})")
        
        WeatherModuleProvider.initialize(apiKey)
        
        // 3️⃣ Инициализировать app контейнер
        AppContainer.init(this)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)

        // Проверка текущего пользователя: если его нет, переходим на логин
        val authRepository = AppContainer.authRepository
        if (!authRepository.isUserLoggedIn()) {
            navController.navigate(R.id.navigation_login)
        }

        // Управляем видимостью меню в зависимости от экрана
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_login, R.id.navigation_register -> {
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }
}
