package com.example.publishingapp.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.data.network.ApiService
import com.example.publishingapp.data.network.EditionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream

class EditionRepository(private val api: ApiService, private val context: Context) {

    suspend fun getAllEditions(): List<Edition> {
        return try {
            api.getAllEditions()
        } catch (e: HttpException) {
            emptyList()
        }
    }

    suspend fun getEditionById(id: Long): Edition? {
        return try {
            api.getEditionById(id)
        } catch (e: HttpException) {
            null
        }
    }

    suspend fun getEditionsByGenre(genre: String): List<Edition> {
        return try {
            api.getEditionsByGenre(genre)
        } catch (e: HttpException) {
            emptyList()
        }
    }

    suspend fun getAllGenres(): List<String> {
        return try {
            api.getAllGenres()
        } catch (e: HttpException) {
            emptyList()
        }
    }

    suspend fun searchEditions(query: String): List<Edition> {
        return try {
            api.searchEditions(query)
        } catch (e: HttpException) {
            emptyList()
        }
    }

    suspend fun uploadFiles(
        coverUri: Uri?,
        interiorUris: List<Uri>?
    ): Pair<String?, List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val coverPart = coverUri?.let { uriToMultipartPart(it, "cover") }
                val interiorParts = interiorUris?.mapNotNull { uriToMultipartPart(it, "images") }

                println("DEBUG: Uploading files - cover: ${coverUri != null}, interior: ${interiorUris?.size ?: 0}")

                val urls = api.uploadFiles(coverPart, interiorParts)

                println("DEBUG: Upload response: $urls")

                val coverUrl = if (coverUri != null) urls.firstOrNull() else null
                val interiorUrls = if (interiorUris != null && interiorUris.isNotEmpty()) {
                    val startIndex = if (coverUri != null) 1 else 0
                    if (startIndex < urls.size) urls.subList(startIndex, urls.size) else emptyList()
                } else emptyList()

                Pair(coverUrl, interiorUrls)
            } catch (e: Exception) {
                println("DEBUG: Upload error: ${e.message}")
                throw e
            }
        }
    }

    private fun uriToMultipartPart(uri: Uri, partName: String): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null

            // Создаем временный файл
            val tempFile = File.createTempFile("temp_${System.currentTimeMillis()}", ".jpg", context.cacheDir)
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()

            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            val fileName = getFileNameFromUri(uri)
            MultipartBody.Part.createFormData(partName, fileName, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "image_${System.currentTimeMillis()}.jpg"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1) {
                    cursor.getString(displayNameIndex)?.let { name ->
                        fileName = name
                    }
                }
            }
        }
        return fileName
    }

    suspend fun createEdition(request: EditionRequest): Edition {
        return try {
            api.createEdition(request)
        } catch (e: HttpException) {
            throw Exception("Ошибка создания: ${e.message}")
        }
    }

    suspend fun updateEdition(id: Long, request: EditionRequest): Edition {
        return try {
            api.updateEdition(id, request)
        } catch (e: HttpException) {
            throw Exception("Ошибка обновления: ${e.message}")
        }
    }

    suspend fun deleteEdition(id: Long): Boolean {
        return try {
            api.deleteEdition(id)
            true
        } catch (e: HttpException) {
            false
        }
    }


}