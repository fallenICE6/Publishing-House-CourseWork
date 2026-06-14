package com.example.publishingapp.data.network

object NetworkConstants {
    //const val BASE_URL = "http://10.0.2.2:8080/"
    const val BASE_URL = "http://192.168.0.102:8080/"

    fun getFullImageUrl(imagePath: String?): String? {
        if (imagePath.isNullOrBlank()) return null

        return when {
            imagePath.startsWith("http") -> imagePath
            imagePath.startsWith("/") -> BASE_URL.removeSuffix("/") + imagePath
            else -> BASE_URL.removeSuffix("/") + "/uploads/" + imagePath
        }
    }
}