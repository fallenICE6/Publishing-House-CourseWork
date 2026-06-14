package com.example.publishingapp.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.publishingapp.R
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.databinding.ItemAddEditionBinding
import com.example.publishingapp.databinding.ItemEditionBinding
import com.example.publishingapp.data.network.NetworkConstants

class EditionAdapter(
    private var editions: List<Edition>,
    private val onClick: (Long) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_ITEM = 1
    }

    private var isAdmin = false
    private var onLongClick: ((Edition) -> Unit)? = null

    fun setAdmin(value: Boolean) {
        isAdmin = value
        notifyDataSetChanged()
    }

    fun setOnLongClickListener(listener: (Edition) -> Unit) {
        onLongClick = listener
    }

    fun submitList(list: List<Edition>) {
        editions = list
        notifyDataSetChanged()
    }

    class EditionHolder(
        val binding: ItemEditionBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class AddHolder(
        val binding: ItemAddEditionBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int {
        return if (isAdmin) editions.size + 1 else editions.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (isAdmin && position == 0) TYPE_ADD else TYPE_ITEM
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_ADD -> {
                AddHolder(
                    ItemAddEditionBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }

            else -> {
                EditionHolder(
                    ItemEditionBinding.inflate(
                        inflater,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        when (holder) {

            is AddHolder -> {

                holder.itemView.setOnClickListener {
                    onClick(-1L)
                }
            }

            is EditionHolder -> {

                val realPosition =
                    if (isAdmin) position - 1 else position

                val item = editions[realPosition]

                bindEdition(holder, item)
            }
        }
    }

    private fun bindEdition(
        holder: EditionHolder,
        item: Edition
    ) {

        val binding = holder.binding
        val context = binding.root.context

        item.coverImage?.let {
            loadImage(it, binding.img)
        } ?: binding.img.setImageResource(R.drawable.book1)

        binding.title.text = item.title
        binding.author.text = item.fio

        binding.genreContainer.removeAllViews()

        item.genres.forEach { genre ->

            val chip = TextView(context).apply {

                text = genre

                textSize = 12f

                setTextColor(Color.WHITE)

                background =
                    context.getDrawable(R.drawable.chip_genre)

                background.setTint(
                    getColorForGenre(genre)
                )

                setPadding(
                    20,
                    8,
                    20,
                    8
                )
            }

            binding.genreContainer.addView(chip)
        }

        holder.itemView.setOnClickListener {
            onClick(item.id)
        }

        holder.itemView.setOnLongClickListener {

            onLongClick?.invoke(item)

            true
        }
    }

    private fun loadImage(
        imagePath: String,
        imageView: ImageView
    ) {

        val fullUrl =
            NetworkConstants.getFullImageUrl(imagePath)

        imageView.load(fullUrl) {

            crossfade(true)

            placeholder(R.drawable.book1)

            error(R.drawable.book1)
        }
    }

    private fun getColorForGenre(
        genre: String
    ): Int {

        val hash = genre.hashCode()

        return Color.rgb(
            80 + (hash shr 16 and 0xFF) % 150,
            80 + (hash shr 8 and 0xFF) % 150,
            80 + (hash and 0xFF) % 150
        )
    }
}