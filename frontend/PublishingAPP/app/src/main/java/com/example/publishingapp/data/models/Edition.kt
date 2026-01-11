package com.example.publishingapp.data.models

data class Edition(
    val id: Long,
    val title: String,
    val authorFirstName: String,
    val authorLastName: String,
    val authorMiddleName: String?,
    val fullAuthorName: String,
    val description: String?,
    val coverImage: String?,
    val genres: List<String>,
    val interiorImages: List<String>
)
 {
    val fio: String
        get() = listOf(authorFirstName, authorMiddleName ?: "", authorLastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
}