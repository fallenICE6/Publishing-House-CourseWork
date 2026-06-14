package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.publishingapp.R
import com.example.publishingapp.data.network.NetworkConstants
import com.example.publishingapp.databinding.ItemPagePreviewBinding

class InteriorPagerAdapter(
    private val pages: List<String>
) : RecyclerView.Adapter<InteriorPagerAdapter.PageHolder>() {

    inner class PageHolder(
        val binding: ItemPagePreviewBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PageHolder {

        return PageHolder(
            ItemPagePreviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = pages.size

    override fun onBindViewHolder(
        holder: PageHolder,
        position: Int
    ) {

        holder.binding.pageImage.load(
            NetworkConstants.getFullImageUrl(
                pages[position]
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
}