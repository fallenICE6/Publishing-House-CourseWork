package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.OrderFileDto
import com.example.publishingapp.databinding.ItemFileManageBinding

class FileManageAdapter(
    private val onDeleteFile: (Long) -> Unit,
    private val onDownloadFile: (OrderFileDto) -> Unit
) : ListAdapter<OrderFileDto, FileManageAdapter.ViewHolder>(FileDiffCallback()) {

    class ViewHolder(
        private val binding: ItemFileManageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(file: OrderFileDto, onDelete: (Long) -> Unit, onDownload: (OrderFileDto) -> Unit) {
            binding.tvFileName.text = file.fileName

            val iconRes = when {
                file.fileName.lowercase().endsWith(".jpg") ||
                        file.fileName.lowercase().endsWith(".jpeg") ||
                        file.fileName.lowercase().endsWith(".png") -> R.drawable.ic_image
                file.fileName.lowercase().endsWith(".pdf") -> R.drawable.ic_pdf
                else -> R.drawable.ic_file
            }
            binding.ivFileIcon.setImageResource(iconRes)

            binding.btnDownload.setOnClickListener { onDownload(file) }
            binding.btnDelete.setOnClickListener { onDelete(file.id) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFileManageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onDeleteFile, onDownloadFile)
    }

    class FileDiffCallback : DiffUtil.ItemCallback<OrderFileDto>() {
        override fun areItemsTheSame(oldItem: OrderFileDto, newItem: OrderFileDto): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: OrderFileDto, newItem: OrderFileDto): Boolean =
            oldItem == newItem
    }
}