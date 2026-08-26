package com.app.flashlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.app.flashlearn.core.seed.DatabaseSeeder
import com.app.flashlearn.domain.repository.SettingsRepository
import com.app.flashlearn.navigation.BottomNavDestination
import com.app.flashlearn.navigation.FlashLearnNavGraph
import com.app.flashlearn.navigation.Routes
import com.app.flashlearn.presentation.settings.ThemeMode
import com.app.flashlearn.ui.theme.FlashLearnTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var databaseSeeder: DatabaseSeeder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeModeFlow = remember(settingsRepository) {
                settingsRepository.observeValue(SettingsRepository.THEME_MODE)
                    .map { ThemeMode.fromStorage(it) }
            }
            val themeMode by themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)

            val useDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> null
            }

            FlashLearnTheme(useDarkTheme = useDarkTheme) {
                AppRoot(settingsRepository = settingsRepository, databaseSeeder = databaseSeeder)
            }
        }
    }
}

@Composable
private fun AppRoot(settingsRepository: SettingsRepository, databaseSeeder: DatabaseSeeder) {
    // تعیین صفحه شروع: اول Seed کردن دیتابیس (زبان‌ها + کلمات نمونه، اگر قبلاً انجام
    // نشده) به‌طور کامل تمام می‌شود، بعد اگر Onboarding قبلاً تکمیل شده، مستقیم به Home
    // می‌رویم. رفع باگ: قبلاً Seed کردن ناهمگام و بدون انتظار در Application اجرا می‌شد؛
    // حالا اینجا با suspend await می‌شود تا تضمین شود قبل از این‌که کاربر بتواند به
    // Backup/Restore برسد و بکاپ Import کند، دیتابیس کاملاً آماده و پایدار است.
    val startDestination by produceState<String?>(initialValue = null) {
        databaseSeeder.seedIfNeeded()
        val completed = settingsRepository.getValue(SettingsRepository.ONBOARDING_COMPLETED) == "true"
        value = if (completed) Routes.HOME else Routes.ONBOARDING
    }

    val destination = startDestination
    if (destination == null) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Text(
                text = "در حال بارگذاری...",
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val bottomNavRoutes = BottomNavDestination.values().map { it.route }.toSet()
    val showBottomBar = currentRoute?.hierarchy?.any { it.route in bottomNavRoutes } == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    BottomNavDestination.values().forEach { dest ->
                        val selected = currentRoute?.hierarchy?.any { it.route == dest.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(Routes.HOME)
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(iconFor(dest), contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            FlashLearnNavGraph(navController = navController, startDestination = destination)
        }
    }
}

private fun iconFor(destination: BottomNavDestination) = when (destination) {
    BottomNavDestination.HOME -> Icons.Filled.Home
    BottomNavDestination.REVIEW -> Icons.Filled.DateRange
    BottomNavDestination.VOCABULARY -> Icons.Filled.List
    BottomNavDestination.ADD -> Icons.Filled.Add
    BottomNavDestination.SETTINGS -> Icons.Filled.Settings
}

