package com.example.publishingapp.data.network

import com.example.publishingapp.data.models.Edition
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    @GET("orders/by-review/{reviewId}")
    suspend fun getOrderByReviewId(@Path("reviewId") reviewId: Long): OrderFullDto
    @PUT("/api/orders/admin/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: Long,
        @Body request: UpdateOrderStatusRequest
    ): OrderFullDto

    @GET("/api/orders/admin/{id}")
    suspend fun getOrderByIdAdmin(@Path("id") id: Long): OrderFullDto

    @GET("/users/admin/all")
    suspend fun getAllUsers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("search") search: String? = null
    ): List<UserResponse>

    @GET("/users/admin/search")
    suspend fun searchUsers(
        @Query("username") username: String
    ): List<UserResponse>

    @GET("/users/admin/{id}")
    suspend fun getUserById(@Path("id") id: Long): UserResponse

    @PUT("/users/admin/{id}/role")
    suspend fun changeUserRole(
        @Path("id") id: Long,
        @Body request: ChangeRoleRequest
    ): UserResponse

    @GET("/api/reviews/pending-orders")
    suspend fun getOrdersForReview(): List<OrderFullDto>

    // Получить рецензию по ID заказа
    @GET("/api/reviews/order/{orderId}")
    suspend fun getReviewByOrderId(@Path("orderId") orderId: Long): ReviewDto

    // Создать рецензию
    @POST("/api/reviews/order/{orderId}")
    suspend fun createReview(
        @Path("orderId") orderId: Long,
        @Body request: CreateReviewRequest
    ): ReviewDto

    // Обновить рецензию
    @PUT("/api/reviews/{reviewId}")
    suspend fun updateReview(
        @Path("reviewId") reviewId: Long,
        @Body request: CreateReviewRequest
    ): ReviewDto

    // Получить мои рецензии
    @GET("/api/reviews/my")
    suspend fun getMyReviews(): List<ReviewDto>

    // Удалить рецензию (добавить в контроллер)
    @DELETE("/api/reviews/{reviewId}")
    suspend fun deleteReview(@Path("reviewId") reviewId: Long): Response<Unit>

    @GET("/api/reviews/order-details/{orderId}")
    suspend fun getOrderDetailsForReview(@Path("orderId") orderId: Long): OrderFullDto

    @Multipart
    @POST("api/files/upload")
    suspend fun uploadFiles(
        @Part cover: MultipartBody.Part?,
        @Part images: List<MultipartBody.Part>?
    ): List<String>

    @POST("api/editions")
    suspend fun createEdition(@Body request: EditionRequest): Edition

    @PUT("api/editions/{id}")
    suspend fun updateEdition(
        @Path("id") id: Long,
        @Body request: EditionRequest
    ): Edition

    @DELETE("api/editions/{id}")
    suspend fun deleteEdition(@Path("id") id: Long): Response<Unit>


    @GET("/api/orders/editor/editing-orders")
    suspend fun getEditingOrdersForEditor(): List<OrderFullDto>

    @POST("/api/orders/editor/{orderId}/comment")
    suspend fun addEditorComment(
        @Path("orderId") orderId: Long,
        @Body request: AddCommentRequest
    ): OrderCommentDto

    @POST("/api/orders/editor/{orderId}/send-to-review")
    suspend fun sendToReview(
        @Path("orderId") orderId: Long
    ): OrderFullDto

    @POST("/api/orders/author/{orderId}/comment")
    suspend fun addAuthorComment(
        @Path("orderId") orderId: Long,
        @Body request: AddCommentRequest
    ): OrderCommentDto

    @Multipart
    @POST("/api/orders/author/{orderId}/reupload")
    suspend fun reuploadFiles(
        @Path("orderId") orderId: Long,
        @Part files: List<MultipartBody.Part>,
        @Part("comment") comment: RequestBody? = null
    ): OrderFullDto

    @GET("/api/orders/{orderId}/comments")
    suspend fun getComments(@Path("orderId") orderId: Long): List<OrderCommentDto>
    @GET("/api/orders/editor/{orderId}")
    suspend fun getOrderByIdForEditor(@Path("orderId") orderId: Long): OrderFullDto

    @Multipart
    @POST("/api/orders/author/{orderId}/add-files")
    suspend fun addFiles(
        @Path("orderId") orderId: Long,
        @Part files: List<MultipartBody.Part>
    ): OrderFullDto

    @DELETE("/api/orders/author/{orderId}/files")
    suspend fun deleteFiles(
        @Path("orderId") orderId: Long,
        @Query("ids") fileIds: List<Long>
    ): OrderFullDto
}

