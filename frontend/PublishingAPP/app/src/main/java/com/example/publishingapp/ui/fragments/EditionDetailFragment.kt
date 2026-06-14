package com.example.publishingapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.example.publishingapp.R
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.data.network.NetworkConstants
import com.example.publishingapp.databinding.FragmentEditionDetailBinding
import com.example.publishingapp.ui.adapters.InteriorPagerAdapter
import com.example.publishingapp.ui.viewmodels.CatalogWorksViewModel

class EditionDetailFragment :
    Fragment(R.layout.fragment_edition_detail) {

    private lateinit var binding: FragmentEditionDetailBinding

    private val vm: CatalogWorksViewModel by activityViewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding =
            FragmentEditionDetailBinding.bind(view)

        val editionId =
            arguments?.getLong("editionId")
                ?: return

        vm.getEditionById(editionId)
            ?.let(::showEdition)
    }

    private fun showEdition(
        edition: Edition
    ) {

        binding.tvTitle.text =
            edition.title

        binding.tvAuthor.text =
            edition.fullAuthorName

        binding.tvDescription.text =
            edition.description ?: "Описание отсутствует"

        edition.coverImage?.let {
            loadImage(it, binding.imgCover)
        }

        setupGenres(edition)

        setupPages(edition)
    }

    private fun setupGenres(
        edition: Edition
    ) {

        val context = requireContext()

        binding.genreContainer.removeAllViews()

        edition.genres.forEach { genre ->

            val chip = TextView(context).apply {

                text = genre

                textSize = 12f

                setTextColor(Color.WHITE)

                background =
                    context.getDrawable(
                        R.drawable.chip_genre
                    )

                background.setTint(
                    getColorForGenre(genre)
                )

                setPadding(
                    24,
                    10,
                    24,
                    10
                )

                layoutParams =
                    ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {

                        setMargins(
                            8,
                            8,
                            8,
                            8
                        )
                    }
            }

            binding.genreContainer.addView(chip)
        }
    }

    private fun setupPages(
        edition: Edition
    ) {

        val adapter =
            InteriorPagerAdapter(
                edition.interiorImages
            )

        binding.viewPagerPages.adapter =
            adapter

        binding.tvPageCounter.text =
            "1 / ${edition.interiorImages.size}"

        binding.viewPagerPages.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(
                    position: Int
                ) {

                    binding.tvPageCounter.text =
                        "${position + 1} / ${edition.interiorImages.size}"
                }
            }
        )

        binding.viewPagerPages.setPageTransformer { page, position ->
            page.alpha = 0.9f + (1 - kotlin.math.abs(position)) * 0.1f
            page.scaleY = 0.98f + (1 - kotlin.math.abs(position)) * 0.02f
            page.scaleX = 0.98f + (1 - kotlin.math.abs(position)) * 0.02f
        }

        binding.btnPrev.setOnClickListener {

            val current =
                binding.viewPagerPages.currentItem

            if (current > 0) {

                binding.viewPagerPages.currentItem =
                    current - 1
            }
        }

        binding.btnNext.setOnClickListener {

            val current =
                binding.viewPagerPages.currentItem

            if (current <
                edition.interiorImages.lastIndex
            ) {

                binding.viewPagerPages.currentItem =
                    current + 1
            }
        }
    }



    private fun loadImage(
        imagePath: String,
        imageView: ImageView
    ) {

        imageView.load(
            NetworkConstants.getFullImageUrl(
                imagePath
            )
        ) {

            crossfade(true)

            placeholder(
                R.drawable.book1
            )

            error(
                R.drawable.book1
            )
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