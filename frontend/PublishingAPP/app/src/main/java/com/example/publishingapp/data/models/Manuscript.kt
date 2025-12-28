package com.example.publishingapp.data.models

data class Manuscript(
    val id: Long,
    val title: String,
    val annotation: String,
    val pages: Int,
    val status: String,
    val filePath: String?   // ссылка в папке media
)
