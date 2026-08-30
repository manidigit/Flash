package com.app.flashlearn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ============ HOME SCREEN ============

data class HomeUiState(
    val todayCardsReady: Int = 20,
    val todayTotalCards: Int = 28,
    val streak: Int = 7,
    val masteredCount: Int = 324,
    val dailyReady: Int = 12,
    val weeklyReady: Int = 8,
    val monthlyReady: Int = 3,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Mock data - replace with real use cases later
                _uiState.value = HomeUiState(
                    todayCardsReady = 20,
                    todayTotalCards = 28,
                    streak = 7,
                    masteredCount = 324,
                    dailyReady = 12,
                    weeklyReady = 8,
                    monthlyReady = 3,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}

// ============ REVIEW TYPE SELECTION ============

data class ReviewTypeSelectionUiState(
    val dailyReady: Int = 4525,
    val weeklyReady: Int = 45,
    val monthlyReady: Int = 0,
    val totalReady: Int = 4570,
    val easyCount: Int = 328,
    val mediumCount: Int = 1245,
    val hardCount: Int = 892,
    val veryHardCount: Int = 156,
    val learnedCount: Int = 324,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ReviewTypeSelectionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewTypeSelectionUiState())
    val uiState: StateFlow<ReviewTypeSelectionUiState> = _uiState.asStateFlow()

    init {
        loadReviewData()
    }

    private fun loadReviewData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                // Mock data - replace with real use cases later
                _uiState.value = ReviewTypeSelectionUiState(
                    dailyReady = 4525,
                    weeklyReady = 45,
                    monthlyReady = 0,
                    totalReady = 4570,
                    easyCount = 328,
                    mediumCount = 1245,
                    hardCount = 892,
                    veryHardCount = 156,
                    learnedCount = 324,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }
}

// ============ SETTINGS SCREEN ============

data class SettingsUiState(
    val currentTheme: String = "system",
    val sourceLanguage: String = "ES",
    val targetLanguage: String = "FA",
    val appVersion: String = "56",
    val buildDate: String = "2026/08/29",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateTheme(theme: String) {
        _uiState.value = _uiState.value.copy(currentTheme = theme)
        // TODO: Save to preferences
    }

    fun updateLanguagePair(source: String, target: String) {
        _uiState.value = _uiState.value.copy(
            sourceLanguage = source,
            targetLanguage = target
        )
        // TODO: Save to preferences
    }
}
