package com.example.publishingapp.data.mock

import com.example.publishingapp.data.models.User
import com.example.publishingapp.data.models.UserRole

object MockUserRepository {

    private val users = mutableListOf(
        User(
            id = 1,
            username = "author1",
            email = "author@mail.ru",
            phone = "+79990000000",
            password = "123456",
            first_name = "Иван",
            last_name = "Иванов",
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
