package com.wadjet.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Authenticator
import okhttp3.Cookie
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.concurrent.withLock

/**
 * OkHttp Authenticator that transparently refreshes access tokens on 401 responses.
 * Uses a ReentrantLock (not runBlocking + Mutex) to synchronize without blocking
 * coroutine dispatchers.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    @Named("baseUrl") private val baseUrl: String,
    private val json: Json,
    private val firebaseIdTokenProvider: FirebaseIdTokenProvider,
) : Authenticator {

    private val lock = ReentrantLock()

    override fun authenticate(route: Route?, response: Response): Request? {
        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        // Don't retry auth endpoints to avoid infinite loops
        if (response.request.url.encodedPath.contains("/auth/")) return null

        // Give up after one retry to avoid infinite loops
        if (responseCount(response) > 1) return null

        return lock.withLock {
            // Check if another thread already refreshed
            val currentToken = tokenManager.accessToken
            if (currentToken != null && currentToken != failedToken) {
                // Token was already refreshed by another thread — just retry with new token
                return@withLock response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Perform refresh
            when (val outcome = refreshToken(response)) {
                is RefreshOutcome.Success -> response.request.newBuilder()
                    .header("Authorization", "Bearer ${outcome.token}")
                    .build()
                RefreshOutcome.Rejected -> {
                    // A1: before declaring the session dead, try to self-heal with a
                    // fresh Firebase ID token — while the Firebase session lives, the
                    // backend session is always recoverable.
                    val recovered = reExchangeWithFirebase()
                    if (recovered != null) {
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $recovered")
                            .build()
                    } else {
                        tokenManager.invalidateSession()
                        null
                    }
                }
                RefreshOutcome.NetworkError -> {
                    // D-08: transient network failure (offline/DNS/timeout) is NOT an auth
                    // rejection. Invalidating here silently signed users out whenever a
                    // 401->refresh raced a connectivity drop. Fail this request only.
                    null
                }
            }
        }
    }

    private sealed interface RefreshOutcome {
        data class Success(val token: String) : RefreshOutcome
        data object Rejected : RefreshOutcome
        data object NetworkError : RefreshOutcome
    }

    private fun refreshToken(response: Response): RefreshOutcome {
        return try {
            val refreshRequest = Request.Builder()
                .url("${baseUrl}api/auth/refresh")
                .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                .apply {
                    tokenManager.refreshToken?.let {
                        header("Cookie", "wadjet_refresh=$it")
                    }
                }
                .build()

            // Build a minimal client sharing connection pool but without auth interceptors
            val refreshClient = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val refreshResponse = refreshClient.newCall(refreshRequest).execute()

            if (refreshResponse.isSuccessful) {
                val body = refreshResponse.body?.string()
                extractAndSaveRefreshToken(refreshResponse)
                refreshResponse.close()
                val token = body?.let { parseAccessToken(it) }?.also {
                    tokenManager.accessToken = it
                }
                if (token != null) RefreshOutcome.Success(token) else RefreshOutcome.Rejected
            } else {
                val code = refreshResponse.code
                refreshResponse.close()
                // Only 401/403 mean the refresh token itself is invalid; a 5xx or
                // gateway error is the backend's problem, not a dead session.
                if (code == 401 || code == 403) RefreshOutcome.Rejected else RefreshOutcome.NetworkError
            }
        } catch (e: java.io.IOException) {
            Timber.w(e, "Token refresh failed (network) — keeping session")
            RefreshOutcome.NetworkError
        } catch (e: Exception) {
            Timber.e(e, "Token refresh failed (unexpected) — keeping session")
            RefreshOutcome.NetworkError
        }
    }

    /**
     * A1: exchanges a fresh Firebase ID token at api/auth/firebase for a new backend
     * session. Returns the new access token, or null when there is no Firebase user
     * or the exchange fails (caller then invalidates the session).
     */
    private fun reExchangeWithFirebase(): String? {
        return try {
            val idToken = firebaseIdTokenProvider.freshIdToken() ?: return null
            val payload = kotlinx.serialization.json.buildJsonObject {
                put("id_token", kotlinx.serialization.json.JsonPrimitive(idToken))
            }.toString()

            val request = Request.Builder()
                .url("${baseUrl}api/auth/firebase")
                .post(
                    okhttp3.RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        payload,
                    ),
                )
                .build()

            val client = okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val exchangeResponse = client.newCall(request).execute()

            if (exchangeResponse.isSuccessful) {
                val body = exchangeResponse.body?.string()
                extractAndSaveRefreshToken(exchangeResponse)
                exchangeResponse.close()
                body?.let { parseAccessToken(it) }?.also { tokenManager.accessToken = it }
            } else {
                Timber.w("Firebase re-exchange rejected (%d)", exchangeResponse.code)
                exchangeResponse.close()
                null
            }
        } catch (e: Exception) {
            Timber.w(e, "Firebase re-exchange failed")
            null
        }
    }

    private fun extractAndSaveRefreshToken(response: Response) {
        val cookies = Cookie.parseAll(response.request.url, response.headers)
        cookies.find { it.name == "wadjet_refresh" }?.let {
            tokenManager.refreshToken = it.value
        }
    }

    private fun parseAccessToken(jsonStr: String): String? {
        return try {
            json.parseToJsonElement(jsonStr).jsonObject["access_token"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse access token from refresh response")
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
