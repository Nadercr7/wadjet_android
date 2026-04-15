package com.wadjet.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.data.datastore.UserPreferencesDataStore
import com.wadjet.core.domain.model.User
import com.wadjet.core.domain.repository.AuthRepository
import com.wadjet.core.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class SettingsUiState(
    val user: User? = null,
    val editName: String = "",
    val isEditingName: Boolean = false,
    val ttsEnabled: Boolean = true,
    val ttsSpeed: Float = 1.0f,
    val cacheSizeMb: Long = 0L,
    val currentPassword: String = "",
    val newPassword: String = "",
    val isChangingPassword: Boolean = false,
    val isSaving: Boolean = false,
    val message: String? = null,
    val signedOut: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val preferencesDataStore: UserPreferencesDataStore,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        observeUser()
        observePreferences()
    }

    fun startEditName() {
        _state.update { it.copy(isEditingName = true, editName = it.user?.displayName ?: "") }
    }

    fun updateEditName(name: String) {
        _state.update { it.copy(editName = name) }
    }

    fun saveName() {
        val name = _state.value.editName.trim()
        if (name.isBlank()) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            userRepository.updateProfile(displayName = name, preferredLang = null)
                .onSuccess {
                    _state.update { it.copy(isEditingName = false, isSaving = false, message = "Name updated") }
                }
                .onFailure { error ->
                    _state.update { it.copy(isSaving = false, message = error.message) }
                }
        }
    }

    fun cancelEditName() {
        _state.update { it.copy(isEditingName = false) }
    }

    fun updateCurrentPassword(password: String) {
        _state.update { it.copy(currentPassword = password) }
    }

    fun updateNewPassword(password: String) {
        _state.update { it.copy(newPassword = password) }
    }

    fun changePassword() {
        val current = _state.value.currentPassword
        val new = _state.value.newPassword
        if (current.isBlank() || new.length < 8) {
            _state.update { it.copy(message = "New password must be at least 8 characters") }
            return
        }
        if (_state.value.isChangingPassword) return
        _state.update { it.copy(isChangingPassword = true) }
        viewModelScope.launch {
            userRepository.changePassword(current, new)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isChangingPassword = false,
                            currentPassword = "",
                            newPassword = "",
                            message = "Password changed",
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(isChangingPassword = false, message = error.message) }
                }
        }
    }

    fun setTtsEnabled(enabled: Boolean) {
        _state.update { it.copy(ttsEnabled = enabled) }
        viewModelScope.launch { preferencesDataStore.setTtsEnabled(enabled) }
    }

    fun setTtsSpeed(speed: Float) {
        _state.update { it.copy(ttsSpeed = speed) }
        viewModelScope.launch { preferencesDataStore.setTtsSpeed(speed) }
    }

    fun setCacheSize(sizeBytes: Long) {
        _state.update { it.copy(cacheSizeMb = sizeBytes / (1024 * 1024)) }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
                _state.update { it.copy(signedOut = true) }
            } catch (e: Exception) {
                Timber.e(e, "Sign out failed")
                _state.update { it.copy(message = e.message ?: "Sign out failed") }
            }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun observeUser() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesDataStore.ttsEnabled.collect { enabled ->
                _state.update { it.copy(ttsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesDataStore.ttsSpeed.collect { speed ->
                _state.update { it.copy(ttsSpeed = speed) }
            }
        }
    }
}
