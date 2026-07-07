package com.wadjet.feature.auth

import androidx.lifecycle.ViewModel
import com.wadjet.core.common.StringResolver
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
    private val strings: StringResolver,
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
                    _state.update { it.copy(isLoading = false, error = e.message ?: strings.get(R.string.error_google_sign_in_failed)) }
                    _events.emit(AuthEvent.AuthError(e.message ?: strings.get(R.string.error_google_sign_in_failed)))
                }
        }
    }

    fun onGoogleSignInError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun signInWithEmail(email: String, password: String) {
        if (!validateEmail(email) || password.isBlank()) {
            _state.update { it.copy(error = strings.get(R.string.auth_error_invalid_email_password)) }
            return
        }
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.signInWithEmail(email.trim(), password)
                .onSuccess { user ->
                    // A1: the repository reloads the verification flag (B-03) and only
                    // establishes a backend session for verified accounts.
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
                    _state.update { it.copy(isLoading = false, error = e.message ?: strings.get(R.string.auth_error_login_failed)) }
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
            _state.update { it.copy(error = strings.get(pwError)) }
            return
        }
        if (password != confirmPassword) {
            _state.update { it.copy(error = strings.get(R.string.register_password_mismatch)) }
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
                    _state.update { it.copy(isLoading = false, error = e.message ?: strings.get(R.string.auth_error_registration_failed)) }
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
                    _state.update { it.copy(isLoading = false, error = e.message ?: strings.get(R.string.auth_error_send_verification)) }
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
                        // A1: the backend session is only created now, after verification
                        authRepository.establishBackendSession()
                            .onSuccess { user ->
                                _state.update {
                                    it.copy(
                                        isLoading = false,
                                        user = user,
                                        pendingVerificationEmail = null,
                                        verificationCheckFailed = false,
                                    )
                                }
                                _activeSheet.value = AuthSheet.NONE
                                _events.emit(AuthEvent.EmailVerified)
                                _events.emit(AuthEvent.AuthSuccess)
                            }
                            .onFailure { e ->
                                _state.update { it.copy(isLoading = false, error = e.message ?: strings.get(R.string.auth_error_login_failed)) }
                            }
                    } else {
                        _state.update { it.copy(isLoading = false, verificationCheckFailed = true) }
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: strings.get(R.string.auth_error_check_verification)) }
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
                    _state.update { it.copy(isLoading = false, error = e.message ?: strings.get(R.string.auth_error_send_reset)) }
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

        /** @return a string resource id describing the violated rule, or null when valid (G-04). */
        fun validatePassword(password: String): Int? {
            if (password.length < 8) return R.string.auth_error_password_length
            if (!password.any { it.isUpperCase() }) return R.string.auth_error_password_uppercase
            if (!password.any { it.isLowerCase() }) return R.string.auth_error_password_lowercase
            if (!password.any { it.isDigit() }) return R.string.auth_error_password_digit
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
