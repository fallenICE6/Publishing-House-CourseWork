
package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.OrderFullDto
import com.google.android.material.button.MaterialButton

class ReviewerOrdersAdapter(
    private val onOrderClick: (OrderFullDto) -> Unit,
    private val onWriteReview: (Long) -> Unit
) : ListAdapter<OrderFullDto, ReviewerOrdersAdapter.ViewHolder>(OrderDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvOrderNumber: TextView = view.findViewById(R.id.tvOrderNumber)
        val tvClientName: TextView = view.findViewById(R.id.tvClientName)
        val tvService: TextView = view.findViewById(R.id.tvService)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val btnWriteReview: MaterialButton = view.findViewById(R.id.btnWriteReview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reviewer_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = getItem(position)

        holder.tvOrderNumber.text = "Заказ №${order.id}"
        holder.tvClientName.text = order.fullName
        holder.tvService.text = order.serviceTitle
        holder.tvStatus.text = order.status
        holder.tvPrice.text = "${order.totalPrice} ₽"

        val date = order.createdAt.substring(0, 10)
        holder.tvDate.text = date

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

        // Проверяем, есть ли уже рецензия
        val hasReview = order.review != null
        if (hasReview) {
            holder.btnWriteReview.text = "Просмотреть рецензию"
            holder.btnWriteReview.setIconResource(R.drawable.ic_visibility)
            holder.btnWriteReview.isEnabled = false // Отключаем кнопку, если рецензия уже есть
        } else {
            holder.btnWriteReview.text = "Написать рецензию"
            holder.btnWriteReview.setIconResource(R.drawable.ic_edit)
            holder.btnWriteReview.isEnabled = true
        }

        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }

        holder.btnWriteReview.setOnClickListener {
            if (!hasReview) {
                onWriteReview(order.id)
            }
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