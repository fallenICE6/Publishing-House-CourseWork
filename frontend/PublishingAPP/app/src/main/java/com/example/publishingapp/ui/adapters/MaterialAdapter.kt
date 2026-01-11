package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.OrderMaterialDto

class MaterialAdapter(
    private val items: List<OrderMaterialDto>
) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvMaterialName)
        val tvCategory: TextView = view.findViewById(R.id.tvMaterialCategory)
        val tvQuantity: TextView = view.findViewById(R.id.tvMaterialQuantity)
        val tvPrice: TextView = view.findViewById(R.id.tvMaterialPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_material, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val material = items[position]

        holder.tvName.text = material.name

        // Переводим категорию на русский
        val categoryRussian = when (material.category.lowercase()) {
            "paper" -> "Бумага"
            "cover" -> "Обложка"
            "binding" -> "Переплёт"
            else -> material.category
        }
        holder.tvCategory.text = categoryRussian

        // Форматируем количество в зависимости от категории
        val quantityText = when (material.category.lowercase()) {
            "paper" -> "${material.quantity} листов"
            "cover", "binding" -> "${material.quantity} шт."
            else -> "${material.quantity} шт."
        }
        holder.tvQuantity.text = quantityText

        // Форматируем цену
        holder.tvPrice.text = "${material.price} ₽"
    }

    override fun getItemCount() = items.size
}