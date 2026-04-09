package com.wadjet.core.network

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AuthInterceptorTest {

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

        val interceptor = AuthInterceptor(
            tokenManager = tokenManager,
            baseUrl = server.url("/").toString(),
        )

        client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `adds bearer token to request`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertEquals("Bearer old-token", recorded.getHeader("Authorization"))
    }

    @Test
    fun `skips auth for login endpoint`() {
        every { tokenManager.accessToken } returns null
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/auth/login"))
            .post(okhttp3.RequestBody.create(null, "{}".toByteArray()))
            .build()
        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertEquals(null, recorded.getHeader("Authorization"))
    }

    @Test
    fun `401 triggers token refresh and retries`() {
        // First request returns 401
        server.enqueue(MockResponse().setResponseCode(401))

        // Refresh request succeeds
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "new-token"}""")
                .addHeader("Set-Cookie", "wadjet_refresh=new-refresh; Path=/; HttpOnly"),
        )

        // Retry with new token succeeds
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"data": "ok"}"""))

        every { tokenManager.accessToken } returnsMany listOf("old-token", "old-token", "new-token")

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)

        // 3 requests: original 401, refresh, retry
        assertEquals(3, server.requestCount)

        // Verify new token was saved
        verify { tokenManager.accessToken = "new-token" }
    }

    @Test
    fun `failed refresh clears tokens`() {
        // First request returns 401
        server.enqueue(MockResponse().setResponseCode(401))

        // Refresh request also fails
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))

        // Retry (with no token) returns 401
        server.enqueue(MockResponse().setResponseCode(401))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        client.newCall(request).execute()

        verify { tokenManager.clearAll() }
    }

    @Test
    fun `refresh endpoint sends cookie`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "refreshed"}"""),
        )

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/auth/refresh"))
            .post(okhttp3.RequestBody.create(null, "{}".toByteArray()))
            .build()
        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertEquals("wadjet_refresh=refresh-token", recorded.getHeader("Cookie"))
    }
}
