package com.example.publishingapp.data.mock

import com.example.publishingapp.data.models.User
import com.example.publishingapp.data.models.UserRole

object MockUserRepository {

    private val users = mutableListOf(
        User(
            id = 1,
            username = "123",
            email = "author@mail.ru",
            phone = "+79990000000",
            password = "123",
            first_name = "Иван",
            last_name = "Иванов",
            middle_name = "Иванович",
            role = UserRole.AUTHOR
        )
    )

    var currentUser: User? = null
        private set

    fun login(username: String, password: String): Boolean {
        val user = users.find {
            it.username == username && it.password == password
        }
        currentUser = user
        return user != null
    }

    fun changePassword(currentPassword: String, newPassword: String): Boolean {
        val user = currentUser ?: return false
        if (user.password != currentPassword) return false
        currentUser = user.copy(password = newPassword)
        return true
    }


    fun updateProfile(
        first: String,
        last: String,
        middle: String,
        email: String,
        phone: String
    ) {
        currentUser = currentUser?.copy(
            first_name = first,
            last_name = last,
            middle_name = middle,
            email = email,
            phone = phone
        )
    }

    fun register(user: User): Boolean {
        if (users.any { it.username == user.username }) return false
        users.add(user)
        currentUser = user
        return true
    }

    fun logout() {
        currentUser = null
    }

    fun isLoggedIn(): Boolean = currentUser != null
}
