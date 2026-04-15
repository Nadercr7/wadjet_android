package com.wadjet.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.HistoryEdu
import androidx.compose.material.icons.outlined.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.wadjet.core.designsystem.R as DesignR

enum class TopLevelDestination(
    val route: Route,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
) {
    HOME(Route.Landing, Icons.Outlined.Home, DesignR.string.nav_home),
    HIEROGLYPHS(Route.Hieroglyphs, Icons.Outlined.HistoryEdu, DesignR.string.nav_hieroglyphs),
    EXPLORE(Route.Explore, Icons.Outlined.Explore, DesignR.string.nav_explore),
    STORIES(Route.Stories, Icons.AutoMirrored.Outlined.MenuBook, DesignR.string.nav_stories),
    THOTH(Route.Chat, Icons.AutoMirrored.Outlined.Chat, DesignR.string.nav_thoth),
}
