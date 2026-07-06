package com.wadjet.core.firebase

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A4: thin wrapper over Firebase Analytics + Crashlytics so feature/data code
 * logs events without touching Firebase types directly.
 */
@Singleton
class WadjetAnalytics @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val analytics = FirebaseAnalytics.getInstance(context)
    private val crashlytics = FirebaseCrashlytics.getInstance()

    fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        })
    }

    fun logLogin(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    fun logSignUp(method: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SIGN_UP, Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        })
    }

    fun logScanCompleted(numDetections: Int, source: String?) {
        analytics.logEvent("scan_completed", Bundle().apply {
            putLong("num_detections", numDetections.toLong())
            putString("detection_source", source ?: "unknown")
        })
    }

    fun logStoryCompleted(storyId: String) {
        analytics.logEvent("story_completed", Bundle().apply {
            putString("story_id", storyId)
        })
    }

    /** Associates analytics + crash reports with the signed-in user (null on sign-out). */
    fun setUser(uid: String?) {
        analytics.setUserId(uid)
        crashlytics.setUserId(uid ?: "")
    }
}
