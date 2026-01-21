package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.ReviewDto
import com.google.android.material.card.MaterialCardView

class MyReviewsAdapter(
    private val onReviewClick: (ReviewDto) -> Unit
) : ListAdapter<ReviewDto, MyReviewsAdapter.ViewHolder>(ReviewDiffCallback()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardReview)
        val tvOrderNumber: TextView = view.findViewById(R.id.tvOrderNumber)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvComment: TextView = view.findViewById(R.id.tvComment)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvOrderStatus: TextView = view.findViewById(R.id.tvOrderStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_my_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = getItem(position)

        holder.tvOrderNumber.text = "Заказ №${review.orderId}"

        // Статус рецензии
        val reviewStatus = when (review.status?.lowercase()) {
            "approved", "одобрена" -> "Одобрено"
            "rejected", "отклонена" -> "Отклонено"
            "pending", "на рассмотрении" -> "На доработку"
            else -> review.status ?: "Неизвестно"
        }
        holder.tvStatus.text = reviewStatus

        // Цвет статуса
        val statusBackground = when (review.status?.lowercase()) {
            "approved", "одобрена" -> R.drawable.status_background_ready_for_print
            "rejected", "отклонена" -> R.drawable.status_background_canceled
            "pending", "на рассмотрении" -> R.drawable.status_background_editing
            else -> R.drawable.status_background_created
        }
        holder.tvStatus.setBackgroundResource(statusBackground)

        // Комментарий (обрезаем если длинный)
        val comment = review.comment ?: "Без комментария"
        val shortComment = if (comment.length > 100) {
            comment.substring(0, 100) + "..."
        } else {
            comment
        }
        holder.tvComment.text = shortComment

        // Дата
        val date = try {
            review.createdAt.substring(0, 10)
        } catch (e: Exception) {
            review.createdAt
        }
        holder.tvDate.text = date

        // Статус заказа после рецензии
        val orderStatus = when (review.status?.lowercase()) {
            "approved", "одобрена" -> "Заказ одобрен для печати"
            "rejected", "отклонена" -> "Заказ отклонён"
            "pending", "на рассмотрении" -> "Заказ отправлен на доработку"
            else -> "Статус заказа обновлён"
        }
        holder.tvOrderStatus.text = orderStatus

        holder.card.setOnClickListener {
            onReviewClick(review)
        }
    }

    class ReviewDiffCallback : DiffUtil.ItemCallback<ReviewDto>() {
        override fun areItemsTheSame(oldItem: ReviewDto, newItem: ReviewDto): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReviewDto, newItem: ReviewDto): Boolean {
            return oldItem == newItem
        }
    }
}