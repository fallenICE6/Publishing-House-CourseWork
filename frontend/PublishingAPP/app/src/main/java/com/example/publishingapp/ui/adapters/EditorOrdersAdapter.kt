package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.OrderFullDto
import com.example.publishingapp.databinding.ItemEditorOrderBinding
import java.text.SimpleDateFormat
import java.util.Locale

class EditorOrdersAdapter(
    private val onOrderClick: (OrderFullDto) -> Unit
) : ListAdapter<OrderFullDto, EditorOrdersAdapter.ViewHolder>(OrderDiffCallback()) {

    companion object {
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    }

    class ViewHolder(
        private val binding: ItemEditorOrderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderFullDto, onClick: (OrderFullDto) -> Unit) {
            binding.tvOrderNumber.text = "Заказ №${order.id}"

            val date = try {
                dateFormat.format(inputFormat.parse(order.createdAt))
            } catch (e: Exception) {
                order.createdAt.take(10)
            }
            binding.tvDate.text = date

            binding.tvClientName.text = order.fullName.takeIf { it.isNotBlank() } ?: "Клиент"
            binding.tvService.text = order.serviceTitle
            binding.tvStatus.text = order.status
            binding.tvPrice.text = String.format("%.2f ₽", order.totalPrice)

            val statusBackground = when (order.status.lowercase()) {
                "редактируется" -> R.drawable.status_background_editing
                "на проверке" -> R.drawable.status_background_under_review
                else -> R.drawable.status_background_created
            }
            binding.tvStatus.setBackgroundResource(statusBackground)

            binding.btnOpenChat.setOnClickListener { onClick(order) }
            binding.root.setOnClickListener { onClick(order) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditorOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onOrderClick)
    }

    class OrderDiffCallback : DiffUtil.ItemCallback<OrderFullDto>() {
        override fun areItemsTheSame(oldItem: OrderFullDto, newItem: OrderFullDto): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OrderFullDto, newItem: OrderFullDto): Boolean =
            oldItem == newItem
    }
}