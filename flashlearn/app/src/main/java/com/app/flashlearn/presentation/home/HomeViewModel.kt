package com.app.flashlearn.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalWords: Int = 0,
    val dueDaily: Int = 0,
    val dueWeekly: Int = 0,
    val dueMonthly: Int = 0,
    val difficultySummary: Map<Difficulty, Int> = emptyMap(),
    val sourceLanguage: String = "",
    val targetLanguage: String = "",
    val isLoading: Boolean = true
) {
    /** طبق بند 37: اگر هیچ آیتمی آماده نباشد "You're all caught up!" نمایش داده شود. */
    val allCaughtUp: Boolean
        get() = !isLoading && dueDaily == 0 && dueWeekly == 0 && dueMonthly == 0
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val languagePairRepository: LanguagePairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            languagePairRepository.observeActivePair().collect { pair ->
                val current = _uiState.value
                _uiState.value = current.copy(sourceLanguage = pair?.sourceLanguage.orEmpty(), targetLanguage = pair?.targetLanguage.orEmpty())
            }
        }
        viewModelScope.launch {
            val now = DateTimeUtils.now()
            val daily = learningStateRepository.countDue(LearningStage.DAILY, now)
            val weekly = learningStateRepository.countDue(LearningStage.WEEKLY, now)
            val monthly = learningStateRepository.countDue(LearningStage.MONTHLY, now)
            val difficultySummary = learningStateRepository.getDifficultySummary()

            conceptRepository.observeActiveCount().collect { total ->
                _uiState.value = HomeUiState(
                    totalWords = total,
                    dueDaily = daily,
                    dueWeekly = weekly,
                    dueMonthly = monthly,
                    difficultySummary = difficultySummary,
                    sourceLanguage = _uiState.value.sourceLanguage,
                    targetLanguage = _uiState.value.targetLanguage,
                    isLoading = false
                )
            }
        }
    }
}
