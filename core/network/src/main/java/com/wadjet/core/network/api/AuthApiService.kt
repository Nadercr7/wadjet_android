package com.wadjet.core.network.api

import com.wadjet.core.network.model.AuthResponse
import com.wadjet.core.network.model.ForgotPasswordRequest
import com.wadjet.core.network.model.GoogleAuthRequest
import com.wadjet.core.network.model.LoginRequest
import com.wadjet.core.network.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>

    @POST("api/auth/google")
    suspend fun googleAuth(@Body body: GoogleAuthRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refresh(): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): Response<Unit>
}
