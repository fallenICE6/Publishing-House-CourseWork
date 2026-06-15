package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.OrderCommentDto
import com.example.publishingapp.databinding.ItemCommentBinding
import java.text.SimpleDateFormat
import java.util.Locale

class CommentsAdapter : ListAdapter<OrderCommentDto, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: OrderCommentDto) {
            val roleEmoji = when (comment.userRole) {
                "EDITOR" -> "✏️"
                "AUTHOR" -> "📖"
                "ADMIN" -> "⚙️"
                "REVIEWER" -> "🔍"
                else -> "👤"
            }
            binding.tvUserName.text = "$roleEmoji ${comment.userName}"
            binding.tvComment.text = comment.comment

            val time = try {
                dateFormat.format(inputFormat.parse(comment.createdAt))
            } catch (e: Exception) {
                comment.createdAt.take(16)
            }
            binding.tvCommentDate.text = time

            if (comment.isSystem) {
                binding.tvUserName.visibility = View.GONE
                binding.tvCommentDate.visibility = View.GONE
                binding.tvComment.gravity = android.view.Gravity.CENTER_HORIZONTAL
                binding.tvComment.setTextColor(
                    binding.root.context.getColor(R.color.purple_60)
                )
                binding.tvComment.setTypeface(binding.tvComment.typeface, android.graphics.Typeface.ITALIC)
            }
            else {
                binding.tvUserName.visibility = View.VISIBLE
                binding.tvCommentDate.visibility = View.VISIBLE
                binding.tvComment.gravity = android.view.Gravity.START
                binding.tvComment.setTextColor(
                    binding.root.context.getColor(R.color.black)
                )
                binding.tvComment.setTypeface(binding.tvComment.typeface, android.graphics.Typeface.NORMAL)
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<OrderCommentDto>() {
        override fun areItemsTheSame(oldItem: OrderCommentDto, newItem: OrderCommentDto): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OrderCommentDto, newItem: OrderCommentDto): Boolean =
            oldItem == newItem
    }
}