package com.example.publishingapp.data.repository

import com.example.publishingapp.data.models.Edition
import com.example.publishingapp.data.network.ApiService
import retrofit2.HttpException

class EditionRepository(private val api: ApiService) {

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
}