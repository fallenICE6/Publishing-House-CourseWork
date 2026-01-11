package com.example.publishingapp.ui.viewmodels

import androidx.lifecycle.*
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.ServiceDto
import kotlinx.coroutines.launch

class ServiceDetailsViewModel : ViewModel() {

    private val _service = MutableLiveData<ServiceDto>()
    val service: LiveData<ServiceDto> = _service

    fun loadService(id: Long) {
        viewModelScope.launch {
            try {
                _service.value = ApiClient.apiService.getServiceById(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
