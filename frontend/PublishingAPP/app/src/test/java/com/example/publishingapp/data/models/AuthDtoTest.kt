package com.example.publishingapp.data.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthDtoTest {

    @Test
    fun `register request should be created correctly`() {
        val request = RegisterRequest(
            username = "author1",
            phone = "+79990000000",
            email = "author@test.com",
            password = "password123",
            firstName = "Ivan",
            lastName = "Ivanov",
            middleName = null
        )

        assertEquals("author1", request.username)
        assertEquals("+79990000000", request.phone)
        assertEquals("Ivan", request.firstName)
        assertNull(request.middleName)
    }

    @Test
    fun `login request should store credentials`() {
        val request = LoginRequest(
            username = "admin",
            password = "admin123"
        )

        assertEquals("admin", request.username)
        assertEquals("admin123", request.password)
    }

    @Test
    fun `auth response should contain token and user`() {
        val user = UserDto(
            id = 1L,
            username = "author",
            firstName = "Ivan",
            lastName = "Ivanov",
            middleName = null,
            email = "author@test.com",
            phone = "+79990000000",
            role = "AUTHOR"
        )

        val response = AuthResponse(
            token = "jwt.token.here",
            user = user
        )

        assertEquals("jwt.token.here", response.token)
        assertEquals("AUTHOR", response.user.role)
    }

    @Test
    fun `user response dto should map correctly`() {
        val user = UserResponse(
            id = 10,
            username = "admin",
            phone = "+78888888888",
            email = null,
            firstName = "Admin",
            lastName = "Root",
            middleName = null,
            role = "ADMIN"
        )

        assertEquals("ADMIN", user.role)
        assertNull(user.email)
    }
}
