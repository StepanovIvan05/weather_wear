package com.stepanov_ivan.weatherwearadvisor.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.stepanov_ivan.weatherwearadvisor.adapter.CityAdapter
import com.stepanov_ivan.weatherwearadvisor.databinding.FragmentLocationBinding
import com.stepanov_ivan.weatherwearadvisor.di.AppContainer
import com.stepanov_ivan.weatherwearadvisor.viewmodel.LocationViewModel
import com.stepanov_ivan.weatherwearadvisor.viewmodel.factory.LocationViewModelFactory
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

class LocationFragment : Fragment() {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory(AppContainer.locationRepository)
    }

    private val cityAdapter = CityAdapter { city ->
        viewModel.selectCity(city)
    }

    private var selectedMarker: Marker? = null
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            viewModel.detectCurrentLocation()
        } else {
            val context = context ?: return@registerForActivityResult
            Toast.makeText(
                context,
                "Location permission is required to detect your city",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

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
        setupMap()
        setupRecyclerView()
        setupButtons()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        binding.osmMap.onResume()
    }

    override fun onPause() {
        binding.osmMap.onPause()
        super.onPause()
    }

    private fun setupRecyclerView() {
        binding.citiesRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.citiesRecycler.adapter = cityAdapter
    }

    private fun setupButtons() {
        binding.listBtn.setOnClickListener { viewModel.switchMode(isMap = false) }
        binding.mapBtn.setOnClickListener { viewModel.switchMode(isMap = true) }

        binding.search.doAfterTextChanged { text ->
            viewModel.onSearchQueryChanged(text?.toString().orEmpty())
        }

        binding.currentLocationListBtn.setOnClickListener {
            detectCurrentLocationOrRequestPermission()
        }

        binding.currentLocationMapBtn.setOnClickListener {
            detectCurrentLocationOrRequestPermission()
        }
    }

    private fun setupMap() {
        val context = requireContext()
        Configuration.getInstance().userAgentValue = context.packageName

        binding.osmMap.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        binding.osmMap.setMultiTouchControls(true)
        binding.osmMap.controller.setZoom(4.5)
        binding.osmMap.controller.setCenter(GeoPoint(60.0, 90.0))
        binding.osmMap.overlays.add(createTapOverlay())
    }

    private fun createTapOverlay(): Overlay {
        return object : Overlay() {
            override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
                val projection = mapView.projection
                val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                viewModel.selectLocationOnMap(geoPoint.latitude, geoPoint.longitude)
                return true
            }
        }
    }

    private fun placeMarker(point: GeoPoint) {
        val map = binding.osmMap
        selectedMarker?.let { map.overlays.remove(it) }

        selectedMarker = Marker(map).apply {
            position = point
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Selected point"
        }

        map.overlays.add(selectedMarker)
        map.invalidate()
    }

    private fun observeViewModel() {
        viewModel.cities.observe(viewLifecycleOwner) { cities ->
            cityAdapter.submitList(cities)
        }

        viewModel.selectedCity.observe(viewLifecycleOwner) { city ->
            if (city == null) {
                binding.selectedCityText.text = "Selected city: not chosen"
                return@observe
            }

            binding.selectedCityText.text = "Selected city: ${city.name}, ${city.region}"
            val lat = city.latitude
            val lon = city.longitude
            if (lat != null && lon != null) {
                val point = GeoPoint(lat, lon)
                binding.osmMap.controller.animateTo(point)
                binding.osmMap.controller.setZoom(8.0)
                placeMarker(point)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.isMapMode.observe(viewLifecycleOwner) { isMap ->
            binding.listView.visibility = if (isMap) View.GONE else View.VISIBLE
            binding.mapView.visibility = if (isMap) View.VISIBLE else View.GONE
        }

        viewModel.statusMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNullOrBlank()) return@observe
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            viewModel.consumeStatusMessage()
        }
    }

    private fun detectCurrentLocationOrRequestPermission() {
        if (hasLocationPermission()) {
            viewModel.detectCurrentLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
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

    override fun onDestroyView() {
        selectedMarker = null
        _binding = null
        super.onDestroyView()
    }
}
