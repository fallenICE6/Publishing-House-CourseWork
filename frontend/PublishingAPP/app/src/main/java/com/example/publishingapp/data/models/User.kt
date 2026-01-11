package com.example.publishingapp.data.models

data class User(
    val id: Int,
    val username: String,
    val phone: String,
    val email: String? = null,
    val password: String,
    val first_name: String,
    val last_name: String,
    val middle_name: String = "",
    val role: UserRole
) {
    val fio: String
        get() = listOf(first_name, middle_name, last_name)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}
