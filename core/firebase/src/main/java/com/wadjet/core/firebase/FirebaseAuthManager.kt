package com.wadjet.core.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(idToken: String): FirebaseUser {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        return result.user ?: throw IllegalStateException("Firebase sign-in returned null user")
    }

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("Firebase sign-in returned null user")
    }

    suspend fun createAccount(email: String, password: String): FirebaseUser {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        return result.user ?: throw IllegalStateException("Firebase create returned null user")
    }

    suspend fun sendPasswordReset(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    /** Sends a verification email to the currently signed-in Firebase user. */
    suspend fun sendEmailVerification() {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No Firebase user signed in to send verification")
        user.sendEmailVerification().await()
    }

    /**
     * Reloads the current Firebase user from the server and returns whether the
     * email is now verified. Returns false if no user is signed in.
     */
    suspend fun reloadAndIsEmailVerified(): Boolean {
        val user = firebaseAuth.currentUser ?: return false
        user.reload().await()
        return firebaseAuth.currentUser?.isEmailVerified == true
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun getIdToken(): String? {
        return try {
            currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            Timber.e(e, "Failed to get Firebase ID token")
            null
        }
    }
}
