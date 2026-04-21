package com.wadjet.core.data.repository

import com.google.firebase.auth.FirebaseUser
import com.wadjet.core.data.datastore.UserPreferencesDataStore
import com.wadjet.core.database.WadjetDatabase
import com.wadjet.core.firebase.FirebaseAuthManager
import com.wadjet.core.network.TokenManager
import com.wadjet.core.network.api.AuthApiService
import com.wadjet.core.network.model.AuthResponse
import com.wadjet.core.network.model.UserResponse
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class AuthRepositoryImplTest {

    private lateinit var firebaseAuth: FirebaseAuthManager
    private lateinit var authApi: AuthApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var json: Json
    private lateinit var database: WadjetDatabase
    private lateinit var preferencesDataStore: UserPreferencesDataStore
    private lateinit var repo: AuthRepositoryImpl

    private val fakeFirebaseUser = mockk<FirebaseUser>(relaxed = true) {
        every { uid } returns "firebase-uid"
        every { email } returns "test@example.com"
        every { displayName } returns "Test User"
        every { photoUrl } returns null
        every { isEmailVerified } returns true
        every { providerData } returns listOf(
            mockk { every { providerId } returns "google.com" },
        )
    }

    @Before
    fun setup() {
        firebaseAuth = mockk(relaxed = true)
        authApi = mockk(relaxed = true)
        tokenManager = mockk(relaxed = true)
        every { tokenManager.sessionInvalidated } returns MutableSharedFlow<Unit>()
        json = Json { ignoreUnknownKeys = true }
        database = mockk(relaxed = true)
        preferencesDataStore = mockk(relaxed = true)

        repo = AuthRepositoryImpl(firebaseAuth, authApi, tokenManager, json, database, preferencesDataStore)
    }

    // --- signInWithGoogle ---

    @Test
    fun `signInWithGoogle success stores token and returns user`() = runTest {
        coEvery { firebaseAuth.signInWithGoogle("id-token") } returns fakeFirebaseUser
        coEvery { authApi.googleAuth(any()) } returns Response.success(
            AuthResponse(
                accessToken = "access-123",
                user = UserResponse(id = "u1", email = "test@example.com"),
            ),
        )

        val result = repo.signInWithGoogle("id-token")

        assertTrue(result.isSuccess)
        assertEquals("u1", result.getOrNull()?.id)
        verify { tokenManager.accessToken = "access-123" }
    }

    @Test
    fun `signInWithGoogle backend failure signs out Firebase`() = runTest {
        coEvery { firebaseAuth.signInWithGoogle("id-token") } returns fakeFirebaseUser
        coEvery { authApi.googleAuth(any()) } returns Response.error(
            500,
            """{"detail": "Backend error"}""".toResponseBody("application/json".toMediaType()),
        )

        val result = repo.signInWithGoogle("id-token")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Backend error") == true)
        coVerify { firebaseAuth.signOut() }
    }

    // --- signInWithEmail ---

    @Test
    fun `signInWithEmail success stores token`() = runTest {
        coEvery { firebaseAuth.signInWithEmail("a@b.com", "pass") } returns fakeFirebaseUser
        coEvery { authApi.login(any()) } returns Response.success(
            AuthResponse(accessToken = "tok-1"),
        )

        val result = repo.signInWithEmail("a@b.com", "pass")

        assertTrue(result.isSuccess)
        verify { tokenManager.accessToken = "tok-1" }
    }

    @Test
    fun `signInWithEmail backend failure signs out Firebase (split-brain fix)`() = runTest {
        coEvery { firebaseAuth.signInWithEmail("a@b.com", "pass") } returns fakeFirebaseUser
        coEvery { authApi.login(any()) } returns Response.error(
            403,
            """{"detail": "Account disabled"}""".toResponseBody("application/json".toMediaType()),
        )

        val result = repo.signInWithEmail("a@b.com", "pass")

        assertTrue(result.isFailure)
        coVerify { firebaseAuth.signOut() }
    }

    @Test
    fun `signInWithEmail 429 surfaces retry-after duration`() = runTest {
        coEvery { firebaseAuth.signInWithEmail("a@b.com", "pass") } returns fakeFirebaseUser

        val rawBody = """{"detail": "Rate limited"}""".toResponseBody("application/json".toMediaType())
        val rawResponse = okhttp3.Response.Builder()
            .request(okhttp3.Request.Builder().url("http://localhost/api/auth/login").build())
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(429)
            .message("Too Many Requests")
            .header("Retry-After", "30")
            .body(rawBody)
            .build()
        val errorResponse = Response.error<AuthResponse>(rawBody, rawResponse)
        coEvery { authApi.login(any()) } returns errorResponse

        val result = repo.signInWithEmail("a@b.com", "pass")

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message ?: ""
        assertTrue("Should mention retry duration", msg.contains("30"))
        coVerify { firebaseAuth.signOut() }
    }

    // --- register ---

    @Test
    fun `register success stores token`() = runTest {
        coEvery { firebaseAuth.createAccount("a@b.com", "pass") } returns fakeFirebaseUser
        coEvery { authApi.register(any()) } returns Response.success(
            AuthResponse(accessToken = "reg-tok"),
        )

        val result = repo.register("a@b.com", "pass", "Name")

        assertTrue(result.isSuccess)
        verify { tokenManager.accessToken = "reg-tok" }
    }

    @Test
    fun `register backend failure signs out Firebase (split-brain fix)`() = runTest {
        coEvery { firebaseAuth.createAccount("a@b.com", "pass") } returns fakeFirebaseUser
        coEvery { authApi.register(any()) } returns Response.error(
            409,
            """{"detail": "Email already exists"}""".toResponseBody("application/json".toMediaType()),
        )

        val result = repo.register("a@b.com", "pass", null)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Email already exists") == true)
        coVerify { firebaseAuth.signOut() }
    }

    // --- signOut ---

    @Test
    fun `signOut clears tokens and signs out Firebase`() = runTest {
        repo.signOut()

        verify { tokenManager.clearAll() }
        coVerify { firebaseAuth.signOut() }
    }
}
