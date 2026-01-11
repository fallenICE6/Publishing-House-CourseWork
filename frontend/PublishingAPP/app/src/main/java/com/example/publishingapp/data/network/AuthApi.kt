package com.example.publishingapp.data.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AuthApi {

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): AuthResponse

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthResponse

    @GET("users/{username}")
    suspend fun getUserByUsername(
        @Path("username") username: String
    ): UserResponse

    @PUT("users/{username}")
    suspend fun updateUser(
        @Path("username") username: String,
        @Body request: UpdateUserRequest
    ): UserResponse

    @PUT("users/{username}/password")
    suspend fun changePassword(
        @Path("username") username: String,
        @Body request: ChangePasswordRequest
    )

}

