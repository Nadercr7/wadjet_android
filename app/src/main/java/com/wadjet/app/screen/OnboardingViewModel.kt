package com.wadjet.app.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wadjet.core.data.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * U7: backs the first-run onboarding carousel. Its only job is to persist the
 * "seen" flag so the carousel is shown exactly once, before Welcome.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val prefs: UserPreferencesDataStore,
) : ViewModel() {
    fun markSeen() {
        viewModelScope.launch { prefs.setOnboardingSeen() }
    }
}
