// AdminEditionViewModel.kt
package com.example.publishingapp.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.data.network.EditionRequest
import com.example.publishingapp.data.repository.EditionRepository
import kotlinx.coroutines.launch

class AdminEditionViewModel(
    private val repository: EditionRepository
) : ViewModel() {

    private val _editionCreated = MutableLiveData<Edition>()
    val editionCreated: LiveData<Edition> = _editionCreated

    private val _editionUpdated = MutableLiveData<Edition>()
    val editionUpdated: LiveData<Edition> = _editionUpdated

    private val _editionDeleted = MutableLiveData<Long>()
    val editionDeleted: LiveData<Long> = _editionDeleted

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _uploadSuccess = MutableLiveData<Pair<String?, List<String>>>()
    val uploadSuccess: LiveData<Pair<String?, List<String>>> = _uploadSuccess

    fun uploadFiles(coverUri: Uri?, interiorUris: List<Uri>?) {
        viewModelScope.launch {
            try {
                val result = repository.uploadFiles(coverUri, interiorUris)
                _uploadSuccess.value = result
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки файлов: ${e.message}"
            }
        }
    }

    fun createEdition(request: EditionRequest) {
        viewModelScope.launch {
            try {
                val edition = repository.createEdition(request)
                _editionCreated.value = edition
            } catch (e: Exception) {
                _error.value = "Ошибка создания: ${e.message}"
            }
        }
    }

    fun updateEdition(id: Long, request: EditionRequest) {
        viewModelScope.launch {
            try {
                val edition = repository.updateEdition(id, request)
                _editionUpdated.value = edition
            } catch (e: Exception) {
                _error.value = "Ошибка обновления: ${e.message}"
            }
        }
    }

    fun deleteEdition(id: Long) {
        viewModelScope.launch {
            try {
                val success = repository.deleteEdition(id)
                if (success) {
                    _editionDeleted.value = id
                } else {
                    _error.value = "Не удалось удалить издание"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка удаления: ${e.message}"
            }
        }
    }
}