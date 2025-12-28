package com.example.publishingapp.data.models

data class Edition(
    val id: Int,
    val title: String,
    val firstName: String,
    val lastName: String,
    val middleName: String = "",
    val description: String,
    val imageName: String,
    val genres: List<String>,
    val interiorImages: List<String> = emptyList()
) {
    val fio: String
        get() = listOf(firstName, middleName, lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}
