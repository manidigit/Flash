package com.app.flashlearn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.app.flashlearn.database.InitialLanguageSeeder
import com.app.flashlearn.database.dao.AppSettingsDao
import com.app.flashlearn.database.dao.LanguageDao
import com.app.flashlearn.database.entity.AppSettingsEntity
import com.app.flashlearn.navigation.FlashLearnNavGraph
import com.app.flashlearn.navigation.Routes
import com.app.flashlearn.ui.theme.FlashLearnTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by viewModel.uiState.collectAsState()
            if (!state.isLoading) {
                AppContent(isDarkTheme = state.isDarkTheme, startDestination = state.startDestination)
            }
        }
    }
}

@Composable
private fun AppContent(isDarkTheme: Boolean?, startDestination: String) {
    FlashLearnTheme(useDarkTheme = isDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            FlashLearnNavGraph(navController = navController, startDestination = startDestination)
        }
    }
}

data class MainActivityUiState(
    val isLoading: Boolean = true,
    val isDarkTheme: Boolean? = null,
    val startDestination: String = Routes.ONBOARDING
)

/**
 * قبل از نمایش هر صفحه‌ای: (۱) اگر جدول زبان‌ها خالی است، سه زبان پیش‌فرض seed می‌شوند،
 * (۲) اگر ردیف تنظیمات هنوز ساخته نشده، یک ردیف پیش‌فرض insert می‌شود (وگرنه update های
 * بعدی روی این ردیف - مثل تکمیل Onboarding - چون ردیفی برای Update وجود ندارد بی‌اثر
 * می‌مانند)، سپس بر اساس onboardingCompleted صفحه شروع تعیین می‌شود.
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val appSettingsDao: AppSettingsDao,
    private val languageDao: LanguageDao,
    private val initialLanguageSeeder: InitialLanguageSeeder
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainActivityUiState())
    val uiState: StateFlow<MainActivityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (languageDao.count() == 0) {
                initialLanguageSeeder.seed()
            }

            var settings = appSettingsDao.getSettingsSync()
            if (settings == null) {
                settings = AppSettingsEntity()
                appSettingsDao.insert(settings)
            }

            val startDestination = if (settings.onboardingCompleted) Routes.HOME else Routes.ONBOARDING
            val isDarkTheme = when (settings.appTheme) {
                "LIGHT" -> false
                "DARK" -> true
                else -> null
            }

            _uiState.value = MainActivityUiState(
                isLoading = false,
                isDarkTheme = isDarkTheme,
                startDestination = startDestination
            )
        }
    }
}
