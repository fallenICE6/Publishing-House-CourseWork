package com.example.publishingapp.data.repository

import android.content.Context
import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.AppPrefs
import com.example.publishingapp.data.network.AuthResponse
import com.example.publishingapp.data.network.ChangePasswordRequest
import com.example.publishingapp.data.network.LoginRequest
import com.example.publishingapp.data.network.RegisterRequest
import com.example.publishingapp.data.network.UpdateUserRequest
import com.example.publishingapp.data.network.UserDto
import com.example.publishingapp.data.network.UserResponse
import com.example.publishingapp.data.storage.JwtStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AuthRepository {

    private val api = ApiClient.authApi
    private val prefs = AppPrefs

    val token: String?
        get() = prefs.getToken()

    var currentUser: UserDto? = prefs.getUser()
        private set

    /* ================= REGISTER ================= */

    suspend fun register(request: RegisterRequest) {
        val response = api.register(request)

        prefs.saveToken(response.token)

        val user = api.getUserByUsername(request.username)

        prefs.saveUser(user.toDto())
        currentUser = user.toDto()
    }


    /* ================= LOGIN ================= */

    suspend fun login(username: String, password: String) {
        val response = api.login(LoginRequest(username, password))

        prefs.saveToken(response.token)

        val user = api.getUserByUsername(username)

        prefs.saveUser(user.toDto())
        currentUser = user.toDto()
    }

    suspend fun updateProfile(request: UpdateUserRequest): UserDto = withContext(Dispatchers.IO) {
        val username = currentUser?.username ?: throw Exception("User not logged in")
        val updatedUser = api.updateUser(username, request)
        prefs.saveUser(updatedUser.toDto())
        currentUser = updatedUser.toDto()
        return@withContext updatedUser.toDto()
    }

    suspend fun changePassword(currentPassword: String, newPassword: String) {
        val username = currentUser?.username ?: throw Exception("User not logged in")
        api.changePassword(username, ChangePasswordRequest(currentPassword, newPassword))
    }


    private fun UserResponse.toDto() = UserDto(
        id = id.toLong(),
        username = username,
        firstName = firstName,
        lastName = lastName,
        middleName = middleName,
        email = email,
        phone = phone,
        role = role
    )

    fun isLoggedIn(): Boolean =
        token != null && currentUser != null

    fun logout() {
        prefs.clear()
        currentUser = null
    }
}




