package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R

class SliderDotsAdapter(
    private val count: Int,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<SliderDotsAdapter.VH>() {

    var selected = 0

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val dotView: View = view.findViewById(R.id.dotView)

        init {
            view.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onClick(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slider_dot, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.dotView.setBackgroundResource(
            if (position == selected)
                R.drawable.bg_slider_dot_active
            else
                R.drawable.bg_slider_dot
        )
    }

    override fun getItemCount() = count
}
