package com.wadjet.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: Route,
    val icon: ImageVector,
    val label: String,
) {
    HOME(Route.Landing, Icons.Outlined.Home, "Home"),
    SCAN(Route.Scan, Icons.Outlined.CameraAlt, "Scan"),
    EXPLORE(Route.Explore, Icons.Outlined.Explore, "Explore"),
    STORIES(Route.Stories, Icons.Outlined.MenuBook, "Stories"),
    PROFILE(Route.Dashboard, Icons.Outlined.Person, "Profile"),
}
