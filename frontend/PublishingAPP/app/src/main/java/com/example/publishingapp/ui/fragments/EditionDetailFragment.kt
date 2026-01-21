package com.example.publishingapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import coil.load
import com.example.publishingapp.R
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.data.network.NetworkConstants
import com.example.publishingapp.databinding.FragmentEditionDetailBinding
import com.example.publishingapp.ui.viewmodels.CatalogWorksViewModel

class EditionDetailFragment : Fragment(R.layout.fragment_edition_detail) {

    private lateinit var binding: FragmentEditionDetailBinding
    private val vm: CatalogWorksViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditionDetailBinding.bind(view)

        val editionId = arguments?.getLong("editionId") ?: return
        val edition: Edition? = vm.getEditionById(editionId)

        edition?.let { showEdition(it) }
    }

    private fun showEdition(edition: Edition) {
        val context = requireContext()

        // ===== Обложка =====
        edition.coverImage?.let { imageUrl ->
            loadImage(imageUrl, binding.imgCover)
        } ?: run {
            binding.imgCover.setImageResource(R.drawable.book1)
        }

        // ===== Текст =====
        binding.tvTitle.text = edition.title
        binding.tvAuthor.text = edition.fullAuthorName
        binding.tvDescription.text = edition.description ?: ""

        // ===== Жанры =====
        binding.genreContainer.removeAllViews()
        edition.genres.forEach { genre ->
            val chip = TextView(context).apply {
                text = genre
                setTextColor(Color.WHITE)
                textSize = 12f
                background = context.getDrawable(R.drawable.chip_genre)
                background.setTint(getColorForGenre(genre))
                setPadding(20, 8, 20, 8)

                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 8, 8, 8)
                layoutParams = params
            }
            binding.genreContainer.addView(chip)
        }

        // ===== Галерея внутренних изображений =====
        binding.interiorGallery.removeAllViews()
        edition.interiorImages.forEach { imageUrl ->
            val imageView = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(400, 600).apply {
                    setMargins(8, 8, 8, 8)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                loadImage(imageUrl, this)
            }
            binding.interiorGallery.addView(imageView)
        }
    }

    private fun loadImage(imageUrl: String, imageView: ImageView) {
        println("DEBUG EditionDetailFragment: Loading image: '$imageUrl'")

        // Создаем список возможных URL
        val urls = mutableListOf<String?>()

        // URL 1: Через NetworkConstants
        urls.add(NetworkConstants.getFullImageUrl(imageUrl))

        // URL 2: Прямой для эмулятора
        urls.add(if (imageUrl.startsWith("/")) {
            "http://10.0.2.2:8080$imageUrl"
        } else if (!imageUrl.startsWith("http")) {
            "http://10.0.2.2:8080/$imageUrl"
        } else {
            imageUrl
        })

        // URL 3: Просто в uploads
        urls.add("http://10.0.2.2:8080/uploads/$imageUrl")

        // Пробуем загрузить
        tryLoadUrls(urls, 0, imageView, imageUrl)
    }

    private fun tryLoadUrls(urls: List<String?>, index: Int, imageView: ImageView, originalUrl: String) {
        if (index >= urls.size) {
            // Все URL не сработали
            loadFromResources(originalUrl, imageView)
            return
        }

        val url = urls.getOrNull(index)
        if (url == null || url.isEmpty()) {
            tryLoadUrls(urls, index + 1, imageView, originalUrl)
            return
        }

        imageView.load(url) {
            placeholder(R.drawable.book1)
            error(R.drawable.book1)
            listener(
                onError = { _, _ ->
                    // Пробуем следующий URL
                    tryLoadUrls(urls, index + 1, imageView, originalUrl)
                }
            )
        }
    }

    private fun loadFromResources(imageUrl: String, imageView: ImageView) {
        try {
            val resId = resources.getIdentifier(imageUrl, "drawable", requireContext().packageName)
            if (resId != 0) {
                imageView.setImageResource(resId)
            } else {
                imageView.setImageResource(R.drawable.book1)
            }
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.book1)
        }
    }

    private fun getColorForGenre(genre: String): Int {
        val hash = genre.hashCode()
        val r = (hash shr 16) and 0xFF
        val g = (hash shr 8) and 0xFF
        val b = hash and 0xFF
        return Color.rgb(
            80 + (r % 150),
            80 + (g % 150),
            80 + (b % 150)
        )
    }
}