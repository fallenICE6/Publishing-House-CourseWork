package com.example.publishingapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.publishingapp.R
import com.example.publishingapp.data.models.ServiceItem
import com.example.publishingapp.ui.adapters.ServicesAdapter

class CatalogFragment : Fragment(R.layout.activity_catalog) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvServices)

        rv.layoutManager = GridLayoutManager(requireContext(), 2)

        val testData = listOf(
            ServiceItem(
                title = "Редактирование книги",
                shortDesc = "Профессиональная вычитка и литературная правка.",
                imageRes = R.drawable.pic_1
            ),
            ServiceItem(
                title = "Верстка книги",
                shortDesc = "Создание макета любой сложности.",
                imageRes = R.drawable.pic_2
            ),
            ServiceItem(
                title = "Печать",
                shortDesc = "Цветная/ч/б печать, мягкий и твёрдый переплёт.",
                imageRes = R.drawable.pic_3
            )
        )

        rv.adapter = ServicesAdapter(testData)
    }
}
