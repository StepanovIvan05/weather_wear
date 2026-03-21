package com.stepanov_ivan.weatherwearadvisor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.stepanov_ivan.weatherwearadvisor.databinding.ActivityLoginBinding
import com.stepanov_ivan.weatherwearadvisor.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupNavigation()
        setupListeners()
        observeViewModel()
    }

    private fun setupNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_profile

        binding.bottomNavigation.setOnItemSelectedListener { item ->
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

    private fun setupListeners() {
        binding.signUpLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            // Simplified for demo
            viewModel.login("user@example.com", "password")
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { success ->
            if (success) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Ошибка входа", Toast.LENGTH_SHORT).show()
            }
        }
    }
}