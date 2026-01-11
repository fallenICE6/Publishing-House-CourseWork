package com.example.publishingapp.utils

import android.content.Context
import android.os.Environment
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


//ссылка на телефоне Файлы -> Хранилище -> Android -> data -> com.example.publishingapp

object FileDownloader {

    suspend fun downloadFile(
        context: Context,
        responseBody: ResponseBody,
        fileName: String
    ): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val downloadsDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "PublishingApp"
            )

            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            var file = File(downloadsDir, fileName)

            var index = 1
            while (file.exists()) {
                val nameWithoutExt = fileName.substringBeforeLast(".")
                val ext = fileName.substringAfterLast(".", "")
                file = File(downloadsDir, "${nameWithoutExt}_($index).$ext")
                index++
            }

            // Записываем данные
            saveResponseToFile(responseBody, file)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Файл сохранен: ${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Ошибка сохранения: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            false
        }
    }

    private fun saveResponseToFile(responseBody: ResponseBody, file: File): Boolean {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null

        return try {
            inputStream = responseBody.byteStream()
            outputStream = FileOutputStream(file)

            val buffer = ByteArray(4096)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.flush()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            inputStream?.close()
            outputStream?.close()
        }
    }

    fun getDownloadsPath(context: Context): String {
        return File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "PublishingApp"
        ).absolutePath
    }
}