package com.example.publishingapp.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.publishingapp.R
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.data.network.NetworkConstants
import com.example.publishingapp.databinding.ItemEditionBinding

class EditionAdapter(
    private var editions: List<Edition>,
    private val onClick: (editionId: Long) -> Unit
) : RecyclerView.Adapter<EditionAdapter.Holder>() {

    class Holder(val binding: ItemEditionBinding) : RecyclerView.ViewHolder(binding.root)

    private var onLongClick: ((Edition) -> Unit)? = null

    fun setOnLongClickListener(listener: (Edition) -> Unit) {
        onLongClick = listener
    }

    fun submitList(newList: List<Edition>) {
        editions = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemEditionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun getItemCount(): Int = editions.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = editions[position]
        val context = holder.itemView.context

        // Обложка
        item.coverImage?.let { imageUrl ->
            loadImage(imageUrl, holder.binding.img)
        } ?: run {
            holder.binding.img.setImageResource(R.drawable.book1)
        }

        holder.binding.title.text = item.title
        holder.binding.author.text = item.fio

        // Жанры
        holder.binding.genreContainer.removeAllViews()
        item.genres.forEach { genre ->
            val chip = TextView(context).apply {
                text = genre
                setTextColor(Color.WHITE)
                textSize = 12f
                background = context.getDrawable(R.drawable.chip_genre)
                background.setTint(getColorForGenre(genre))
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 8, 8, 8)
                layoutParams = params
                setPadding(20, 8, 20, 8)
            }
            holder.binding.genreContainer.addView(chip)
        }

        // Клик на элемент
        holder.itemView.setOnClickListener {
            onClick(item.id)
        }

        // Долгое нажатие (для админа)
        holder.itemView.setOnLongClickListener {
            onLongClick?.invoke(item)
            true
        }
    }

    private fun loadImage(imageUrl: String, imageView: android.widget.ImageView) {
        println("DEBUG EditionAdapter: Loading image: '$imageUrl'")

        val baseUrl = "http://10.0.2.2:8080"

        // Очищаем URL от возможных дублирований
        val cleanUrl = imageUrl.replace("//", "/")

        // Формируем правильный URL
        val fullUrl = when {
            cleanUrl.startsWith("http") -> cleanUrl
            cleanUrl.startsWith("/uploads/") -> baseUrl + cleanUrl  // ← правильный путь
            cleanUrl.startsWith("/") -> baseUrl + cleanUrl
            else -> baseUrl + "/" + cleanUrl
        }

        println("DEBUG EditionAdapter: Full URL: '$fullUrl'")

        // Только один URL, без проб разных вариантов
        imageView.load(fullUrl) {
            crossfade(true)
            placeholder(R.drawable.book1)
            error(R.drawable.book1)
            listener(
                onSuccess = { _, _ ->
                    println("DEBUG EditionAdapter: ✅ SUCCESS: $fullUrl")
                },
                onError = { _, result ->
                    println("DEBUG EditionAdapter: ❌ FAILED: $fullUrl - ${result.throwable.message}")
                    imageView.setImageResource(R.drawable.book1)
                }
            )
        }
    }

    private fun tryLoadUrls(urls: List<String?>, index: Int, imageView: android.widget.ImageView, originalUrl: String) {
        if (index >= urls.size) {
            // Все URL не сработали, пробуем ресурсы
            println("DEBUG EditionAdapter: All URLs failed, trying resources")
            loadFromResources(originalUrl, imageView)
            return
        }

        val url = urls.getOrNull(index)
        if (url == null || url.isEmpty()) {
            // Пробуем следующий URL
            tryLoadUrls(urls, index + 1, imageView, originalUrl)
            return
        }

        println("DEBUG EditionAdapter: Trying URL #$index: $url")

        imageView.load(url) {
            crossfade(true)
            placeholder(R.drawable.book1)
            error(R.drawable.book1)
            listener(
                onSuccess = { _, _ ->
                    println("DEBUG EditionAdapter: ✅ SUCCESS with URL #$index: $url")
                },
                onError = { _, result ->
                    println("DEBUG EditionAdapter: ❌ FAILED with URL #$index: $url")
                    println("DEBUG EditionAdapter: Error: ${result.throwable.message}")
                    // Пробуем следующий URL
                    tryLoadUrls(urls, index + 1, imageView, originalUrl)
                }
            )
        }
    }

    private fun loadFromResources(imageUrl: String, imageView: android.widget.ImageView) {
        println("DEBUG EditionAdapter: Trying to load from resources: '$imageUrl'")

        try {
            val resId = imageView.resources.getIdentifier(
                imageUrl,
                "drawable",
                imageView.context.packageName
            )
            if (resId != 0) {
                imageView.setImageResource(resId)
                println("DEBUG EditionAdapter: ✅ Loaded from resources: $imageUrl")
            } else {
                imageView.setImageResource(R.drawable.book1)
                println("DEBUG EditionAdapter: ❌ Resource not found, using placeholder")
            }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.book1)
            println("DEBUG EditionAdapter: ❌ Error loading resource: ${e.message}")
        }
    }

    private fun getColorForGenre(genre: String): Int {
        val hash = genre.hashCode()
        val r = (hash shr 16) and 0xFF
        val g = (hash shr 8) and 0xFF
        val b = hash and 0xFF
        return Color.rgb(80 + (r % 150), 80 + (g % 150), 80 + (b % 150))
    }
}