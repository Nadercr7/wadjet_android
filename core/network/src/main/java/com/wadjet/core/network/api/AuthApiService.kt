package com.wadjet.core.network.api

import com.wadjet.core.network.model.AuthResponse
import com.wadjet.core.network.model.FirebaseAuthRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * A1 (Firebase-primary auth): the app authenticates with Firebase and exchanges the
 * Firebase ID token for a backend session. Credentials never go to the backend directly;
 * the old login/register/google endpoints remain server-side for the web app only.
 */
interface AuthApiService {

    @POST("api/auth/firebase")
    suspend fun firebaseAuth(@Body body: FirebaseAuthRequest): Response<AuthResponse>

    @POST("api/auth/refresh")
    suspend fun refresh(): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<Unit>
}
