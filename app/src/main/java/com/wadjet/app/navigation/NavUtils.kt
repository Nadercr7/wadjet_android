package com.wadjet.app.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry

/**
 * Anti-double-navigation guard.
 * Only allow navigation when the current entry is fully RESUMED,
 * preventing duplicate pushes from rapid taps.
 *
 * Reference: Jetsnack JetsnackNavController.kt
 */
fun NavBackStackEntry.lifecycleIsResumed(): Boolean =
    lifecycle.currentState == Lifecycle.State.RESUMED
