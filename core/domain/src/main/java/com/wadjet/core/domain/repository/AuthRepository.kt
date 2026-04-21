package com.wadjet.core.domain.repository

import com.wadjet.core.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    val isLoggedIn: Boolean

    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithEmail(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, displayName: String?): Result<User>
    suspend fun forgotPassword(email: String): Result<Unit>

    /** Sends a verification email to the currently signed-in user. */
    suspend fun sendEmailVerification(): Result<Unit>

    /**
     * Reloads the Firebase user and returns whether the email is now verified.
     * Returns failure if no user is signed in.
     */
    suspend fun reloadEmailVerified(): Result<Boolean>

    suspend fun signOut()
}
