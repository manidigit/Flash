package com.app.flashlearn.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.usecase.GetDailyReviewCardsUseCase
import com.app.flashlearn.domain.usecase.GetWeeklyReviewCardsUseCase
import com.app.flashlearn.domain.usecase.GetMonthlyReviewCardsUseCase
import com.app.flashlearn.domain.usecase.GetLearnedCardsUseCase
import com.app.flashlearn.domain.usecase.GetStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ============ HOME SCREEN ============

data class HomeUiState(
    val todayCardsReady: Int = 0,
    val todayTotalCards: Int = 0,
    val streak: Int = 0,
    val masteredCount: Int = 0,
    val dailyReady: Int = 0,
    val weeklyReady: Int = 0,
    val monthlyReady: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDailyReviewCardsUseCase: GetDailyReviewCardsUseCase,
    private val getWeeklyReviewCardsUseCase: GetWeeklyReviewCardsUseCase,
    private val getMonthlyReviewCardsUseCase: GetMonthlyReviewCardsUseCase,
    private val getStatisticsUseCase: GetStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val dailyCards = getDailyReviewCardsUseCase()
                val weeklyCards = getWeeklyReviewCardsUseCase()
                val monthlyCards = getMonthlyReviewCardsUseCase()
                val statistics = getStatisticsUseCase()

                val todayTotal = dailyCards.size
                val todayReady = dailyCards.size

                _uiState.value = HomeUiState(
                    todayCardsReady = todayReady,
                    todayTotalCards = todayTotal,
                    streak = statistics.streak,
                    masteredCount = statistics.masteredCount,
                    dailyReady = dailyCards.size,
                    weeklyReady = weeklyCards.size,
                    monthlyReady = monthlyCards.size,
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
    val dailyReady: Int = 0,
    val weeklyReady: Int = 0,
    val monthlyReady: Int = 0,
    val totalReady: Int = 0,
    val easyCount: Int = 0,
    val mediumCount: Int = 0,
    val hardCount: Int = 0,
    val veryHardCount: Int = 0,
    val learnedCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReviewTypeSelectionViewModel @Inject constructor(
    private val getDailyReviewCardsUseCase: GetDailyReviewCardsUseCase,
    private val getWeeklyReviewCardsUseCase: GetWeeklyReviewCardsUseCase,
    private val getMonthlyReviewCardsUseCase: GetMonthlyReviewCardsUseCase,
    private val getLearnedCardsUseCase: GetLearnedCardsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewTypeSelectionUiState())
    val uiState: StateFlow<ReviewTypeSelectionUiState> = _uiState.asStateFlow()

    init {
        loadReviewData()
    }

    private fun loadReviewData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val dailyCards = getDailyReviewCardsUseCase()
                val weeklyCards = getWeeklyReviewCardsUseCase()
                val monthlyCards = getMonthlyReviewCardsUseCase()
                val learnedCards = getLearnedCardsUseCase()

                val easyCards = dailyCards.filter { it.difficulty == "EASY" }
                val mediumCards = dailyCards.filter { it.difficulty == "MEDIUM" }
                val hardCards = dailyCards.filter { it.difficulty == "HARD" }
                val veryHardCards = dailyCards.filter { it.difficulty == "VERY_HARD" }

                _uiState.value = ReviewTypeSelectionUiState(
                    dailyReady = dailyCards.size,
                    weeklyReady = weeklyCards.size,
                    monthlyReady = monthlyCards.size,
                    totalReady = dailyCards.size + weeklyCards.size + monthlyCards.size,
                    easyCount = easyCards.size,
                    mediumCount = mediumCards.size,
                    hardCount = hardCards.size,
                    veryHardCount = veryHardCards.size,
                    learnedCount = learnedCards.size,
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

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(currentTheme = theme)
            // Save to preferences
        }
    }

    fun updateLanguagePair(source: String, target: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                sourceLanguage = source,
                targetLanguage = target
            )
            // Save to preferences
        }
    }
}
