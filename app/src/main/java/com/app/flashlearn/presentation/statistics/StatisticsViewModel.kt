package com.app.flashlearn.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.repository.ReviewHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class AccuracyStat(val correct: Int, val total: Int) {
    val percentage: Int get() = if (total == 0) 0 else (correct * 100) / total
}

data class StatisticsUiState(
    val totalWords: Int = 0,
    val stageSummary: Map<LearningStage, Int> = emptyMap(),
    val difficultySummary: Map<Difficulty, Int> = emptyMap(),
    val today: AccuracyStat = AccuracyStat(0, 0),
    val thisWeek: AccuracyStat = AccuracyStat(0, 0),
    val thisMonth: AccuracyStat = AccuracyStat(0, 0),
    val allTime: AccuracyStat = AccuracyStat(0, 0),
    val isLoading: Boolean = true
)

/**
 * صفحه Statistics (بند 66-67): آمار کلی + آمار زمانی (Today/This Week/This Month/All Time)
 * با استفاده از داده واقعی ReviewHistory.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val now = DateTimeUtils.now()
            val startOfToday = DateTimeUtils.startOfDay(now)
            val startOfWeek = now - TimeUnit.DAYS.toMillis(7)
            val startOfMonth = now - TimeUnit.DAYS.toMillis(30)

            val stageSummary = learningStateRepository.getStageSummary()
            val difficultySummary = learningStateRepository.getDifficultySummary()

            conceptRepository.observeActiveCount().collect { totalWords ->
                _uiState.value = StatisticsUiState(
                    totalWords = totalWords,
                    stageSummary = stageSummary,
                    difficultySummary = difficultySummary,
                    today = accuracyBetween(startOfToday, now),
                    thisWeek = accuracyBetween(startOfWeek, now),
                    thisMonth = accuracyBetween(startOfMonth, now),
                    allTime = accuracyBetween(0L, now),
                    isLoading = false
                )
            }
        }
    }

    private suspend fun accuracyBetween(from: Long, to: Long): AccuracyStat {
        val correct = reviewHistoryRepository.countCorrectBetween(from, to)
        val total = reviewHistoryRepository.countTotalBetween(from, to)
        return AccuracyStat(correct, total)
    }
}
