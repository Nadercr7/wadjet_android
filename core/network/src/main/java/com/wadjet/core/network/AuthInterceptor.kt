package com.wadjet.core.network

import okhttp3.Cookie
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Adds Authorization header and handles auth-endpoint specifics (refresh token cookie,
 * Set-Cookie extraction). Token refresh on 401 is handled by [TokenAuthenticator].
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    @Named("baseUrl") private val baseUrl: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Only apply auth to our own API — skip external URLs (e.g. Wikipedia images via Coil)
        val requestUrl = original.url.toString()
        if (!requestUrl.startsWith(baseUrl)) {
            return chain.proceed(original)
        }

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

        return chain.proceed(request)
    }

    private fun handleAuthRequest(chain: Interceptor.Chain, request: Request): Response {
        // Refresh needs the cookie to rotate; logout needs it so the server can
        // actually revoke the stored refresh token (D-04).
        val path = request.url.encodedPath
        val modifiedRequest = if (path.endsWith("/auth/refresh") || path.endsWith("/auth/logout")) {
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

    private fun extractAndSaveRefreshToken(response: Response) {
        val cookies = Cookie.parseAll(
            response.request.url,
            response.headers,
        )
        cookies.find { it.name == "wadjet_refresh" }?.let {
            tokenManager.refreshToken = it.value
        }
    }

    private fun Request.isAuthEndpoint(): Boolean {
        val path = url.encodedPath
        return path.contains("/auth/firebase") ||
            path.contains("/auth/refresh") ||
            path.contains("/auth/logout")
    }
}
