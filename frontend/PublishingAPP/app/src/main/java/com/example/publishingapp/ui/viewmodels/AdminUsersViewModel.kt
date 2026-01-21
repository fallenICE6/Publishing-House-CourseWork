// ui/viewmodels/AdminUsersViewModel.kt
package com.example.publishingapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.publishingapp.data.network.UserDto
import com.example.publishingapp.data.repository.UserManagementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminUsersViewModel : ViewModel() {

    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users: StateFlow<List<UserDto>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private var currentPage = 0
    private var hasMoreUsers = true
    private var currentSearch: String? = null

    // Загрузить первую порцию пользователей
    fun loadInitialUsers(search: String? = null) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            currentSearch = search

            try {
                val result = UserManagementRepository.getFirstUsers(search)
                _users.value = result
                currentPage = 1
                hasMoreUsers = result.size == 20 // Если загрузили 20, значит есть еще
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Загрузить следующую порцию (при прокрутке)
    fun loadMoreUsers() {
        if (_isLoading.value || !hasMoreUsers) return

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = UserManagementRepository.getNextUsers(currentPage, currentSearch)
                if (result.isNotEmpty()) {
                    _users.value = _users.value + result
                    currentPage++
                    hasMoreUsers = result.size == 20
                } else {
                    hasMoreUsers = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Поиск пользователей (загружаем все сразу)
    fun searchUsers(username: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = UserManagementRepository.searchUsers(username)
                _users.value = result
                currentSearch = username
                currentPage = 0
                hasMoreUsers = false // При поиске не подгружаем дальше
            } catch (e: Exception) {
                _error.value = "Ошибка поиска: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Изменить роль пользователя
    fun changeUserRole(userId: Long, newRole: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val updatedUser = UserManagementRepository.changeUserRole(userId, newRole)

                // Обновляем пользователя в списке
                val updatedList = _users.value.toMutableList()
                val index = updatedList.indexOfFirst { it.id == userId }
                if (index != -1) {
                    updatedList[index] = updatedUser
                    _users.value = updatedList
                }

                // Устанавливаем сообщение для Toast
                _toastMessage.value = "Роль изменена на ${getRoleDisplayName(newRole)}"

            } catch (e: Exception) {
                _error.value = "Ошибка изменения роли: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    private fun getRoleDisplayName(role: String): String {
        return when (role) {
            "ADMIN" -> "Администратор"
            "AUTHOR" -> "Автор"
            "REVIEWER" -> "Рецензент"
            else -> role
        }
    }

    // Очищаем Toast сообщение после показа
    fun clearToastMessage() {
        _toastMessage.value = null
    }

    // Обновить список
    fun refresh() {
        currentPage = 0
        hasMoreUsers = true
        loadInitialUsers(currentSearch)
    }

    // Очистить ошибку
    fun clearError() {
        _error.value = null
    }
}