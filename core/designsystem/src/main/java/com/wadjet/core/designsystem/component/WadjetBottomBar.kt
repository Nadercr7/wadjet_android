package com.wadjet.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wadjet.core.designsystem.WadjetColors

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem("landing", "Home", Icons.Filled.Home),
    BottomNavItem("scan", "Scan", Icons.Filled.QrCodeScanner),
    BottomNavItem("explore", "Explore", Icons.Filled.Explore),
    BottomNavItem("stories", "Stories", Icons.Filled.AutoStories),
    BottomNavItem("dashboard", "Profile", Icons.Filled.Person),
)

@Composable
fun WadjetBottomBar(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
) {
    NavigationBar(
        containerColor = WadjetColors.Surface,
        contentColor = WadjetColors.TextMuted,
        tonalElevation = 0.dp,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = {
                    Text(item.label, style = MaterialTheme.typography.labelSmall)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = WadjetColors.Gold,
                    selectedTextColor = WadjetColors.Gold,
                    unselectedIconColor = WadjetColors.TextMuted,
                    unselectedTextColor = WadjetColors.TextMuted,
                    indicatorColor = WadjetColors.Gold.copy(alpha = 0.12f),
                ),
            )
        }
    }
}
