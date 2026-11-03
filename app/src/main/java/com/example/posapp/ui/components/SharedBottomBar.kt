package com.example.posapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.posapp.R
import com.example.posapp.ui.navigation.Screen
import com.example.posapp.ui.theme.ThemeMaroonPrimary
import com.example.posapp.ui.theme.ThemeTextSecondary

@Composable
fun SharedBottomBar(
    navController: NavController,
    currentRoute: String?,
    onMenuClick: () -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .height(72.dp)
            .shadow(elevation = 14.dp, shape = RoundedCornerShape(36.dp), spotColor = Color.Black.copy(alpha = 0.1f))
            .clip(RoundedCornerShape(36.dp)),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = ThemeTextSecondary,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val navColors = NavigationBarItemDefaults.colors(
            selectedIconColor = ThemeMaroonPrimary,
            unselectedIconColor = ThemeTextSecondary,
            selectedTextColor = ThemeMaroonPrimary,
            unselectedTextColor = ThemeTextSecondary,
            indicatorColor = ThemeMaroonPrimary.copy(alpha = 0.1f)
        )

        // Tab 1: Primary Sale / POS Page
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {
                if (currentRoute != Screen.Home.route) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            },
            icon = { 
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_pos), 
                    contentDescription = "Sale", 
                    modifier = Modifier.size(32.dp)
                ) 
            },
            colors = navColors,
            alwaysShowLabel = false
        )

        // Tab 2: Business Analytics Page
        NavigationBarItem(
            selected = currentRoute == Screen.Dashboard.route,
            onClick = {
                if (currentRoute != Screen.Dashboard.route) {
                    navController.navigate(Screen.Dashboard.route)
                }
            },
            icon = { 
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_dashboard), 
                    contentDescription = "Business Analytics", 
                    modifier = Modifier.size(32.dp)
                ) 
            },
            colors = navColors,
            alwaysShowLabel = false
        )

        // Tab 3: Menu Sheet Trigger
        NavigationBarItem(
            selected = false,
            onClick = onMenuClick,
            icon = { 
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_menu), 
                    contentDescription = "Menu", 
                    modifier = Modifier.size(32.dp)
                ) 
            },
            colors = navColors,
            alwaysShowLabel = false
        )

        // Tab 4: Inventory Management
        NavigationBarItem(
            selected = currentRoute == Screen.Inventory.route,
            onClick = {
                if (currentRoute != Screen.Inventory.route) navController.navigate(Screen.Inventory.route)
            },
            icon = { 
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_inventory), 
                    contentDescription = "Inventory", 
                    modifier = Modifier.size(32.dp)
                ) 
            },
            colors = navColors,
            alwaysShowLabel = false
        )

        // Tab 5: Settings
        NavigationBarItem(
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                if (currentRoute != Screen.Settings.route) navController.navigate(Screen.Settings.route)
            },
            icon = { 
                Icon(
                    painter = painterResource(id = R.drawable.ic_nav_settings), 
                    contentDescription = "Settings", 
                    modifier = Modifier.size(32.dp)
                ) 
            },
            colors = navColors,
            alwaysShowLabel = false
        )
    }
}