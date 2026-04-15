package com.wadjet.core.network

import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        every { tokenManager.accessToken } returns "test-token"
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
    fun `adds bearer token to API request`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertEquals("Bearer test-token", recorded.getHeader("Authorization"))
    }

    @Test
    fun `skips auth header when token is null`() {
        every { tokenManager.accessToken } returns null
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertNull(recorded.getHeader("Authorization"))
    }

    @Test
    fun `skips auth for login endpoint`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/auth/login"))
            .post(okhttp3.RequestBody.create(null, "{}".toByteArray()))
            .build()
        client.newCall(request).execute()

        val recorded = server.takeRequest()
        // Auth endpoints go through handleAuthRequest, no Bearer header
        assertNull(recorded.getHeader("Authorization"))
    }

    @Test
    fun `skips auth for external URLs`() {
        val externalServer = MockWebServer()
        externalServer.start()
        externalServer.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val request = okhttp3.Request.Builder()
            .url(externalServer.url("/some/image.png"))
            .build()
        client.newCall(request).execute()

        val recorded = externalServer.takeRequest()
        assertNull(recorded.getHeader("Authorization"))
        externalServer.shutdown()
    }

    @Test
    fun `refresh endpoint sends cookie`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"access_token": "new"}"""))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/auth/refresh"))
            .post(okhttp3.RequestBody.create(null, "{}".toByteArray()))
            .build()
        client.newCall(request).execute()

        val recorded = server.takeRequest()
        assertEquals("wadjet_refresh=refresh-token", recorded.getHeader("Cookie"))
    }

    @Test
    fun `401 is passed through without retry`() {
        // AuthInterceptor no longer handles 401 — TokenAuthenticator does
        server.enqueue(MockResponse().setResponseCode(401))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(401, response.code)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `extracts refresh token from Set-Cookie on auth success`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"access_token": "new"}""")
                .addHeader("Set-Cookie", "wadjet_refresh=new-refresh; Path=/; HttpOnly"),
        )

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/auth/login"))
            .post(okhttp3.RequestBody.create(null, "{}".toByteArray()))
            .build()
        client.newCall(request).execute()

        io.mockk.verify { tokenManager.refreshToken = "new-refresh" }
    }
}
