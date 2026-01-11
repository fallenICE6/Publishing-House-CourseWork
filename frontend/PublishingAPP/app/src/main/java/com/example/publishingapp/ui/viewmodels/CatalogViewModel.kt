package com.example.publishingapp.ui.viewmodels

import androidx.lifecycle.*
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.ServiceDto
import kotlinx.coroutines.launch

class CatalogViewModel : ViewModel() {

    private val _services = MutableLiveData<List<ServiceDto>>()
    val services: LiveData<List<ServiceDto>> = _services

    fun loadServices(category: String? = null) {
        viewModelScope.launch {
            try {
                _services.value = ApiClient.apiService.getServices(category)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
