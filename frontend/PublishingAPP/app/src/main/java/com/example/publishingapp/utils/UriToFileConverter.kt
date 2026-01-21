// UriToFileConverter.kt
package com.example.publishingapp.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object UriToFileConverter {

    fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open stream")

        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        return file
    }
}