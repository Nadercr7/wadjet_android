package com.wadjet.app

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * A4: release-build Timber tree that forwards warnings/errors to Crashlytics.
 * INFO and below are dropped; WARN becomes a breadcrumb log line; ERROR (and
 * any attached throwable) becomes a non-fatal report.
 */
class CrashlyticsTree : Timber.Tree() {

    private val crashlytics = FirebaseCrashlytics.getInstance()

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        crashlytics.log("${tag ?: "Wadjet"}: $message")
        if (priority >= Log.ERROR) {
            crashlytics.recordException(t ?: RuntimeException(message))
        } else if (t != null) {
            crashlytics.recordException(t)
        }
    }
}
