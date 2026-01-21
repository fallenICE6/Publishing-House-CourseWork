package com.example.publishingapp.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServiceAndMaterialDtoTest {

    @Test
    fun `service dto should be created correctly`() {
        val service = ServiceDto(
            id = 1L,
            title = "Book printing",
            shortDescription = "Fast book printing",
            fullDescription = "High quality printing service",
            price = 5000.0,
            image = null,
            category = "PRINT"
        )

        assertEquals("Book printing", service.title)
        assertEquals(5000.0, service.price, 0.0)
        assertNull(service.image)
    }

    @Test
    fun `material dto should store price and category`() {
        val material = MaterialDto(
            id = 3L,
            name = "Glossy paper",
            category = "PAPER",
            price = 12.5
        )

        assertEquals("PAPER", material.category)
        assertEquals(12.5, material.price, 0.0)
    }
}
