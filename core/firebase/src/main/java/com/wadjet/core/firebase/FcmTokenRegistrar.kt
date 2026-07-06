package com.wadjet.core.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A3: keeps the device's FCM registration token associated with the signed-in
 * user at users/{uid}/fcm_tokens/{token} (covered by firestore.rules — a user
 * can only touch their own subtree). Multiple devices coexist naturally since
 * the token is the document id.
 */
@Singleton
class FcmTokenRegistrar @Inject constructor(
    private val firebaseAuth: FirebaseAuthManager,
    private val firestore: FirebaseFirestore,
) {

    /** Registers the current device token for the signed-in user (no-op when signed out). */
    suspend fun registerCurrentToken() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            registerToken(uid, token)
        } catch (e: Exception) {
            Timber.w(e, "FCM token registration failed")
        }
    }

    /** Registers a specific token (used by onNewToken, where FCM hands us the fresh token). */
    suspend fun registerToken(uid: String, token: String) {
        try {
            firestore.collection("users").document(uid)
                .collection("fcm_tokens").document(token)
                .set(
                    mapOf(
                        "token" to token,
                        "platform" to "android",
                        "updated_at" to Timestamp.now(),
                    ),
                )
                .await()
            Timber.d("FCM token registered for user %s: %s…", uid.take(6), token.take(10))
        } catch (e: Exception) {
            Timber.w(e, "FCM token write failed")
        }
    }

    /** Removes this device's token from the user's registry. Call BEFORE Firebase sign-out. */
    suspend fun unregisterCurrentToken() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            firestore.collection("users").document(uid)
                .collection("fcm_tokens").document(token)
                .delete()
                .await()
        } catch (e: Exception) {
            Timber.w(e, "FCM token unregister failed")
        }
    }
}
