package com.app.flashlearn.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.database.dao.AppSettingsDao
import com.app.flashlearn.domain.model.AppSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

enum class ThemeMode { LIGHT, DARK, SYSTEM }

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val sourceLanguage: String = "fa",
    val targetLanguage: String = "en"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsDao: AppSettingsDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        appSettingsDao.getSettings().onEach { s ->
            val settings = s ?: AppSettings()
            _uiState.value = _uiState.value.copy(
                themeMode = when (settings.appTheme.uppercase()) {
                    "LIGHT" -> ThemeMode.LIGHT
                    "SYSTEM" -> ThemeMode.SYSTEM
                    else -> ThemeMode.DARK
                },
                sourceLanguage = settings.sourceLanguage,
                targetLanguage = settings.targetLanguage
            )
        }.launchIn(viewModelScope)
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            val current = appSettingsDao.getSettingsSync() ?: AppSettings().let {
                com.app.flashlearn.database.entity.AppSettingsEntity(
                    appTheme = it.appTheme,
                    appLanguage = it.appLanguage,
                    sourceLanguage = it.sourceLanguage,
                    targetLanguage = it.targetLanguage
                )
            }
            appSettingsDao.update(current.copy(appTheme = mode.name))
        }
    }

    fun reverseLanguagePair() {
        viewModelScope.launch {
            val s = appSettingsDao.getSettingsSync() ?: return@launch
            appSettingsDao.updateLanguagePair(s.targetLanguage, s.sourceLanguage)
        }
    }
}
