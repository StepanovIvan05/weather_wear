package com.stepanov_ivan.weatherwearadvisor.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.stepanov_ivan.weatherwearadvisor.R
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem

class ClothingAdapter(
    private val items: List<ClothingItem>,
    private val onItemClick: (ClothingItem) -> Unit,
    private val onItemLongClick: (ClothingItem) -> Unit
) : RecyclerView.Adapter<ClothingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image)
        val name: TextView = view.findViewById(R.id.name)
        val tempRange: TextView = view.findViewById(R.id.brand)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clothing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.tempRange.text = "${item.minTemp}°C - ${item.maxTemp}°C"
        
        Glide.with(holder.itemView.context)
            .load(item.photoUri)
            .placeholder(R.drawable.outfit_placeholder)
            .into(holder.image)

        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(item)
            true
        }
    }

    override fun getItemCount() = items.size
}
