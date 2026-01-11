package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.ServiceDto
import com.example.publishingapp.ui.viewholders.ServiceViewHolder

class ServicesAdapter(
    private var list: List<ServiceDto>,
    private val onClick: (ServiceDto) -> Unit
) : RecyclerView.Adapter<ServiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(list[position], onClick)
    }

    override fun getItemCount(): Int = list.size

    fun updateData(newList: List<ServiceDto>) {
        list = newList
        notifyDataSetChanged()
    }
}
