package com.example.publishingapp.data.network

import com.example.publishingapp.data.models.Edition
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/editions")
    suspend fun getAllEditions(): List<Edition>

    @GET("api/editions/{id}")
    suspend fun getEditionById(@Path("id") id: Long): Edition

    @GET("api/editions/genre/{genre}")
    suspend fun getEditionsByGenre(@Path("genre") genre: String): List<Edition>

    @GET("api/editions/genres")
    suspend fun getAllGenres(): List<String>

    @GET("api/editions/search")
    suspend fun searchEditions(@Query("query") query: String): List<Edition>

    @GET("api/services")
    suspend fun getServices(
        @Query("category") category: String? = null
    ): List<ServiceDto>

    @GET("api/services/{id}")
    suspend fun getServiceById(
        @Path("id") id: Long
    ): ServiceDto

    @Multipart
    @POST("/api/orders/create")
    fun createOrder(
        @Part("order") order: RequestBody,
        @Part files: List<MultipartBody.Part>
    ): Call<OrderDto>


    @GET("api/materials")
    fun getMaterials(): Call<List<MaterialDto>>

    @GET("/api/orders/my")
    suspend fun getMyOrders(): List<OrderFullDto>

    @GET("/api/orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): OrderFullDto

    @GET("/api/files/{id}/download")
    suspend fun downloadFile(
        @retrofit2.http.Path("id") id: Long
    ): okhttp3.ResponseBody

    @GET("/api/orders/admin/all")
    suspend fun getAllOrders(
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): List<OrderFullDto>

    @PUT("/api/orders/admin/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Long,
        @Body request: UpdateOrderStatusRequest
    ): OrderFullDto

    @GET("/api/orders/admin/{id}")
    suspend fun getOrderByIdAdmin(@Path("id") id: Long): OrderFullDto
}

