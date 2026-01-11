package com.example.publishingapp.ui.viewholders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.publishingapp.R
import com.example.publishingapp.data.network.NetworkConstants
import com.example.publishingapp.data.network.ServiceDto

class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val img: ImageView = itemView.findViewById(R.id.serviceImage)
    private val title: TextView = itemView.findViewById(R.id.serviceTitle)
    private val shortDesc: TextView = itemView.findViewById(R.id.serviceShortDesc)
    private val price: TextView = itemView.findViewById(R.id.servicePrice)

    fun bind(item: ServiceDto, onClick: (ServiceDto) -> Unit) {
        title.text = item.title
        shortDesc.text = item.shortDescription
        price.text = "${item.price} ₽"

        img.load("${NetworkConstants.BASE_URL}images/${item.image}") {
            placeholder(R.drawable.loading)
            error(R.drawable.loading)
        }

        itemView.setOnClickListener { onClick(item) }
    }
}
