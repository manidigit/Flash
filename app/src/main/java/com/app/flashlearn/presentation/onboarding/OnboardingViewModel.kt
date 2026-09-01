package com.app.flashlearn.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.repository.LanguageRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val languageRepository: LanguageRepository,
    private val languagePairRepository: LanguagePairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState

    init {
        loadLanguages()
    }

    private fun loadLanguages() {
        viewModelScope.launch {
            languageRepository.getSupportedLanguages().collect { languages ->
                _uiState.value = _uiState.value.copy(
                    languages = languages,
                    isLoading = false
                )
            }
        }
    }

    fun completeOnboarding(source: String, target: String) {
        viewModelScope.launch {
            languagePairRepository.setActiveLanguagePair(source, target)
            _uiState.value = _uiState.value.copy(onboardingComplete = true)
        }
    }
}

data class OnboardingUiState(
    val languages: List<com.app.flashlearn.domain.model.Language> = emptyList(),
    val isLoading: Boolean = true,
    val onboardingComplete: Boolean = false
)
