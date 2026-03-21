package com.stepanov_ivan.weatherwearadvisor.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stepanov_ivan.weatherwearadvisor.model.Outfit
import com.stepanov_ivan.weatherwearadvisor.R

class RecommendationsViewModel : ViewModel() {
    private val _outfits = MutableLiveData<List<Outfit>>()
    val outfits: LiveData<List<Outfit>> = _outfits

    init {
        _outfits.value = listOf(
            Outfit(
                "Весенний город",
                "Хлопковая куртка, светлые брюки, белые кроссовки",
                "15-20°C",
                R.drawable.outfit_placeholder
            ),
            Outfit(
                "Прогулка в парке",
                "Худи, джинсовая куртка, чиносы",
                "10-15°C",
                R.drawable.outfit_placeholder
            ),
            Outfit(
                "Дождливый день",
                "Плащ, непромокаемые кроссовки",
                "12-18°C",
                R.drawable.outfit_placeholder
            )
        )
    }
}