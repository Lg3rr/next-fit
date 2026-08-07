package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NextDarkSurface
import com.example.ui.theme.NextEmeraldPrimary
import com.example.ui.theme.NextTextMuted
import com.example.ui.theme.NextTextPrimary

enum class BottomTab(val title: String, val icon: ImageVector, val tag: String) {
    WORKOUT("Workout", Icons.Default.FitnessCenter, "tab_workout"),
    EXERCISES("Exercises", Icons.Default.FormatListBulleted, "tab_exercises"),
    ANALYTICS("History & PRs", Icons.Default.Analytics, "tab_analytics")
}

@Composable
fun NextBottomNavigationBar(
    currentTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = NextDarkSurface,
        tonalElevation = 8.dp
    ) {
        BottomTab.values().forEach { tab ->
            val isSelected = tab == currentTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title
                    )
                },
                label = { Text(tab.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = androidx.compose.ui.graphics.Color.White,
                    selectedTextColor = NextEmeraldPrimary,
                    indicatorColor = NextEmeraldPrimary,
                    unselectedIconColor = NextTextMuted,
                    unselectedTextColor = NextTextMuted
                ),
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }
}
