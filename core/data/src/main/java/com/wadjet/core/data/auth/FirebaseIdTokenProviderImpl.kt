package com.wadjet.core.data.auth

import com.wadjet.core.firebase.FirebaseAuthManager
import com.wadjet.core.network.FirebaseIdTokenProvider
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A1: bridges :core:firebase into :core:network's TokenAuthenticator.
 * runBlocking is safe here — the Authenticator runs synchronously on an
 * OkHttp worker thread, never on a coroutine dispatcher or the main thread.
 */
@Singleton
class FirebaseIdTokenProviderImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuthManager,
) : FirebaseIdTokenProvider {

    override fun freshIdToken(): String? = runBlocking {
        firebaseAuth.getIdToken(forceRefresh = true)
    }
}
