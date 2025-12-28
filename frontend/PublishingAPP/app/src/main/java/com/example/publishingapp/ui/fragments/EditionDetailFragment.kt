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
import com.example.publishingapp.R
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.databinding.FragmentEditionDetailBinding
import com.example.publishingapp.ui.viewmodels.CatalogWorksViewModel

class EditionDetailFragment : Fragment(R.layout.fragment_edition_detail) {

    private lateinit var binding: FragmentEditionDetailBinding
    private val vm: CatalogWorksViewModel by activityViewModels() // Используем тот же ViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentEditionDetailBinding.bind(view)

        val editionId = arguments?.getInt("editionId") ?: return
        val edition: Edition? = vm.getEditionById(editionId)
        edition?.let { showEdition(it) }
    }

    private fun showEdition(edition: Edition) {
        val context = requireContext()

        // Обложка
        val resId = context.resources.getIdentifier(edition.imageName, "drawable", context.packageName)
        binding.imgCover.setImageResource(resId)

        binding.tvTitle.text = edition.title
        binding.tvAuthor.text = "${edition.firstName} ${edition.lastName} ${edition.middleName}".trim()
        binding.tvDescription.text = edition.description

        // Жанры
        binding.genreContainer.removeAllViews()
        edition.genres.forEach { genre ->
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
            binding.genreContainer.addView(chip)
        }

        // Галерея interiorImages
        binding.interiorGallery.removeAllViews()
        edition.interiorImages.forEach { imageName ->
            val imageView = ImageView(context).apply {
                val id = context.resources.getIdentifier(imageName, "drawable", context.packageName)
                setImageResource(id)
                layoutParams = LinearLayout.LayoutParams(400, 600).apply { setMargins(8, 8, 8, 8) }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            binding.interiorGallery.addView(imageView)
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
