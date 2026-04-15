package com.wadjet.core.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var tokenManager: TokenManager
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        tokenManager = mockk(relaxed = true)
        every { tokenManager.accessToken } returns "old-token"
        every { tokenManager.refreshToken } returns "refresh-token"

        val json = Json { ignoreUnknownKeys = true }
        val authenticator = TokenAuthenticator(
            tokenManager = tokenManager,
            baseUrl = server.url("/").toString(),
            json = json,
        )

        val interceptor = AuthInterceptor(
            tokenManager = tokenManager,
            baseUrl = server.url("/").toString(),
        )

        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .authenticator(authenticator)
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `401 triggers token refresh and retries`() {
        // Original request returns 401
        server.enqueue(MockResponse().setResponseCode(401))
        // Refresh request succeeds
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "new-token"}"""),
        )
        // Retry with new token succeeds
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data": "ok"}"""))

        every { tokenManager.accessToken } returnsMany listOf("old-token", "old-token", "new-token")

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        verify { tokenManager.accessToken = "new-token" }
    }

    @Test
    fun `failed refresh clears tokens`() {
        // Original request returns 401
        server.enqueue(MockResponse().setResponseCode(401))
        // Refresh request also fails
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        val response = client.newCall(request).execute()

        // Should propagate 401
        assertEquals(401, response.code)
        verify { tokenManager.clearAll() }
    }

    @Test
    fun `does not retry auth endpoints`() {
        // Auth endpoint returns 401 — should NOT trigger authenticator
        server.enqueue(MockResponse().setResponseCode(401))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/auth/login"))
            .post(okhttp3.RequestBody.create(null, "{}".toByteArray()))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(401, response.code)
        // Only 1 request — no refresh attempt
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `skips refresh if another thread already refreshed`() {
        // Original request returns 401
        server.enqueue(MockResponse().setResponseCode(401))
        // Retry with already-refreshed token succeeds
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data": "ok"}"""))

        // First call: AuthInterceptor sets "old-token". Second call: TokenAuthenticator sees new token.
        every { tokenManager.accessToken } returnsMany listOf("old-token", "already-refreshed-token")

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
        // 2 requests: original 401 + retry (no refresh call)
        assertEquals(2, server.requestCount)
    }

    @Test
    fun `saves refresh token from Set-Cookie`() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "new-token"}""")
                .addHeader("Set-Cookie", "wadjet_refresh=new-refresh; Path=/; HttpOnly"),
        )
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok": true}"""))

        every { tokenManager.accessToken } returnsMany listOf("old-token", "old-token", "new-token")

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        client.newCall(request).execute()

        verify { tokenManager.refreshToken = "new-refresh" }
    }
}
