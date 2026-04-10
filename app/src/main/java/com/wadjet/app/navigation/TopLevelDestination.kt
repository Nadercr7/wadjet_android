package com.wadjet.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: Route,
    val icon: ImageVector,
    val label: String,
) {
    HOME(Route.Landing, Icons.Outlined.Home, "Home"),
    HIEROGLYPHS(Route.Hieroglyphs, Icons.Outlined.HistoryEdu, "Hieroglyphs"),
    EXPLORE(Route.Explore, Icons.Outlined.Explore, "Explore"),
    STORIES(Route.Stories, Icons.AutoMirrored.Outlined.MenuBook, "Stories"),
    THOTH(Route.Chat, Icons.AutoMirrored.Outlined.Chat, "Thoth"),
}
