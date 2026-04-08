package com.wadjet.feature.auth

import com.wadjet.core.domain.model.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val forgotPasswordSent: Boolean = false,
)

sealed interface AuthEvent {
    data object AuthSuccess : AuthEvent
    data class AuthError(val message: String) : AuthEvent
    data object ForgotPasswordSent : AuthEvent
}

enum class AuthSheet { NONE, LOGIN, REGISTER, FORGOT_PASSWORD }
