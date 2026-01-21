package com.example.publishingapp.data.network

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class OrderDtoTest {

    @Test
    fun `order dto should be created correctly`() {
        val order = OrderDto(
            id = 100L,
            userId = 1L,
            serviceId = 5L,
            totalPrice = 12000.0,
            pages = 200,
            quantity = 1,
            status = "CREATED",
            files = listOf("file1.pdf", "file2.docx")
        )

        assertEquals(1L, order.userId)
        assertEquals("CREATED", order.status)
        assertEquals(2, order.files.size)
    }

    @Test
    fun `create order request with optional fields`() {
        val request = CreateOrderRequest(
            serviceId = 5L,
            pages = null,
            quantity = 10,
            materials = null,
            email = "client@test.com"
        )

        assertEquals(10, request.quantity)
        assertNull(request.pages)
    }

    @Test
    fun `full order dto should contain materials and files`() {
        val material = OrderMaterialDto(
            name = "Hard cover",
            category = "COVER",
            quantity = 1,
            price = BigDecimal("1500.00")
        )

        val file = OrderFileDto(
            id = 1L,
            fileName = "book.pdf",
            fileType = "application/pdf",
            downloadUrl = "http://localhost/media/book.pdf"
        )

        val order = OrderFullDto(
            id = 1L,
            fullName = "Ivan Ivanov",
            email = "ivan@test.com",
            phone = "+79990000000",
            serviceTitle = "Book printing",
            pages = 300,
            quantity = 2,
            materials = listOf(material),
            files = listOf(file),
            review = null,
            totalPrice = 20000.0,
            status = "IN_PROGRESS",
            createdAt = "2024-05-01T10:00:00"
        )

        assertEquals("IN_PROGRESS", order.status)
        assertEquals(1, order.materials.size)
        assertEquals("Hard cover", order.materials[0].name)
    }

    @Test
    fun `update order status request should store status`() {
        val request = UpdateOrderStatusRequest(
            status = "DONE"
        )

        assertEquals("DONE", request.status)
    }
}
