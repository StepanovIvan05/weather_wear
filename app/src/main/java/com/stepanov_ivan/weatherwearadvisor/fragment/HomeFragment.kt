package com.stepanov_ivan.weatherwearadvisor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.stepanov_ivan.weatherwearadvisor.databinding.FragmentHomeBinding
import com.stepanov_ivan.weatherwearadvisor.viewmodel.HomeViewModel
import com.stepanov_ivan.weatherwearadvisor.R

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

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
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.tvUserName.text = "Привет, ${state.userName}!"
            binding.tvCity.text = "📍 ${state.city}"
            binding.tvCondition.text = state.condition
            binding.tvTemp.text = state.temperature
            binding.tvFeelsLike.text = state.feelsLike
            binding.tvWindSpeed.text = state.windSpeed
            binding.tvHumidity.text = state.humidity
            binding.tvWardrobeCount.text = state.wardrobeCount
            binding.tvOutfitsCount.text = state.outfitsCount
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
