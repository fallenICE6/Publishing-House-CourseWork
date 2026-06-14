package com.example.publishingapp.ui.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.databinding.ItemSelectedFileBinding

class SelectedFilesAdapter(
    private val onDeleteClick: (Uri) -> Unit
) : RecyclerView.Adapter<SelectedFilesAdapter.FileViewHolder>() {

    private var files: List<Uri> = emptyList()

    fun submitList(newList: List<Uri>) {
        files = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemSelectedFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount() = files.size

    inner class FileViewHolder(
        private val binding: ItemSelectedFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            val fileName = uri.lastPathSegment ?: "Файл"
            binding.tvFileName.text = fileName
            binding.btnDelete.setOnClickListener {
                onDeleteClick(uri)
            }
        }
    }
}