package com.wadjet.core.data.repository

import com.wadjet.core.common.suspendRunCatching
import com.wadjet.core.data.datastore.UserPreferencesDataStore
import com.wadjet.core.database.WadjetDatabase
import com.wadjet.core.domain.model.User
import com.wadjet.core.domain.repository.AuthRepository
import com.wadjet.core.firebase.FcmTokenRegistrar
import com.wadjet.core.firebase.FirebaseAuthManager
import com.wadjet.core.firebase.WadjetAnalytics
import com.wadjet.core.network.TokenManager
import com.wadjet.core.network.api.AuthApiService
import com.wadjet.core.network.model.FirebaseAuthRequest
import com.wadjet.core.network.model.UserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A1 (Firebase-primary auth): Firebase Auth is the single identity source.
 * Credentials only ever go to Firebase; the backend session is derived by
 * exchanging the Firebase ID token at POST api/auth/firebase. Unverified
 * email accounts get NO backend session until the verification gate passes.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuthManager,
    private val authApi: AuthApiService,
    private val tokenManager: TokenManager,
    private val json: Json,
    private val database: WadjetDatabase,
    private val preferencesDataStore: UserPreferencesDataStore,
    private val fcmTokenRegistrar: FcmTokenRegistrar,
    private val analytics: WadjetAnalytics,
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // When the backend session dies for good, sign out Firebase to prevent split-brain state
        scope.launch {
            tokenManager.sessionInvalidated.collect {
                try { database.clearAllTables() } catch (e: Exception) { Timber.w(e, "Clear Room DB on session invalidation failed") }
                firebaseAuth.signOut()
            }
        }
    }

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
        get() = tokenManager.isLoggedIn

    override suspend fun signInWithGoogle(idToken: String): Result<User> = suspendRunCatching {
        firebaseAuth.signInWithGoogle(idToken)
        // Google emails are pre-verified — exchange immediately
        exchangeFirebaseSession().also { analytics.logLogin("google") }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> = suspendRunCatching {
        val firebaseUser = firebaseAuth.signInWithEmail(email, password)
        // B-03: the cached flag is stale right after sign-in — reload before gating
        val verified = firebaseUser.isEmailVerified || firebaseAuth.reloadAndIsEmailVerified()
        if (verified) {
            exchangeFirebaseSession().also { analytics.logLogin("password") }
        } else {
            // No backend session until the verification gate passes (establishBackendSession)
            firebaseUser.toDomain().copy(emailVerified = false)
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String?,
    ): Result<User> = suspendRunCatching {
        val firebaseUser = firebaseAuth.createAccount(email, password)

        // Best-effort profile + verification mail — account creation already succeeded
        if (!displayName.isNullOrBlank()) {
            try {
                firebaseAuth.updateDisplayName(displayName)
            } catch (e: Exception) {
                Timber.w(e, "Failed to set display name")
            }
        }
        try {
            firebaseAuth.sendEmailVerification()
        } catch (e: Exception) {
            Timber.w(e, "Failed to send verification email")
        }

        // New accounts are unverified — backend session comes after the gate
        analytics.logSignUp("password")
        firebaseUser.toDomain().copy(displayName = displayName ?: firebaseUser.displayName)
    }

    override suspend fun establishBackendSession(): Result<User> = suspendRunCatching {
        exchangeFirebaseSession()
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = suspendRunCatching {
        firebaseAuth.sendPasswordReset(email)
    }

    override suspend fun sendEmailVerification(): Result<Unit> = suspendRunCatching {
        firebaseAuth.sendEmailVerification()
    }

    override suspend fun reloadEmailVerified(): Result<Boolean> = suspendRunCatching {
        firebaseAuth.reloadAndIsEmailVerified()
    }

    override suspend fun signOut() {
        // A3: detach this device from push targeting while still authenticated
        try { fcmTokenRegistrar.unregisterCurrentToken() } catch (e: Exception) { Timber.w(e, "FCM unregister failed") }
        try { authApi.logout() } catch (e: Exception) { Timber.w(e, "Backend logout failed") }
        tokenManager.clearAll()
        try { database.clearAllTables() } catch (e: Exception) { Timber.w(e, "Clear Room DB failed") }
        firebaseAuth.signOut()
        analytics.setUser(null)
    }

    /**
     * Exchanges the current Firebase ID token for a backend session.
     * On explicit backend rejection the Firebase session is signed out to
     * prevent split-brain; on network errors the caller's Result captures the
     * exception and the Firebase session survives for a later retry.
     */
    private suspend fun exchangeFirebaseSession(): User {
        val idToken = firebaseAuth.getIdToken()
            ?: throw AuthException("Could not read Firebase credential")

        val response = authApi.firebaseAuth(FirebaseAuthRequest(idToken = idToken))
        if (response.isSuccessful) {
            val body = response.body()!!
            tokenManager.accessToken = body.accessToken
            // A3: attach this device's FCM token to the account (best-effort)
            scope.launch { fcmTokenRegistrar.registerCurrentToken() }
            // A4: associate analytics + crash reports with this user
            firebaseAuth.currentUser?.uid?.let { analytics.setUser(it) }
            return body.user?.toDomain()
                ?: firebaseAuth.currentUser?.toDomain()
                ?: throw AuthException("Sign-in returned no user")
        }

        // Backend explicitly refused the exchange — sign out Firebase (split-brain guard)
        firebaseAuth.signOut()
        if (response.code() == 429) {
            val retryAfter = response.headers()["Retry-After"]?.toLongOrNull() ?: 60L
            throw AuthException("Too many attempts. Try again in ${retryAfter}s")
        }
        val errorBody = response.errorBody()?.string()
        throw AuthException(parseError(errorBody) ?: "Sign-in failed")
    }

    private fun parseError(body: String?): String? {
        if (body == null) return null
        return try {
            json.parseToJsonElement(body).jsonObject["detail"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }
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
