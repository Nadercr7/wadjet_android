package com.wadjet.core.network

/**
 * A1: supplies a fresh Firebase ID token so [TokenAuthenticator] can re-exchange
 * a rejected backend session instead of signing the user out. Implemented in
 * :core:data (which sees :core:firebase); returns null when no Firebase user
 * is signed in or the token cannot be fetched.
 */
fun interface FirebaseIdTokenProvider {
    fun freshIdToken(): String?
}
