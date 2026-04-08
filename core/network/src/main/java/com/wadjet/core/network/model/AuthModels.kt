package com.wadjet.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    @SerialName("display_name") val displayName: String? = null,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class GoogleAuthRequest(
    val credential: String,
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String = "bearer",
    val user: UserResponse? = null,
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("preferred_lang") val preferredLang: String = "en",
    val tier: String = "free",
    @SerialName("auth_provider") val authProvider: String = "email",
    @SerialName("email_verified") val emailVerified: Boolean = false,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
)

@Serializable
data class ErrorResponse(
    val detail: String,
)
