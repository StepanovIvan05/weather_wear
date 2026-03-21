package com.stepanov_ivan.weatherwearadvisor

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.stepanov_ivan.weatherwearadvisor.databinding.ActivityRecommendationsBinding
import com.stepanov_ivan.weatherwearadvisor.viewmodel.RecommendationsViewModel

class RecommendationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecommendationsBinding
    private val viewModel: RecommendationsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecommendationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }

        setupRecyclerView()
        setupNavigation()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recycler.layoutManager = LinearLayoutManager(this)
    }

    private fun setupNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.navigation_recommendations

        binding.bottomNavigation.setOnItemSelectedListener { item ->
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

    private fun observeViewModel() {
        viewModel.outfits.observe(this) { outfits ->
            binding.recycler.adapter = OutfitAdapter(outfits)
        }
    }
}