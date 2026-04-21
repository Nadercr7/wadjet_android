package com.wadjet.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Authenticator
import okhttp3.Cookie
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
            val newToken = refreshToken(response)
            if (newToken != null) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } else {
                // Refresh failed — invalidate session (signals Firebase signout + local cleanup)
                tokenManager.invalidateSession()
                null
            }
        }
    }

    private fun refreshToken(response: Response): String? {
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
                body?.let { parseAccessToken(it) }?.also {
                    tokenManager.accessToken = it
                }
            } else {
                refreshResponse.close()
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Token refresh failed")
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
