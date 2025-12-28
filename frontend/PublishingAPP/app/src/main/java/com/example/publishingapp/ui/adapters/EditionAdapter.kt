package com.example.publishingapp.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.databinding.ItemEditionBinding

class EditionAdapter(
    private var editions: List<Edition>,
    private val onClick: (editionId: Int) -> Unit
) : RecyclerView.Adapter<EditionAdapter.Holder>() {

    class Holder(val binding: ItemEditionBinding) : RecyclerView.ViewHolder(binding.root)

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
        val resId = context.resources.getIdentifier(item.imageName, "drawable", context.packageName)
        holder.binding.img.setImageResource(resId)

        holder.binding.title.text = item.title
        holder.binding.author.text = "${item.firstName} ${item.lastName} ${item.middleName}".trim()

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
    }

    private fun getColorForGenre(genre: String): Int {
        val hash = genre.hashCode()
        val r = (hash shr 16) and 0xFF
        val g = (hash shr 8) and 0xFF
        val b = hash and 0xFF
        return Color.rgb(80 + (r % 150), 80 + (g % 150), 80 + (b % 150))
    }
}
