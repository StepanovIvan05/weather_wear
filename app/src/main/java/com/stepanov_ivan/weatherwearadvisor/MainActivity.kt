package com.stepanov_ivan.weatherwearadvisor

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.stepanov_ivan.weatherwearadvisor.databinding.ActivityMainBinding
import com.stepanov_ivan.weatherwearadvisor.di.AppContainer
import com.stepanov_ivan.weatherwearadvisor.R

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authRepository = AppContainer.authRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        binding.bottomNavigation.setupWithNavController(navController)

        // Проверка текущего пользователя: если его нет, переходим на логин
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
