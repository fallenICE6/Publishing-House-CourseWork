package com.example.publishingapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.publishingapp.data.models.Manuscript

object MockManuscriptRepository {

    private val list = mutableListOf(
        Manuscript(
            id = 1,
            title = "Моя первая книга",
            annotation = "Описание...",
            pages = 120,
            status = "IN_REVIEW",
            filePath = null
        ),
        Manuscript(
            id = 2,
            title = "Повесть о тестировании",
            annotation = "Тесты важны",
            pages = 54,
            status = "SUBMITTED",
            filePath = null
        )
    )

    private val _data = MutableLiveData(list.toList())
    val data: LiveData<List<Manuscript>> = _data

    fun add(manuscript: Manuscript) {
        list.add(manuscript)
        _data.postValue(list.toList())
    }

    fun getById(id: Long): Manuscript? {
        return list.find { it.id == id }
    }
}
