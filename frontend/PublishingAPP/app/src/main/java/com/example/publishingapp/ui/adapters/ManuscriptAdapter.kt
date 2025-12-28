package com.example.publishingapp.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.data.models.Manuscript
import com.example.publishingapp.databinding.ItemManuscriptBinding

class ManuscriptAdapter(
    private val onClick: (Manuscript) -> Unit
) : RecyclerView.Adapter<ManuscriptAdapter.Holder>() {

    private val list = mutableListOf<Manuscript>()

    inner class Holder(val binding: ItemManuscriptBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemManuscriptBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = list[position]

        holder.binding.tvTitle.text = item.title
        holder.binding.tvStatus.text = item.status

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = list.size

    fun submitList(newList: List<Manuscript>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
