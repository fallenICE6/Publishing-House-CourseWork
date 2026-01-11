package com.example.publishingapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.publishingapp.data.repository.EditionRepository

class CatalogWorksViewModelFactory(
    private val repository: EditionRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogWorksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CatalogWorksViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
