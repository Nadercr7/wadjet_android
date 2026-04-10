package com.wadjet.core.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles HTTP 429 (rate-limited) and 503 (overloaded) responses with retry.
 * - 429: respects Retry-After header, retries once. Detects login lockout.
 * - 503: exponential backoff up to 3 retries (1s, 2s, 4s).
 */
@Singleton
class RateLimitInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)

        // Handle 429 – rate limited
        if (response.code == 429) {
            val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: 2L
            val waitMs = (retryAfter * 1000).coerceAtMost(30_000)
            val path = request.url.encodedPath

            // Detect login lockout
            if (path.contains("/auth/login")) {
                Timber.w("Login lockout detected (429). Locked for ${retryAfter}s")
                // Return the 429 immediately — don't retry login lockouts
                return response
            }

            Timber.w("Rate limited (429) on $path. Retry-After: ${retryAfter}s")
            response.close()
            Thread.sleep(waitMs)
            response = chain.proceed(request)
        }

        // Handle 503 – exponential backoff, up to 3 retries
        var attempt = 0
        while (response.code == 503 && attempt < 3) {
            val backoffMs = (1000L shl attempt).coerceAtMost(8000)
            Timber.w("Service unavailable (503). Retry ${attempt + 1}/3, backoff ${backoffMs}ms")
            response.close()
            Thread.sleep(backoffMs)
            response = chain.proceed(request)
            attempt++
        }

        return response
    }
}
