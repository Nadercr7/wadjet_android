package com.wadjet.core.network

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    @Named("baseUrl") private val baseUrl: String,
) : Interceptor {

    private val mutex = Mutex()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Skip auth for auth endpoints (except refresh/logout)
        if (original.isAuthEndpoint()) {
            return handleAuthRequest(chain, original)
        }

        // Add access token
        val token = tokenManager.accessToken
        val request = if (token != null) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        val response = chain.proceed(request)

        // If 401, try to refresh
        if (response.code == 401 && token != null) {
            response.close()
            return handleTokenRefresh(chain, original, token)
        }

        return response
    }

    private fun handleAuthRequest(chain: Interceptor.Chain, request: Request): Response {
        // For refresh endpoint, send refresh token as cookie
        val modifiedRequest = if (request.url.encodedPath.endsWith("/auth/refresh")) {
            val refreshToken = tokenManager.refreshToken
            if (refreshToken != null) {
                request.newBuilder()
                    .header("Cookie", "wadjet_refresh=$refreshToken")
                    .build()
            } else request
        } else {
            request
        }

        val response = chain.proceed(modifiedRequest)

        // Extract refresh token from Set-Cookie headers
        if (response.isSuccessful) {
            extractAndSaveRefreshToken(response)
        }

        return response
    }

    private fun handleTokenRefresh(chain: Interceptor.Chain, original: Request, failedToken: String): Response {
        val newToken = runBlocking {
            mutex.withLock {
                // Check if another thread already refreshed
                val currentToken = tokenManager.accessToken
                if (currentToken != null && currentToken != failedToken) {
                    return@withLock currentToken
                }

                // Perform refresh
                try {
                    val refreshRequest = Request.Builder()
                        .url("${baseUrl}api/auth/refresh")
                        .post(okhttp3.RequestBody.create(null, ByteArray(0)))
                        .apply {
                            tokenManager.refreshToken?.let {
                                header("Cookie", "wadjet_refresh=$it")
                            }
                        }
                        .build()

                    // Use a new client call to avoid interceptor recursion
                    val refreshResponse = chain.proceed(refreshRequest)
                    if (refreshResponse.isSuccessful) {
                        val body = refreshResponse.body?.string()
                        extractAndSaveRefreshToken(refreshResponse)
                        refreshResponse.close()

                        // Parse access_token from JSON
                        body?.let { parseAccessToken(it) }?.also {
                            tokenManager.accessToken = it
                        }
                    } else {
                        refreshResponse.close()
                        tokenManager.clearAll()
                        null
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Token refresh failed")
                    tokenManager.clearAll()
                    null
                }
            }
        }

        return if (newToken != null) {
            val retryRequest = original.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
            chain.proceed(retryRequest)
        } else {
            // Return a 401 — auth state observer will redirect to login
            chain.proceed(original)
        }
    }

    private fun extractAndSaveRefreshToken(response: Response) {
        val cookies = Cookie.parseAll(
            response.request.url,
            response.headers,
        )
        cookies.find { it.name == "wadjet_refresh" }?.let {
            tokenManager.refreshToken = it.value
        }
    }

    private fun parseAccessToken(json: String): String? {
        // Simple extraction — avoids adding JSON dep to interceptor hot path
        val regex = """"access_token"\s*:\s*"([^"]+)"""".toRegex()
        return regex.find(json)?.groupValues?.get(1)
    }

    private fun Request.isAuthEndpoint(): Boolean {
        val path = url.encodedPath
        return path.contains("/auth/login") ||
            path.contains("/auth/register") ||
            path.contains("/auth/google") ||
            path.contains("/auth/refresh") ||
            path.contains("/auth/forgot-password")
    }
}
