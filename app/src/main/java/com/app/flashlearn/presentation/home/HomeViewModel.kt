package com.app.flashlearn.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.usecase.GetStreakDaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val dailyCount: Int = 0,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    val learnedCount: Int = 0,
    val streakDays: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val learningStateRepository: LearningStateRepository,
    private val getStreakDaysUseCase: GetStreakDaysUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val streak = getStreakDaysUseCase()
            val daily = learningStateRepository.countByStage(ReviewStage.DAILY)
            val weekly = learningStateRepository.countByStage(ReviewStage.WEEKLY)
            val monthly = learningStateRepository.countByStage(ReviewStage.MONTHLY)
            val learned = learningStateRepository.countByStage(ReviewStage.LEARNED)
            _uiState.value = HomeUiState(
                dailyCount = daily,
                weeklyCount = weekly,
                monthlyCount = monthly,
                learnedCount = learned,
                streakDays = streak,
                isLoading = false
            )
        }
    }
}
