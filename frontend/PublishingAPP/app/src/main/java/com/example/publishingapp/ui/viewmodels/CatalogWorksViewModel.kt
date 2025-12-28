package com.example.publishingapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.publishingapp.data.models.Edition

class CatalogWorksViewModel : ViewModel() {

    private val _editions = MutableLiveData<List<Edition>>()
    val editions: LiveData<List<Edition>> = _editions

    private val _filtered = MutableLiveData<List<Edition>>()
    val filtered: LiveData<List<Edition>> = _filtered

    private val _genres = MutableLiveData<List<String>>()
    val genres: LiveData<List<String>> = _genres

    fun load() {
        val list = listOf(
            Edition(
                1, "Мастер и Маргарита",
                firstName = "Михаил",
                lastName = "Булгаков",
                middleName = "Афанасьевич",
                description = "Роман о любви, добре и зле, сатире на бюрократию и мистике Москвы 1930-х годов.",
                imageName = "book1",
                genres = listOf("Фантастика", "Классика"),
                interiorImages = listOf("book1", "book1", "book1")
            ),
            Edition(
                2, "Война и мир",
                firstName = "Лев",
                lastName = "Толстой",
                middleName = "Николаевич",
                description = "Эпический роман о судьбах нескольких семей на фоне событий Отечественной войны 1812 года.",
                imageName = "book2",
                genres = listOf("История", "Классика"),
                interiorImages = listOf("book1", "book1")
            ),
            Edition(
                3, "451°",
                firstName = "Рэй",
                lastName = "Брэдбери",
                middleName = "Дуглас",
                description = "Антиутопия о мире, где книги запрещены, а пожарные сжигают их.",
                imageName = "book3",
                genres = listOf("Фантастика"),
                interiorImages = listOf("book1")
            )
        )

        _editions.value = list
        _filtered.value = list
        _genres.value = list.flatMap { it.genres }.distinct()
    }



    fun filterByGenre(genre: String?) {
        _filtered.value = if (genre == null) _editions.value
        else _editions.value?.filter { it.genres.contains(genre) }
    }

    // Новый метод для детального экрана
    fun getEditionById(id: Int): Edition? {
        return _editions.value?.firstOrNull { it.id == id }
    }
}
