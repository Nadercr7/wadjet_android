package com.wadjet.core.data.repository

import com.google.firebase.auth.FirebaseUser
import com.wadjet.core.data.datastore.UserPreferencesDataStore
import com.wadjet.core.database.WadjetDatabase
import com.wadjet.core.firebase.FcmTokenRegistrar
import com.wadjet.core.firebase.FirebaseAuthManager
import com.wadjet.core.firebase.WadjetAnalytics
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

/**
 * A1 (Firebase-primary auth): credentials go only to Firebase; the backend
 * session is derived by exchanging the Firebase ID token at api/auth/firebase.
 */
class AuthRepositoryImplTest {

    private lateinit var firebaseAuth: FirebaseAuthManager
    private lateinit var authApi: AuthApiService
    private lateinit var tokenManager: TokenManager
    private lateinit var json: Json
    private lateinit var database: WadjetDatabase
    private lateinit var preferencesDataStore: UserPreferencesDataStore
    private lateinit var fcmTokenRegistrar: FcmTokenRegistrar
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

    private val unverifiedFirebaseUser = mockk<FirebaseUser>(relaxed = true) {
        every { uid } returns "firebase-uid-2"
        every { email } returns "new@example.com"
        every { displayName } returns null
        every { photoUrl } returns null
        every { isEmailVerified } returns false
        every { providerData } returns listOf(
            mockk { every { providerId } returns "password" },
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
        fcmTokenRegistrar = mockk(relaxed = true)

        coEvery { firebaseAuth.getIdToken(any()) } returns "fb-id-token"

        repo = AuthRepositoryImpl(
            firebaseAuth, authApi, tokenManager, json, database, preferencesDataStore,
            fcmTokenRegistrar, mockk<WadjetAnalytics>(relaxed = true),
        )
    }

    private fun successExchange(user: UserResponse? = UserResponse(id = "u1", email = "test@example.com")) {
        coEvery { authApi.firebaseAuth(any()) } returns Response.success(
            AuthResponse(accessToken = "access-123", user = user),
        )
    }

    // --- signInWithGoogle ---

    @Test
    fun `signInWithGoogle exchanges Firebase token and stores backend session`() = runTest {
        coEvery { firebaseAuth.signInWithGoogle("id-token") } returns fakeFirebaseUser
        successExchange()

        val result = repo.signInWithGoogle("id-token")

        assertTrue(result.isSuccess)
        assertEquals("u1", result.getOrNull()?.id)
        verify { tokenManager.accessToken = "access-123" }
        coVerify(exactly = 1) { authApi.firebaseAuth(match { it.idToken == "fb-id-token" }) }
    }

    @Test
    fun `signInWithGoogle backend rejection signs out Firebase`() = runTest {
        coEvery { firebaseAuth.signInWithGoogle("id-token") } returns fakeFirebaseUser
        coEvery { authApi.firebaseAuth(any()) } returns Response.error(
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
    fun `signInWithEmail verified user exchanges and stores token`() = runTest {
        coEvery { firebaseAuth.signInWithEmail("a@b.com", "pass") } returns fakeFirebaseUser
        successExchange(user = null)

        val result = repo.signInWithEmail("a@b.com", "pass")

        assertTrue(result.isSuccess)
        verify { tokenManager.accessToken = "access-123" }
    }

    @Test
    fun `signInWithEmail unverified user gets NO backend session`() = runTest {
        coEvery { firebaseAuth.signInWithEmail("a@b.com", "pass") } returns unverifiedFirebaseUser
        coEvery { firebaseAuth.reloadAndIsEmailVerified() } returns false

        val result = repo.signInWithEmail("a@b.com", "pass")

        assertTrue(result.isSuccess)
        assertFalse(result.getOrNull()!!.emailVerified)
        coVerify(exactly = 0) { authApi.firebaseAuth(any()) }
        verify(exactly = 0) { tokenManager.accessToken = any() }
    }

    @Test
    fun `signInWithEmail stale flag reloads before gating (B-03)`() = runTest {
        // Cached user object says unverified, but the server-side reload says verified
        coEvery { firebaseAuth.signInWithEmail("a@b.com", "pass") } returns unverifiedFirebaseUser
        coEvery { firebaseAuth.reloadAndIsEmailVerified() } returns true
        successExchange()

        val result = repo.signInWithEmail("a@b.com", "pass")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { authApi.firebaseAuth(any()) }
    }

    @Test
    fun `signInWithEmail exchange rejection signs out Firebase (split-brain fix)`() = runTest {
        coEvery { firebaseAuth.signInWithEmail("a@b.com", "pass") } returns fakeFirebaseUser
        coEvery { authApi.firebaseAuth(any()) } returns Response.error(
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
            .request(okhttp3.Request.Builder().url("http://localhost/api/auth/firebase").build())
            .protocol(okhttp3.Protocol.HTTP_1_1)
            .code(429)
            .message("Too Many Requests")
            .header("Retry-After", "30")
            .body(rawBody)
            .build()
        val errorResponse = Response.error<AuthResponse>(rawBody, rawResponse)
        coEvery { authApi.firebaseAuth(any()) } returns errorResponse

        val result = repo.signInWithEmail("a@b.com", "pass")

        assertTrue(result.isFailure)
        val msg = result.exceptionOrNull()?.message ?: ""
        assertTrue("Should mention retry duration", msg.contains("30"))
        coVerify { firebaseAuth.signOut() }
    }

    // --- register ---

    @Test
    fun `register creates Firebase account only — no backend call until verified`() = runTest {
        coEvery { firebaseAuth.createAccount("a@b.com", "pass") } returns unverifiedFirebaseUser

        val result = repo.register("a@b.com", "pass", "Name")

        assertTrue(result.isSuccess)
        assertEquals("Name", result.getOrNull()?.displayName)
        assertFalse(result.getOrNull()!!.emailVerified)
        coVerify(exactly = 1) { firebaseAuth.sendEmailVerification() }
        coVerify(exactly = 1) { firebaseAuth.updateDisplayName("Name") }
        coVerify(exactly = 0) { authApi.firebaseAuth(any()) }
        verify(exactly = 0) { tokenManager.accessToken = any() }
    }

    // --- establishBackendSession ---

    @Test
    fun `establishBackendSession exchanges current Firebase identity`() = runTest {
        every { firebaseAuth.currentUser } returns fakeFirebaseUser
        successExchange()

        val result = repo.establishBackendSession()

        assertTrue(result.isSuccess)
        assertEquals("u1", result.getOrNull()?.id)
        verify { tokenManager.accessToken = "access-123" }
    }

    @Test
    fun `establishBackendSession fails when no Firebase credential available`() = runTest {
        coEvery { firebaseAuth.getIdToken(any()) } returns null

        val result = repo.establishBackendSession()

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { authApi.firebaseAuth(any()) }
    }

    // --- forgotPassword ---

    @Test
    fun `forgotPassword is Firebase-only`() = runTest {
        val result = repo.forgotPassword("a@b.com")

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { firebaseAuth.sendPasswordReset("a@b.com") }
    }

    // --- signOut ---

    @Test
    fun `signOut clears tokens and signs out Firebase`() = runTest {
        repo.signOut()

        verify { tokenManager.clearAll() }
        coVerify { firebaseAuth.signOut() }
    }
}
