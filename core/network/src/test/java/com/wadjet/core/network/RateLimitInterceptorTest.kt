package com.wadjet.core.network

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RateLimitInterceptorTest {

    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()

        client = OkHttpClient.Builder()
            .addInterceptor(RateLimitInterceptor())
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `passes through 200 response unchanged`() {
        server.enqueue(MockResponse().setResponseCode(200).setBody("""{"ok": true}"""))

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(200, response.code)
    }

    @Test
    fun `passes through 429 without blocking`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Retry-After", "5")
                .setBody("""{"detail": "Rate limited"}"""),
        )

        val start = System.currentTimeMillis()
        val request = okhttp3.Request.Builder()
            .url(server.url("/api/dictionary/signs"))
            .build()
        val response = client.newCall(request).execute()
        val elapsed = System.currentTimeMillis() - start

        assertEquals(429, response.code)
        // Must not sleep — should complete in well under 1 second
        assert(elapsed < 1000) { "RateLimitInterceptor should not sleep, took ${elapsed}ms" }
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `passes through 503 without blocking`() {
        server.enqueue(MockResponse().setResponseCode(503).setBody("Service Unavailable"))

        val start = System.currentTimeMillis()
        val request = okhttp3.Request.Builder()
            .url(server.url("/api/chat/ask"))
            .build()
        val response = client.newCall(request).execute()
        val elapsed = System.currentTimeMillis() - start

        assertEquals(503, response.code)
        assert(elapsed < 1000) { "RateLimitInterceptor should not sleep, took ${elapsed}ms" }
    }

    @Test
    fun `login lockout 429 is not retried`() {
        server.enqueue(
            MockResponse()
                .setResponseCode(429)
                .addHeader("Retry-After", "60")
                .setBody("""{"detail": "Too many login attempts"}"""),
        )

        val request = okhttp3.Request.Builder()
            .url(server.url("/api/auth/login"))
            .post(okhttp3.RequestBody.create(null, "{}".toByteArray()))
            .build()
        val response = client.newCall(request).execute()

        assertEquals(429, response.code)
        assertEquals(1, server.requestCount)
    }
}
