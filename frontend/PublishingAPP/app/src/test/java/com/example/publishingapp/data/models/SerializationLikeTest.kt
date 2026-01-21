package com.example.publishingapp.data.network

import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class SerializationLikeTest {

    private val gson = Gson()

    @Test
    fun `service dto should serialize to json`() {
        val service = ServiceDto(
            id = 1L,
            title = "Print",
            shortDescription = "Short",
            fullDescription = "Full",
            price = 1000.0,
            image = null,
            category = "PRINT"
        )

        val json = gson.toJson(service)
        val restored = gson.fromJson(json, ServiceDto::class.java)

        assertEquals(service.title, restored.title)
        assertEquals(service.price, restored.price, 0.001)
    }

    @Test
    fun `material dto json serialization should preserve data`() {
        val material = MaterialDto(
            id = 2L,
            name = "Paper",
            category = "PAPER",
            price = 5.0
        )

        val json = gson.toJson(material)
        val restored = gson.fromJson(json, MaterialDto::class.java)

        assertEquals(material.name, restored.name)
        assertEquals(material.price, restored.price, 0.001)
    }

    @Test
    fun `create order request json serialization`() {
        val request = CreateOrderRequest(
            serviceId = 3L,
            pages = 200,
            quantity = 2,
            materials = null,
            email = "client@test.com"
        )

        val json = gson.toJson(request)
        val restored = gson.fromJson(json, CreateOrderRequest::class.java)

        assertEquals(request.email, restored.email)
        assertEquals(request.serviceId, restored.serviceId)
    }

    @Test
    fun `order dto json serialization with files list`() {
        val order = OrderDto(
            id = 1L,
            userId = 1L,
            serviceId = 1L,
            totalPrice = 100.0,
            pages = null,
            quantity = 1,
            status = "CREATED",
            files = listOf("a.pdf", "b.pdf")
        )

        val json = gson.toJson(order)
        val restored = gson.fromJson(json, OrderDto::class.java)

        assertEquals(2, restored.files.size)
        assertEquals("a.pdf", restored.files[0])
    }

    @Test
    fun `register request json serialization`() {
        val request = RegisterRequest(
            username = "testuser",
            phone = "+79991234567",
            email = "test@example.com",
            password = "password123",
            firstName = "Иван",
            lastName = "Иванов",
            middleName = "Иванович"
        )

        val json = gson.toJson(request)
        val restored = gson.fromJson(json, RegisterRequest::class.java)

        assertEquals(request.username, restored.username)
        assertEquals(request.phone, restored.phone)
        assertEquals(request.firstName, restored.firstName)
    }

    @Test
    fun `login request json serialization`() {
        val request = LoginRequest(
            username = "admin",
            password = "admin123"
        )

        val json = gson.toJson(request)
        val restored = gson.fromJson(json, LoginRequest::class.java)

        assertEquals(request.username, restored.username)
        assertEquals(request.password, restored.password)
    }

    @Test
    fun `auth response with user dto json serialization`() {
        val user = UserDto(
            id = 1L,
            username = "john_doe",
            firstName = "John",
            lastName = "Doe",
            middleName = null,
            email = "john@example.com",
            phone = "+79998887766",
            role = "USER"
        )

        val authResponse = AuthResponse(
            token = "jwt-token-here",
            user = user
        )

        val json = gson.toJson(authResponse)
        val restored = gson.fromJson(json, AuthResponse::class.java)

        assertEquals(authResponse.token, restored.token)
        assertEquals(user.username, restored.user.username)
        assertEquals(user.role, restored.user.role)
    }

    @Test
    fun `order full dto complex json serialization`() {
        val orderFull = OrderFullDto(
            id = 1L,
            fullName = "Иван Иванов",
            email = "ivan@example.com",
            phone = "+79991112233",
            serviceTitle = "Печать книги",
            pages = 100,
            quantity = 10,
            materials = listOf(
                OrderMaterialDto("Бумага", "paper", 100, BigDecimal("25.50"))
            ),
            files = listOf(
                OrderFileDto(1L, "file.pdf", "application/pdf", "/files/1")
            ),
            review = ReviewDto("Хорошо", "approved"),
            totalPrice = 2550.0,
            status = "completed",
            createdAt = "2024-01-01T10:00:00"
        )

        val json = gson.toJson(orderFull)
        val restored = gson.fromJson(json, OrderFullDto::class.java)

        assertEquals(orderFull.id, restored.id)
        assertEquals(orderFull.fullName, restored.fullName)
        assertEquals(orderFull.totalPrice, restored.totalPrice, 0.001)
        assertEquals(1, restored.materials.size)
        assertEquals(1, restored.files.size)
        assertNotNull(restored.review)
    }

    @Test
    fun `update order status request json serialization`() {
        val request = UpdateOrderStatusRequest(
            status = "under_review"
        )

        val json = gson.toJson(request)
        val restored = gson.fromJson(json, UpdateOrderStatusRequest::class.java)

        assertEquals(request.status, restored.status)
    }

    @Test
    fun `user response json serialization`() {
        val userResponse = UserResponse(
            id = 1,
            username = "test_user",
            phone = "+79990001122",
            email = "test@test.com",
            firstName = "Тест",
            lastName = "Тестов",
            middleName = "Тестович",
            role = "ADMIN"
        )

        val json = gson.toJson(userResponse)
        val restored = gson.fromJson(json, UserResponse::class.java)

        assertEquals(userResponse.id, restored.id)
        assertEquals(userResponse.role, restored.role)
        assertEquals("Тест Тестович Тестов",
            "${restored.firstName} ${restored.middleName} ${restored.lastName}".trim())
    }

    @Test
    fun `update user request json serialization`() {
        val request = UpdateUserRequest(
            firstName = "Анна",
            lastName = "Петрова",
            middleName = "Сергеевна",
            email = "anna@example.com",
            phone = "+79995556677"
        )

        val json = gson.toJson(request)
        val restored = gson.fromJson(json, UpdateUserRequest::class.java)

        assertEquals(request.firstName, restored.firstName)
        assertEquals(request.email, restored.email)
        assertEquals(request.phone, restored.phone)
    }

    @Test
    fun `change password request json serialization`() {
        val request = ChangePasswordRequest(
            currentPassword = "oldPass123",
            newPassword = "newPass456"
        )

        val json = gson.toJson(request)
        val restored = gson.fromJson(json, ChangePasswordRequest::class.java)

        assertEquals(request.currentPassword, restored.currentPassword)
        assertEquals(request.newPassword, restored.newPassword)
    }

    @Test
    fun `material selection dto json serialization`() {
        val selection = MaterialSelectionDto(
            materialId = 5L,
            quantity = 10
        )

        val json = gson.toJson(selection)
        val restored = gson.fromJson(json, MaterialSelectionDto::class.java)

        assertEquals(selection.materialId, restored.materialId)
        assertEquals(selection.quantity, restored.quantity)
    }

    @Test
    fun `order material request json serialization`() {
        val request = OrderMaterialRequest(
            materialId = 7L
        )

        val json = gson.toJson(request)
        val restored = gson.fromJson(json, OrderMaterialRequest::class.java)

        assertEquals(request.materialId, restored.materialId)
    }
}