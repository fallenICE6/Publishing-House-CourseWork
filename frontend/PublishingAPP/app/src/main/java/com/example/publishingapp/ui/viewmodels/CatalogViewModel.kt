package com.example.publishingapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.publishingapp.R
import com.example.publishingapp.data.models.ServiceItem

class CatalogViewModel : ViewModel() {

    private val _services = MutableLiveData<List<ServiceItem>>()
    val services: LiveData<List<ServiceItem>> = _services

    fun loadServices() {
        _services.value = listOf(
            ServiceItem(
                title = "Редактирование книги",
                shortDesc = "Профессиональная вычитка и литературная правка",
                imageRes = R.drawable.pic_1
            ),
            ServiceItem(
                title = "Верстка книги",
                shortDesc = "Создание макета любой сложности",
                imageRes = R.drawable.pic_2
            ),
            ServiceItem(
                title = "Печать",
                shortDesc = "Цветная/ч/б печать",
                imageRes = R.drawable.pic_3
            )
        )
    }
}
