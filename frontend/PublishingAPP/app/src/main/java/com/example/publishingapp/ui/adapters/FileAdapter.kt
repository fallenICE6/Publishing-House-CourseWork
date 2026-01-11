package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.network.OrderFileDto

class FileAdapter(
    private val items: List<OrderFileDto>,
    private val onDownloadClick: (OrderFileDto) -> Unit
) : RecyclerView.Adapter<FileAdapter.ViewHolder>() {

    private val downloadingItems = mutableSetOf<Long>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val ivFileIcon: ImageView = view.findViewById(R.id.ivFileIcon)
        val btnDownload: ImageView = view.findViewById(R.id.btnDownload)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = items[position]

        holder.tvFileName.text = file.fileName

        // Определяем иконку по расширению файла
        val fileName = file.fileName.lowercase()
        val iconRes = when {
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> R.drawable.ic_image
            fileName.endsWith(".png") -> R.drawable.ic_image
            else -> R.drawable.ic_file
        }
        holder.ivFileIcon.setImageResource(iconRes)

        // Показываем/скрываем прогресс
        val isDownloading = downloadingItems.contains(file.id)
        holder.progressBar.visibility = if (isDownloading) View.VISIBLE else View.GONE
        holder.btnDownload.visibility = if (isDownloading) View.GONE else View.VISIBLE

        holder.btnDownload.setOnClickListener {
            // Показываем индикатор загрузки
            downloadingItems.add(file.id)
            notifyItemChanged(position)

            onDownloadClick(file)

            holder.itemView.postDelayed({
                downloadingItems.remove(file.id)
                notifyItemChanged(position)
            }, 10000)
        }
    }

    override fun getItemCount() = items.size
}