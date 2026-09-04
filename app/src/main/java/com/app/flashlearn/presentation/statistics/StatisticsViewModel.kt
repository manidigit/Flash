package com.app.flashlearn.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class StatsPeriod(val days: Int, val label: String) {
    WEEK(7, "هفته"), MONTH(30, "ماه"), ALL_TIME(3650, "کل")
}

data class StatisticsUiState(
    val period: StatsPeriod = StatsPeriod.WEEK,
    val dailyCounts: List<Pair<String, Int>> = emptyList(),
    val difficultyDistribution: Map<Difficulty, Int> = emptyMap(),
    val accuracyPercent: Int = 0,
    val totalReviews: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init { load(StatsPeriod.WEEK) }

    fun onPeriodChanged(period: StatsPeriod) = load(period)

    private fun load(period: StatsPeriod) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, period = period)
            val since = System.currentTimeMillis() - period.days * 24 * 60 * 60 * 1000L

            val daily = statisticsRepository.getDailyReviewCounts(since)
            val difficulty = statisticsRepository.getDifficultyDistribution()
            val (correct, total) = statisticsRepository.getAccuracySummary(since)
            val accuracyPercent = if (total > 0) (correct * 100 / total) else 0

            _uiState.value = StatisticsUiState(
                period = period,
                dailyCounts = daily,
                difficultyDistribution = difficulty,
                accuracyPercent = accuracyPercent,
                totalReviews = total,
                isLoading = false
            )
        }
    }
}
