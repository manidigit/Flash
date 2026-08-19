package com.app.flashlearn.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Language
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.LanguageRepository
import com.app.flashlearn.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val availableLanguages: List<Language> = emptyList(),
    val sourceLanguage: String? = null,
    val targetLanguage: String? = null,
    val isSaving: Boolean = false,
    val completed: Boolean = false
) {
    val canConfirm: Boolean
        get() = sourceLanguage != null && targetLanguage != null && sourceLanguage != targetLanguage
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val languageRepository: LanguageRepository,
    private val languagePairRepository: LanguagePairRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val languages = languageRepository.getAll()
            _uiState.value = _uiState.value.copy(
                availableLanguages = languages,
                sourceLanguage = languages.firstOrNull { it.code == "es" }?.code,
                targetLanguage = languages.firstOrNull { it.code == "fa" }?.code
            )
        }
    }

    fun selectSource(code: String) {
        _uiState.value = _uiState.value.copy(sourceLanguage = code)
    }

    fun selectTarget(code: String) {
        _uiState.value = _uiState.value.copy(targetLanguage = code)
    }

    fun confirm() {
        val state = _uiState.value
        val source = state.sourceLanguage ?: return
        val target = state.targetLanguage ?: return
        if (source == target) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            languagePairRepository.setActivePair(source, target)
            settingsRepository.setValue(SettingsRepository.ONBOARDING_COMPLETED, "true")
            _uiState.value = _uiState.value.copy(isSaving = false, completed = true)
        }
    }
}
