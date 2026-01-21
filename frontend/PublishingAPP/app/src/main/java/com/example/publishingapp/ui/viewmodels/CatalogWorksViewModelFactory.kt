package com.example.publishingapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.publishingapp.data.repository.EditionRepository

class CatalogWorksViewModelFactory(
    private val repository: EditionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CatalogWorksViewModel::class.java)) {
            return CatalogWorksViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}