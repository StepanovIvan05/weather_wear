package com.stepanov_ivan.weatherwearadvisor.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.firebase.auth.FirebaseAuth
import com.stepanov_ivan.weatherwearadvisor.databinding.FragmentHomeBinding
import com.stepanov_ivan.weatherwearadvisor.di.AppContainer
import com.stepanov_ivan.weatherwearadvisor.viewmodel.HomeViewModel
import com.stepanov_ivan.weatherwearadvisor.viewmodel.WeatherState
import com.stepanov_ivan.weatherwearadvisor.R

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private val auth = FirebaseAuth.getInstance()
    private val authRepository = AppContainer.authRepository

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            viewModel.loadWeatherByLocation()
        } else {
            // Показать сообщение о необходимости разрешений
            binding.tvFeelsLike.text = "Нужны разрешения на местоположение"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLocationWeather.setOnClickListener {
            if (hasLocationPermission()) {
                viewModel.loadWeatherByLocation()
            } else {
                requestLocationPermission()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun observeViewModel() {
        val userName = auth.currentUser?.displayName ?: "Алекс"
        
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.tvUserName.text = "Привет, $userName!"
            binding.tvCity.text = "📍 ${state.city}"
            binding.tvCondition.text = state.condition
            binding.tvTemp.text = state.temperature
            binding.tvFeelsLike.text = state.feelsLike
            binding.tvWindSpeed.text = state.windSpeed
            binding.tvHumidity.text = state.humidity
            binding.tvWardrobeCount.text = state.wardrobeCount
            binding.tvOutfitsCount.text = state.outfitsCount
        }

        // Наблюдаем за состоянием погоды
        viewModel.weatherState.observe(viewLifecycleOwner) { weatherState ->
            when (weatherState) {
                is WeatherState.Loading -> {
                    binding.tvCondition.text = "⏳ Загрузка..."
                    binding.tvTemp.text = "--°"
                    binding.tvFeelsLike.text = "Ощущается как --°"
                    binding.tvWindSpeed.text = "💨 -- км/ч"
                    binding.tvHumidity.text = "💧 --%"
                }
                is WeatherState.Success -> {
                    val weather = weatherState.weatherData
                    binding.tvCity.text = "📍 ${weather.city}"
                    binding.tvCondition.text = weather.description.replaceFirstChar { it.uppercase() }
                    binding.tvTemp.text = "${weather.temperature.toInt()}°"
                    binding.tvFeelsLike.text = "Ощущается как ${weather.feelsLike.toInt()}°"
                    binding.tvWindSpeed.text = "💨 ${weather.windSpeed.toInt()} км/ч"
                    binding.tvHumidity.text = "💧 ${weather.humidity}%"
                }
                is WeatherState.Error -> {
                    binding.tvCondition.text = "❌ Ошибка"
                    binding.tvTemp.text = "--°"
                    binding.tvFeelsLike.text = weatherState.message
                    binding.tvWindSpeed.text = "💨 -- км/ч"
                    binding.tvHumidity.text = "💧 --%"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
