package com.wadjet.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Patterns
import com.wadjet.core.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    private val _activeSheet = MutableStateFlow(AuthSheet.NONE)
    val activeSheet: StateFlow<AuthSheet> = _activeSheet.asStateFlow()

    fun showSheet(sheet: AuthSheet) {
        _activeSheet.value = sheet
        _state.update { it.copy(error = null, forgotPasswordSent = false, verificationSent = false, verificationCheckFailed = false) }
    }

    fun dismissSheet() {
        _activeSheet.value = AuthSheet.NONE
        _state.update { it.copy(error = null, forgotPasswordSent = false, verificationSent = false, verificationCheckFailed = false) }
    }

    fun signInWithGoogle(idToken: String) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithGoogle(idToken)
                .onSuccess { user ->
                    _state.update { it.copy(isLoading = false, user = user) }
                    _activeSheet.value = AuthSheet.NONE
                    _events.emit(AuthEvent.AuthSuccess)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Google sign-in failed") }
                    _events.emit(AuthEvent.AuthError(e.message ?: "Google sign-in failed"))
                }
        }
    }

    fun onGoogleSignInError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun signInWithEmail(email: String, password: String) {
        if (!validateEmail(email) || password.isBlank()) {
            _state.update { it.copy(error = "Please enter a valid email and password") }
            return
        }
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithEmail(email.trim(), password)
                .onSuccess { user ->
                    if (!user.emailVerified) {
                        // Stop at verification gate — don't emit AuthSuccess yet
                        _state.update {
                            it.copy(
                                isLoading = false,
                                user = user,
                                pendingVerificationEmail = user.email,
                            )
                        }
                        _activeSheet.value = AuthSheet.VERIFY_EMAIL
                    } else {
                        _state.update { it.copy(isLoading = false, user = user, pendingVerificationEmail = null) }
                        _activeSheet.value = AuthSheet.NONE
                        _events.emit(AuthEvent.AuthSuccess)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
                }
        }
    }

    fun register(email: String, password: String, confirmPassword: String, displayName: String?) {
        if (!validateEmail(email)) {
            _state.update { it.copy(error = "Please enter a valid email") }
            return
        }
        val pwError = validatePassword(password)
        if (pwError != null) {
            _state.update { it.copy(error = pwError) }
            return
        }
        if (password != confirmPassword) {
            _state.update { it.copy(error = "Passwords do not match") }
            return
        }
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.register(email.trim(), password, displayName?.trim()?.ifBlank { null })
                .onSuccess { user ->
                    // New accounts are always unverified — gate AuthSuccess on verification.
                    _state.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            pendingVerificationEmail = user.email,
                            verificationSent = true,
                        )
                    }
                    _activeSheet.value = AuthSheet.VERIFY_EMAIL
                    _events.emit(AuthEvent.VerificationEmailSent)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Registration failed") }
                }
        }
    }

    fun resendVerification() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, verificationSent = false, verificationCheckFailed = false) }
            authRepository.sendEmailVerification()
                .onSuccess {
                    _state.update { it.copy(isLoading = false, verificationSent = true) }
                    _events.emit(AuthEvent.VerificationEmailSent)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to send verification email") }
                }
        }
    }

    fun checkEmailVerified() {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, verificationCheckFailed = false, verificationSent = false) }
            authRepository.reloadEmailVerified()
                .onSuccess { verified ->
                    if (verified) {
                        val current = _state.value.user?.copy(emailVerified = true)
                        _state.update {
                            it.copy(
                                isLoading = false,
                                user = current,
                                pendingVerificationEmail = null,
                                verificationCheckFailed = false,
                            )
                        }
                        _activeSheet.value = AuthSheet.NONE
                        _events.emit(AuthEvent.EmailVerified)
                        _events.emit(AuthEvent.AuthSuccess)
                    } else {
                        _state.update { it.copy(isLoading = false, verificationCheckFailed = true) }
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to check verification") }
                }
        }
    }

    /** User cancels the verification flow — sign out to avoid a half-authenticated local session. */
    fun cancelVerification() {
        viewModelScope.launch {
            authRepository.signOut()
            _state.update {
                AuthUiState()
            }
            _activeSheet.value = AuthSheet.NONE
        }
    }

    fun forgotPassword(email: String) {
        if (!validateEmail(email)) {
            _state.update { it.copy(error = "Please enter a valid email") }
            return
        }
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.forgotPassword(email.trim())
                .onSuccess {
                    _state.update { it.copy(isLoading = false, forgotPasswordSent = true) }
                    _events.emit(AuthEvent.ForgotPasswordSent)
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to send reset email") }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    companion object {
        fun validateEmail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
        }

        fun validatePassword(password: String): String? {
            if (password.length < 8) return "Password must be at least 8 characters"
            if (!password.any { it.isUpperCase() }) return "Password must contain an uppercase letter"
            if (!password.any { it.isLowerCase() }) return "Password must contain a lowercase letter"
            if (!password.any { it.isDigit() }) return "Password must contain a digit"
            return null
        }

        fun passwordStrength(password: String): Float {
            if (password.isEmpty()) return 0f
            var score = 0
            if (password.length >= 8) score++
            if (password.any { it.isUpperCase() }) score++
            if (password.any { it.isLowerCase() }) score++
            if (password.any { it.isDigit() }) score++
            if (password.length >= 12) score++
            return score / 5f
        }
    }
}
