package com.example.publishingapp.data.repository

import com.example.publishingapp.data.network.ApiClient
import com.example.publishingapp.data.network.ChangeRoleRequest
import com.example.publishingapp.data.network.UserDto
import com.example.publishingapp.data.network.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserManagementRepository {

    private val api = ApiClient.apiService

    // Загрузить первую порцию пользователей
    suspend fun getFirstUsers(search: String? = null): List<UserDto> = withContext(Dispatchers.IO) {
        val users = api.getAllUsers(page = 0, size = 20, search = search)
        return@withContext users.map { it.toDto() }
    }

    // Загрузить следующую порцию (для бесконечной прокрутки)
    suspend fun getNextUsers(page: Int, search: String? = null): List<UserDto> = withContext(Dispatchers.IO) {
        val users = api.getAllUsers(page = page, size = 20, search = search)
        return@withContext users.map { it.toDto() }
    }

    // Поиск пользователей (все результаты сразу)
    suspend fun searchUsers(username: String): List<UserDto> = withContext(Dispatchers.IO) {
        val users = api.searchUsers(username)
        return@withContext users.map { it.toDto() }
    }

    // Изменить роль пользователя
    suspend fun changeUserRole(userId: Long, role: String): UserDto = withContext(Dispatchers.IO) {
        val response = api.changeUserRole(userId, ChangeRoleRequest(role))
        return@withContext response.toDto()
    }

    // Получить пользователя по ID
    suspend fun getUserById(userId: Long): UserDto = withContext(Dispatchers.IO) {
        val response = api.getUserById(userId)
        return@withContext response.toDto()
    }

    // Конвертация UserResponse в UserDto
    private fun UserResponse.toDto(): UserDto {
        return UserDto(
            id = this.id,
            username = this.username,
            firstName = this.firstName,
            lastName = this.lastName,
            middleName = this.middleName,
            email = this.email,
            phone = this.phone,
            role = this.role
        )
    }
}