package com.app.flashlearn.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

enum class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    HOME(Screen.Home.route, "خانه", Icons.Filled.Home),
    VOCABULARY(Screen.Vocabulary.route, "واژگان", Icons.Filled.Book),
    ADD(Screen.AddConcept.route, "افزودن", Icons.Filled.Add),
    SETTINGS(Screen.Settings.route, "تنظیمات", Icons.Filled.Settings)
}

@Composable
fun FlashLearnApp(isFirstLaunch: Boolean) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route, Screen.Vocabulary.route, Screen.AddConcept.route, Screen.Settings.route
    )

    Scaffold(
        bottomBar = { if (showBottomBar) FlashLearnBottomBar(navController, currentRoute) }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            FlashLearnNavGraph(navController = navController, isFirstLaunch = isFirstLaunch)
        }
    }
}

@Composable
fun FlashLearnBottomBar(navController: androidx.navigation.NavHostController, currentRoute: String?) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { navController.navigate(item.route) { launchSingleTop = true } },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
