package com.example.publishingapp.data.network

import java.io.Serializable
import java.math.BigDecimal


data class RegisterRequest(
    val username: String,
    val phone: String,
    val email: String?,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?
)


data class LoginRequest(
    val username: String,
    val password: String
)
data class AuthResponse(
    val token: String,
    val user: UserDto
)
data class UserDto(
    val id: Long,
    val username: String,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val email: String?,
    val phone: String,
    val role: String
)

data class UserResponse(
    val id: Int,
    val username: String,
    val phone: String,
    val email: String?,
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val role: String
)

data class UpdateUserRequest(
    val firstName: String,
    val lastName: String,
    val middleName: String?,
    val email: String?,
    val phone: String
)

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)


data class ServiceDto(
    val id: Long,
    val title: String,
    val shortDescription: String,
    val fullDescription: String?,
    val price: Double,
    val image: String?,
    val category: String
) : Serializable

data class MaterialDto(
    val id: Long,
    val name: String,
    val category: String,
    val price: Double
) : Serializable

data class OrderDto(
    val id: Long,
    val userId: Long,
    val serviceId: Long,
    val totalPrice: Double,
    val pages: Int?,
    val quantity: Int,
    val status: String,
    val files: List<String>
)

data class MaterialSelectionDto(
    val materialId: Long,
    val quantity: Int
)

data class CreateOrderRequest(
    val serviceId: Long,
    val pages: Int? = null,
    val quantity: Int? = null,
    val materials: List<OrderMaterialRequest>? = null,
    val email: String
) : Serializable

data class OrderMaterialRequest(
    val materialId: Long
)

data class OrderFullDto(
    val id: Long,
    val fullName: String,
    val email: String?,
    val phone: String?,
    val serviceTitle: String,
    val pages: Int?,
    val quantity: Int?,
    val materials: List<OrderMaterialDto>,
    val files: List<OrderFileDto>,
    val review: ReviewDto?,
    val totalPrice: Double,
    val status: String,
    val createdAt: String
)

data class OrderMaterialDto(
    val name: String,
    val category: String,
    val quantity: Int,
    val price: BigDecimal
)
data class OrderFileDto(
    val id: Long,
    val fileName: String,
    val fileType: String?,
    val downloadUrl: String
)

data class ReviewDto(
    val text: String,
    val status: String
)

data class UpdateOrderStatusRequest(
    val status: String
)





