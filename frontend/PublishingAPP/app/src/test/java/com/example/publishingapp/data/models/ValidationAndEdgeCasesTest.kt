package com.example.publishingapp.data.network

import org.junit.Assert.*
import org.junit.Test

class ValidationAndEdgeCasesTest {

    @Test
    fun `user dto with empty middle name should be allowed`() {
        val user = UserDto(
            id = 1L,
            username = "author",
            firstName = "Ivan",
            lastName = "Ivanov",
            middleName = "",
            email = null,
            phone = "+79990000000",
            role = "AUTHOR"
        )

        assertEquals("", user.middleName)
    }

    @Test
    fun `service with zero price should be valid`() {
        val service = ServiceDto(
            id = 2L,
            title = "Free consultation",
            shortDescription = "Intro",
            fullDescription = null,
            price = 0.0,
            image = null,
            category = "CONSULT"
        )

        assertEquals(0.0, service.price, 0.0)
    }

    @Test
    fun `order with zero pages should be allowed`() {
        val order = OrderDto(
            id = 1L,
            userId = 1L,
            serviceId = 1L,
            totalPrice = 1000.0,
            pages = 0,
            quantity = 1,
            status = "CREATED",
            files = emptyList()
        )

        assertEquals(0, order.pages)
    }

    @Test
    fun `change password request should store both passwords`() {
        val request = ChangePasswordRequest(
            currentPassword = "oldPass",
            newPassword = "newPass"
        )

        assertEquals("oldPass", request.currentPassword)
        assertEquals("newPass", request.newPassword)
    }

    @Test
    fun `order status should support admin states`() {
        val request = UpdateOrderStatusRequest("CANCELLED")

        assertTrue(request.status in listOf("CREATED", "IN_PROGRESS", "DONE", "CANCELLED"))
    }

    @Test
    fun `create order request without materials`() {
        val request = CreateOrderRequest(
            serviceId = 10L,
            pages = 100,
            quantity = 1,
            materials = null,
            email = "test@test.com"
        )

        assertNull(request.materials)
    }
}
