package com.example.publishingapp.data.network

object NetworkConstants {
    const val BASE_URL = "http://10.0.2.2:8080/"
    //const val BASE_URL = "http://192.168.0.108:8080/"

    fun getFullImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrEmpty()) return null

        return when {
            imagePath.startsWith("http") -> imagePath
            imagePath.startsWith("/uploads/") -> {
                // Если путь уже содержит /uploads/, убираем его чтобы не дублировать
                val cleanPath = imagePath.removePrefix("/uploads/")
                "$BASE_URL/uploads/$cleanPath"
            }
            imagePath.startsWith("/") -> {
                // Если путь начинается с /, убираем его чтобы не было двойного слеша
                val cleanPath = imagePath.removePrefix("/")
                "$BASE_URL$cleanPath"
            }
            else -> "$BASE_URL/uploads/$imagePath"
        }
    }
}