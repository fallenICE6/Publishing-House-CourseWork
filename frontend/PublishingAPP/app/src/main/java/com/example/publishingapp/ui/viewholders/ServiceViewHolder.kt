package com.example.publishingapp.ui.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.models.ServiceItem

class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val img: ImageView = itemView.findViewById(R.id.serviceImage)
    private val title: TextView = itemView.findViewById(R.id.serviceTitle)
    private val shortDesc: TextView = itemView.findViewById(R.id.serviceShortDesc)

    fun bind(item: ServiceItem) {
        title.text = item.title
        shortDesc.text = item.shortDesc

        img.setImageResource(item.imageRes)
    }
}
