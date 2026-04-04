package com.stepanov_ivan.weatherwearadvisor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.stepanov_ivan.weatherwearadvisor.adapter.CityAdapter
import com.stepanov_ivan.weatherwearadvisor.databinding.FragmentLocationBinding
import com.stepanov_ivan.weatherwearadvisor.model.City

class LocationFragment : Fragment() {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupButtons()
    }

    private fun setupRecyclerView() {
        binding.citiesRecycler.layoutManager = LinearLayoutManager(requireContext())
        
        // В будущем эти данные должны приходить из ViewModel
        val cities = listOf(
            City("Москва", "Россия", true),
            City("Санкт-Петербург", "Россия", false),
            City("Сочи", "Россия", false)
        )
        binding.citiesRecycler.adapter = CityAdapter(cities)
    }

    private fun setupButtons() {
        binding.listBtn.setOnClickListener {
            binding.listView.visibility = View.VISIBLE
            binding.mapView.visibility = View.GONE
        }

        binding.mapBtn.setOnClickListener {
            binding.listView.visibility = View.GONE
            binding.mapView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
