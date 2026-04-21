package com.wadjet.feature.auth

import com.wadjet.core.domain.model.User

data class AuthUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val forgotPasswordSent: Boolean = false,
    /** Email of the user who just registered and still needs to verify. Null when not pending. */
    val pendingVerificationEmail: String? = null,
    /** True after a verification email has just been resent (for transient UI feedback). */
    val verificationSent: Boolean = false,
    /** True when a reload check confirmed the email is still not verified. */
    val verificationCheckFailed: Boolean = false,
)

sealed interface AuthEvent {
    data object AuthSuccess : AuthEvent
    data class AuthError(val message: String) : AuthEvent
    data object ForgotPasswordSent : AuthEvent
    data object VerificationEmailSent : AuthEvent
    data object EmailVerified : AuthEvent
}

enum class AuthSheet { NONE, LOGIN, REGISTER, FORGOT_PASSWORD, VERIFY_EMAIL }
