package com.wadjet.core.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles HTTP 429 (rate-limited) and 503 (overloaded) responses.
 * Logs the event and returns the error response to the caller.
 * Retry logic is handled at the repository/ViewModel layer using coroutine delay().
 */
@Singleton
class RateLimitInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 429) {
            val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: 2L
            val path = request.url.encodedPath
            if (path.contains("/auth/login")) {
                Timber.w("Login lockout detected (429). Locked for ${retryAfter}s")
            } else {
                Timber.w("Rate limited (429) on $path. Retry-After: ${retryAfter}s")
            }
        }

        if (response.code == 503) {
            Timber.w("Service unavailable (503) on ${request.url.encodedPath}")
        }

        return response
    }
}
