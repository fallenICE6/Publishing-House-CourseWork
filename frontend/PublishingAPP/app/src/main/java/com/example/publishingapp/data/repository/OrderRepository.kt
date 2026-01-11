package com.example.publishingapp.data.repository



import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.OrderFullDto
import okhttp3.ResponseBody

object OrderRepository {

    private val api = ApiClient.apiService

    suspend fun getMyOrders(): List<OrderFullDto> {
        return api.getMyOrders()
    }

    suspend fun downloadFile(fileId: Long): ResponseBody {
        return api.downloadFile(fileId)
    }
}
