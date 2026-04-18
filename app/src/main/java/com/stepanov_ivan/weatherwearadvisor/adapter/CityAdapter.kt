package com.stepanov_ivan.weatherwearadvisor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.stepanov_ivan.weatherwearadvisor.R
import com.stepanov_ivan.weatherwearadvisor.model.City

class CityAdapter(
    private val onCityClick: (City) -> Unit
) : ListAdapter<City, CityAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
        val region: TextView = view.findViewById(R.id.region)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.name.text = item.name
        holder.region.text = item.region
        holder.itemView.isSelected = item.isActive
        holder.itemView.setOnClickListener { onCityClick(item) }
    }

    private object DiffCallback : DiffUtil.ItemCallback<City>() {
        override fun areItemsTheSame(oldItem: City, newItem: City): Boolean {
            return oldItem.name == newItem.name &&
                oldItem.countryCode == newItem.countryCode &&
                oldItem.latitude == newItem.latitude &&
                oldItem.longitude == newItem.longitude
        }

        override fun areContentsTheSame(oldItem: City, newItem: City): Boolean = oldItem == newItem
    }
}
