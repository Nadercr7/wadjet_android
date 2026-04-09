package com.wadjet.core.data.repository

import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.domain.model.User
import com.wadjet.core.domain.repository.AuthRepository
import com.wadjet.core.firebase.FirebaseAuthManager
import com.wadjet.core.network.TokenManager
import com.wadjet.core.network.api.AuthApiService
import com.wadjet.core.network.model.GoogleAuthRequest
import com.wadjet.core.network.model.LoginRequest
import com.wadjet.core.network.model.ForgotPasswordRequest
import com.wadjet.core.network.model.RegisterRequest
import com.wadjet.core.network.model.UserResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuthManager,
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager,
) : AuthRepository {

    override val currentUser: Flow<User?> = firebaseAuth.authStateFlow.map { firebaseUser ->
        if (firebaseUser != null && tokenManager.isLoggedIn) {
            User(
                id = firebaseUser.uid,
                email = firebaseUser.email.orEmpty(),
                displayName = firebaseUser.displayName,
                avatarUrl = firebaseUser.photoUrl?.toString(),
                authProvider = firebaseUser.providerData.firstOrNull()?.providerId ?: "email",
                emailVerified = firebaseUser.isEmailVerified,
            )
        } else null
    }

    override val isLoggedIn: Boolean
        get() = firebaseAuth.currentUser != null && tokenManager.isLoggedIn

    override suspend fun signInWithGoogle(idToken: String): Result<User> = suspendRunCatching {
        // 1. Firebase sign-in
        val firebaseUser = firebaseAuth.signInWithGoogle(idToken)

        // 2. Sync with Wadjet backend
        val response = authApi.googleAuth(GoogleAuthRequest(credential = idToken))
        if (response.isSuccessful) {
            val body = response.body()!!
            tokenManager.accessToken = body.accessToken
            body.user?.toDomain() ?: firebaseUser.toDomain()
        } else {
            Timber.w("Backend google auth failed: ${response.code()}")
            // Still signed into Firebase, just no backend token
            firebaseUser.toDomain()
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> = suspendRunCatching {
        // 1. Firebase sign-in
        val firebaseUser = firebaseAuth.signInWithEmail(email, password)

        // 2. Sync with Wadjet backend
        val response = authApi.login(LoginRequest(email = email, password = password))
        if (response.isSuccessful) {
            val body = response.body()!!
            tokenManager.accessToken = body.accessToken
            body.user?.toDomain() ?: firebaseUser.toDomain()
        } else {
            val errorBody = response.errorBody()?.string()
            throw AuthException(parseError(errorBody) ?: "Login failed")
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String?,
    ): Result<User> = suspendRunCatching {
        // 1. Firebase account creation
        val firebaseUser = firebaseAuth.createAccount(email, password)

        // 2. Sync with Wadjet backend
        val response = authApi.register(
            RegisterRequest(email = email, password = password, displayName = displayName),
        )
        if (response.isSuccessful) {
            val body = response.body()!!
            tokenManager.accessToken = body.accessToken
            body.user?.toDomain() ?: firebaseUser.toDomain()
        } else {
            Timber.w("Backend register failed: ${response.code()}")
            firebaseUser.toDomain()
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = suspendRunCatching {
        firebaseAuth.sendPasswordReset(email)
        // Also notify backend (best-effort)
        try {
            authApi.forgotPassword(ForgotPasswordRequest(email = email))
        } catch (e: Exception) {
            Timber.w(e, "Backend forgot-password failed")
        }
    }

    override suspend fun signOut() {
        try { authApi.logout() } catch (e: Exception) { Timber.w(e, "Backend logout failed") }
        tokenManager.clearAll()
        firebaseAuth.signOut()
    }

    private fun parseError(body: String?): String? {
        if (body == null) return null
        val regex = """"detail"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(body)?.groupValues?.get(1)
    }
}

class AuthException(message: String) : Exception(message)

private fun UserResponse.toDomain() = User(
    id = id,
    email = email,
    displayName = displayName,
    preferredLang = preferredLang,
    tier = tier,
    authProvider = authProvider,
    emailVerified = emailVerified,
    avatarUrl = avatarUrl,
)

private fun com.google.firebase.auth.FirebaseUser.toDomain() = User(
    id = uid,
    email = email.orEmpty(),
    displayName = displayName,
    avatarUrl = photoUrl?.toString(),
    authProvider = providerData.firstOrNull()?.providerId ?: "email",
    emailVerified = isEmailVerified,
)
