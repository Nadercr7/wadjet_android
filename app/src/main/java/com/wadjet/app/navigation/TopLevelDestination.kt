package com.wadjet.app.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.wadjet.core.designsystem.R as DesignR

enum class TopLevelDestination(
    val route: Route,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
) {
    // U3: bespoke Egyptian line-glyph tab icons (retire the generic Material set).
    HOME(Route.Landing, DesignR.drawable.ic_nav_home, DesignR.string.nav_home),
    HIEROGLYPHS(Route.Hieroglyphs, DesignR.drawable.ic_nav_hieroglyphs, DesignR.string.nav_hieroglyphs),
    EXPLORE(Route.Explore, DesignR.drawable.ic_nav_explore, DesignR.string.nav_explore),
    STORIES(Route.Stories, DesignR.drawable.ic_nav_stories, DesignR.string.nav_stories),
    THOTH(Route.Chat, DesignR.drawable.ic_nav_thoth, DesignR.string.nav_thoth),
}
