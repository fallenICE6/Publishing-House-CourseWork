package com.example.publishingapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.data.repository.EditionRepository
import kotlinx.coroutines.launch

class CatalogWorksViewModel(
    private val repository: EditionRepository
) : ViewModel() {

    private val _editions = MutableLiveData<List<Edition>>()
    val editions: LiveData<List<Edition>> = _editions

    private val _filtered = MutableLiveData<List<Edition>>()
    val filtered: LiveData<List<Edition>> = _filtered

    private val _genres = MutableLiveData<List<String>>()
    val genres: LiveData<List<String>> = _genres

    fun load() {
        viewModelScope.launch {
            val list = repository.getAllEditions()
            _editions.value = list
            _filtered.value = list
            _genres.value = list.flatMap { it.genres }.distinct()
        }
    }

    fun filterByGenre(genre: String?) {
        _filtered.value = if (genre == null || genre == "Все") {
            _editions.value
        } else {
            _editions.value?.filter { it.genres.contains(genre) }
        }
    }

    fun getEditionById(id: Long): Edition? {
        return _editions.value?.firstOrNull { it.id == id }
    }
}
