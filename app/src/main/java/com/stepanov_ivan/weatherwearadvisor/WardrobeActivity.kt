package com.stepanov_ivan.weatherwearadvisor

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.stepanov_ivan.weatherwearadvisor.databinding.ActivityWardrobeBinding
import com.stepanov_ivan.weatherwearadvisor.viewmodel.WardrobeViewModel

class WardrobeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWardrobeBinding
    private val viewModel: WardrobeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWardrobeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupRecyclers()
        setupNavigation()
        observeViewModel()
    }

    private fun setupRecyclers() {
        binding.categoriesRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.itemsRecycler.layoutManager = GridLayoutManager(this, 2)
    }

    private fun setupNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_wardrobe

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

    private fun observeViewModel() {
        viewModel.items.observe(this) { items ->
            binding.itemsRecycler.adapter = ClothingAdapter(items)
        }
    }
}
