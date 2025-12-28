package com.example.publishingapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.publishingapp.data.models.Manuscript
import com.example.publishingapp.data.repository.MockManuscriptRepository

class ManuscriptsViewModel : ViewModel() {

    val manuscripts: LiveData<List<Manuscript>> =
        MockManuscriptRepository.data

    fun reload() {
        // mock — ничего не делаем
    }
}
