package com.app.flashlearn.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.LanguagePair
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ThemeMode(val storageValue: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromStorage(value: String?): ThemeMode = values().firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val activePair: LanguagePair? = null
)

/**
 * صفحه Settings (بند 54): تغییر تم (بند 6) و تغییر جهت زبان (بند 71 — بدون تغییر یا Duplicate
 * کردن Vocabulary، فقط رکورد LanguagePair فعال عوض می‌شود).
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val languagePairRepository: LanguagePairRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.observeValue(SettingsRepository.THEME_MODE),
        languagePairRepository.observeActivePair()
    ) { themeValue, activePair ->
        SettingsUiState(themeMode = ThemeMode.fromStorage(themeValue), activePair = activePair)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setValue(SettingsRepository.THEME_MODE, mode.storageValue)
        }
    }

    /** جهت زبان فعال را برعکس می‌کند (مثلاً ES->FA به FA->ES) بدون تغییر دیتای واژگان. */
    fun swapLanguageDirection() {
        val pair = uiState.value.activePair ?: return
        viewModelScope.launch {
            languagePairRepository.setActivePair(pair.targetLanguage, pair.sourceLanguage)
        }
    }
}
