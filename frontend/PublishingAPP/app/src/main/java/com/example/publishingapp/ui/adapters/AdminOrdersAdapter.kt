package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.OrderFullDto
import com.google.android.material.button.MaterialButton

class AdminOrdersAdapter(
    private val onOrderClick: (OrderFullDto) -> Unit,
    private val onStatusChange: (OrderFullDto) -> Unit
) : ListAdapter<OrderFullDto, AdminOrdersAdapter.ViewHolder>(OrderDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderNumber: TextView = view.findViewById(R.id.tvOrderNumber)
        val tvClientName: TextView = view.findViewById(R.id.tvClientName)
        val tvService: TextView = view.findViewById(R.id.tvService)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val btnChangeStatus: MaterialButton = view.findViewById(R.id.btnChangeStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = getItem(position)

        holder.tvOrderNumber.text = "Заказ №${order.id}"
        holder.tvClientName.text = order.fullName
        holder.tvService.text = order.serviceTitle
        holder.tvStatus.text = order.status
        holder.tvPrice.text = "${order.totalPrice} ₽"

        // Форматируем дату
        val date = order.createdAt.substring(0, 10) // Берем только дату
        holder.tvDate.text = date

        // Устанавливаем цвет статуса
        val statusBackground = when (order.status.lowercase()) {
            "создан" -> R.drawable.status_background_created
            "на проверке" -> R.drawable.status_background_under_review
            "редактируется" -> R.drawable.status_background_editing
            "готов к печати" -> R.drawable.status_background_ready_for_print
            "завершён" -> R.drawable.status_background_completed
            "отменён" -> R.drawable.status_background_canceled
            else -> R.drawable.status_background_created
        }
        holder.tvStatus.setBackgroundResource(statusBackground)

        // Показываем/скрываем кнопку изменения статуса
        val isEditable = when (order.status.lowercase()) {
            "завершён", "отменён" -> false
            else -> true
        }
        holder.btnChangeStatus.isVisible = isEditable
        holder.btnChangeStatus.text = if (isEditable) "Изменить статус" else "Неизменяемо"

        // Клик на весь элемент
        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }

        // Клик на кнопку изменения статуса
        if (isEditable) {
            holder.btnChangeStatus.setOnClickListener {
                onStatusChange(order)
            }
        } else {
            holder.btnChangeStatus.setOnClickListener(null)
        }
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderFullDto>() {
        override fun areItemsTheSame(oldItem: OrderFullDto, newItem: OrderFullDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: OrderFullDto, newItem: OrderFullDto): Boolean {
            return oldItem == newItem
        }
    }
}