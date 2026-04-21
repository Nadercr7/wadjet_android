package com.wadjet.feature.auth

import android.util.Patterns
import app.cash.turbine.test
import com.wadjet.core.domain.model.User
import com.wadjet.core.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.regex.Pattern

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var vm: AuthViewModel

    private val fakeUser = User(
        id = "u1",
        email = "test@example.com",
        displayName = "Test",
        emailVerified = true,
    )

    private val unverifiedUser = User(
        id = "u1",
        email = "test@example.com",
        displayName = "Test",
        emailVerified = false,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Patterns.EMAIL_ADDRESS is null in JVM unit tests (Android stub).
        // Use Unsafe to set the static final field on JDK 17+.
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe = unsafeField.get(null)
        val emailField = Patterns::class.java.getDeclaredField("EMAIL_ADDRESS")
        val offset = unsafeClass.getMethod("staticFieldOffset", java.lang.reflect.Field::class.java)
            .invoke(unsafe, emailField) as Long
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+",
        )
        unsafeClass.getMethod("putObject", Any::class.java, Long::class.javaPrimitiveType, Any::class.java)
            .invoke(unsafe, Patterns::class.java, offset, emailPattern)

        authRepository = mockk(relaxed = true) {
            every { currentUser } returns flowOf(null)
        }
        vm = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Double-submit guard ---

    @Test
    fun `signInWithEmail ignores duplicate calls while loading`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            Result.success(fakeUser)
        }

        // Fire both calls before advancing — the second should be ignored
        vm.signInWithEmail("test@example.com", "Password1")
        // First call enters the coroutine but hasn't completed due to StandardTestDispatcher
        // The _state.isLoading is set to true inside the launch BEFORE the suspend call
        testDispatcher.scheduler.advanceTimeBy(1) // allow launch to start and set isLoading
        vm.signInWithEmail("test@example.com", "Password1") // should be ignored
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.signInWithEmail(any(), any()) }
    }

    @Test
    fun `signInWithGoogle ignores duplicate calls while loading`() = runTest {
        coEvery { authRepository.signInWithGoogle(any()) } coAnswers {
            kotlinx.coroutines.delay(1000)
            Result.success(fakeUser)
        }

        vm.signInWithGoogle("id-token")
        testDispatcher.scheduler.advanceTimeBy(1) // allow launch to start and set isLoading
        vm.signInWithGoogle("id-token") // should be ignored
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.signInWithGoogle(any()) }
    }

    // --- Email validation ---

    @Test
    fun `validateEmail rejects invalid emails`() {
        assertFalse(AuthViewModel.validateEmail(""))
        assertFalse(AuthViewModel.validateEmail("not-an-email"))
        assertFalse(AuthViewModel.validateEmail("@no-local.com"))
    }

    @Test
    fun `validateEmail accepts valid emails`() {
        assertTrue(AuthViewModel.validateEmail("a@b.com"))
        assertTrue(AuthViewModel.validateEmail("user@example.org"))
        assertTrue(AuthViewModel.validateEmail("first.last@domain.co.uk"))
    }

    @Test
    fun `signInWithEmail rejects invalid email with error`() = runTest {
        vm.signInWithEmail("bad-email", "Password1")
        advanceUntilIdle()

        assertEquals("Please enter a valid email and password", vm.state.value.error)
        coVerify(exactly = 0) { authRepository.signInWithEmail(any(), any()) }
    }

    // --- Password validation ---

    @Test
    fun `validatePassword enforces minimum requirements`() {
        assertTrue(AuthViewModel.validatePassword("short") != null)
        assertTrue(AuthViewModel.validatePassword("alllowercase1") != null)
        assertTrue(AuthViewModel.validatePassword("ALLUPPERCASE1") != null)
        assertTrue(AuthViewModel.validatePassword("NoDigitsHere") != null)
        assertNull(AuthViewModel.validatePassword("ValidPass1"))
    }

    // --- forgotPasswordSent reset ---

    @Test
    fun `showSheet resets forgotPasswordSent`() = runTest {
        coEvery { authRepository.forgotPassword(any()) } returns Result.success(Unit)

        vm.forgotPassword("test@example.com")
        advanceUntilIdle()

        assertTrue(vm.state.value.forgotPasswordSent)

        vm.showSheet(AuthSheet.LOGIN)
        assertFalse(vm.state.value.forgotPasswordSent)
    }

    @Test
    fun `dismissSheet resets forgotPasswordSent`() = runTest {
        coEvery { authRepository.forgotPassword(any()) } returns Result.success(Unit)

        vm.forgotPassword("test@example.com")
        advanceUntilIdle()

        assertTrue(vm.state.value.forgotPasswordSent)

        vm.dismissSheet()
        assertFalse(vm.state.value.forgotPasswordSent)
    }

    // --- Events ---

    @Test
    fun `successful login emits AuthSuccess event`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(fakeUser)

        vm.events.test {
            vm.signInWithEmail("test@example.com", "Password1")
            advanceUntilIdle()

            assertEquals(AuthEvent.AuthSuccess, awaitItem())
        }
    }

    @Test
    fun `failed login shows error in state`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns
            Result.failure(Exception("Invalid credentials"))

        vm.signInWithEmail("test@example.com", "Password1")
        advanceUntilIdle()

        assertEquals("Invalid credentials", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    // --- Register ---

    @Test
    fun `register rejects mismatched passwords`() = runTest {
        vm.register("test@example.com", "Password1", "Different1", null)
        advanceUntilIdle()

        assertEquals("Passwords do not match", vm.state.value.error)
        coVerify(exactly = 0) { authRepository.register(any(), any(), any()) }
    }

    @Test
    fun `clearError clears error state`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns
            Result.failure(Exception("fail"))

        vm.signInWithEmail("test@example.com", "Password1")
        advanceUntilIdle()

        assertTrue(vm.state.value.error != null)

        vm.clearError()
        assertNull(vm.state.value.error)
    }

    // --- Email verification flow ---

    @Test
    fun `successful register opens VERIFY_EMAIL sheet and emits VerificationEmailSent`() = runTest {
        coEvery { authRepository.register(any(), any(), any()) } returns Result.success(unverifiedUser)

        vm.events.test {
            vm.register("test@example.com", "Password1", "Password1", "Test")
            advanceUntilIdle()

            assertEquals(AuthEvent.VerificationEmailSent, awaitItem())
        }
        assertEquals(AuthSheet.VERIFY_EMAIL, vm.activeSheet.value)
        assertEquals("test@example.com", vm.state.value.pendingVerificationEmail)
        assertTrue(vm.state.value.verificationSent)
    }

    @Test
    fun `signInWithEmail with unverified user opens VERIFY_EMAIL sheet`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(unverifiedUser)

        vm.signInWithEmail("test@example.com", "Password1")
        advanceUntilIdle()

        assertEquals(AuthSheet.VERIFY_EMAIL, vm.activeSheet.value)
        assertEquals("test@example.com", vm.state.value.pendingVerificationEmail)
    }

    @Test
    fun `signInWithEmail with verified user emits AuthSuccess and closes sheet`() = runTest {
        coEvery { authRepository.signInWithEmail(any(), any()) } returns Result.success(fakeUser)

        vm.events.test {
            vm.signInWithEmail("test@example.com", "Password1")
            advanceUntilIdle()

            assertEquals(AuthEvent.AuthSuccess, awaitItem())
        }
        assertEquals(AuthSheet.NONE, vm.activeSheet.value)
        assertNull(vm.state.value.pendingVerificationEmail)
    }

    @Test
    fun `resendVerification calls repository and sets verificationSent`() = runTest {
        coEvery { authRepository.sendEmailVerification() } returns Result.success(Unit)

        vm.events.test {
            vm.resendVerification()
            advanceUntilIdle()

            assertEquals(AuthEvent.VerificationEmailSent, awaitItem())
        }
        assertTrue(vm.state.value.verificationSent)
        coVerify(exactly = 1) { authRepository.sendEmailVerification() }
    }

    @Test
    fun `checkEmailVerified when verified emits EmailVerified and AuthSuccess`() = runTest {
        coEvery { authRepository.register(any(), any(), any()) } returns Result.success(unverifiedUser)
        coEvery { authRepository.reloadEmailVerified() } returns Result.success(true)

        vm.register("test@example.com", "Password1", "Password1", null)
        advanceUntilIdle()
        assertEquals(AuthSheet.VERIFY_EMAIL, vm.activeSheet.value)

        vm.events.test {
            vm.checkEmailVerified()
            advanceUntilIdle()

            assertEquals(AuthEvent.EmailVerified, awaitItem())
            assertEquals(AuthEvent.AuthSuccess, awaitItem())
        }
        assertEquals(AuthSheet.NONE, vm.activeSheet.value)
        assertNull(vm.state.value.pendingVerificationEmail)
        assertTrue(vm.state.value.user?.emailVerified == true)
    }

    @Test
    fun `checkEmailVerified when still not verified sets verificationCheckFailed`() = runTest {
        coEvery { authRepository.reloadEmailVerified() } returns Result.success(false)

        vm.checkEmailVerified()
        advanceUntilIdle()

        assertTrue(vm.state.value.verificationCheckFailed)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `cancelVerification signs out and resets state`() = runTest {
        coEvery { authRepository.signOut() } returns Unit

        vm.cancelVerification()
        advanceUntilIdle()

        coVerify(exactly = 1) { authRepository.signOut() }
        assertEquals(AuthSheet.NONE, vm.activeSheet.value)
        assertNull(vm.state.value.user)
        assertNull(vm.state.value.pendingVerificationEmail)
    }
}
