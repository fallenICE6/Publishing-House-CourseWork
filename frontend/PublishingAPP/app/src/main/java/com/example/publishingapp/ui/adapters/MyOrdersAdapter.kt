package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.OrderFullDto

class MyOrdersAdapter(
    private val items: List<OrderFullDto>,
    private val onClick: (OrderFullDto) -> Unit
) : RecyclerView.Adapter<MyOrdersAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvService)
        val status: TextView = v.findViewById(R.id.tvStatus)
        val price: TextView = v.findViewById(R.id.tvPrice)
        val orderTitle: TextView = v.findViewById(R.id.tvOrderTitle)
    }


    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH {
        val v = LayoutInflater.from(p.context)
            .inflate(R.layout.item_order, p, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, i: Int) {
        val o = items[i]

        h.orderTitle.text = "Заказ №${o.id}"
        h.title.text = o.serviceTitle
        h.status.text = o.status
        h.price.text = "${o.totalPrice} ₽"

        h.itemView.setOnClickListener { onClick(o) }
    }


    override fun getItemCount() = items.size
}
