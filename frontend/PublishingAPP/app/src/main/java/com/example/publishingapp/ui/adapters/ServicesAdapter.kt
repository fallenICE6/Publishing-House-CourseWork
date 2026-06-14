package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.ServiceDto
import com.example.publishingapp.ui.viewholders.ServiceViewHolder

class ServicesAdapter(
    private val onClick: (ServiceDto) -> Unit
) : RecyclerView.Adapter<ServiceViewHolder>() {

    private var fullList: List<ServiceDto> = emptyList()
    private var filteredList: List<ServiceDto> = emptyList()

    private var onEmptyChanged: ((Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(filteredList[position], onClick)
    }

    override fun getItemCount(): Int = filteredList.size

    fun setOnEmptyChangedListener(listener: (Boolean) -> Unit) {
        onEmptyChanged = listener
    }

    fun setData(newList: List<ServiceDto>) {
        fullList = newList
        filteredList = newList

        notifyDataSetChanged()
        onEmptyChanged?.invoke(false)
    }

    fun search(query: String) {
        val q = query.trim().lowercase()

        filteredList = if (q.isEmpty()) {
            fullList
        } else {
            fullList.filter {
                it.title.lowercase().contains(q) ||
                        it.shortDescription.lowercase().contains(q)
            }
        }

        notifyDataSetChanged()
        onEmptyChanged?.invoke(filteredList.isEmpty())
    }

    fun reset() {
        filteredList = fullList
        notifyDataSetChanged()
        onEmptyChanged?.invoke(false)
    }
}