package com.app.flashlearn.presentation.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.Content
import com.app.flashlearn.domain.model.LanguagePair
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.ReviewRepository
import com.app.flashlearn.domain.usecase.GetDueConceptsUseCase
import com.app.flashlearn.domain.usecase.ProcessReviewAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ReviewUiState {
    data object Loading : ReviewUiState
    data object Empty : ReviewUiState
    data class InProgress(
        val currentIndex: Int,
        val totalCount: Int,
        val currentConcept: Concept,
        val showAnswer: Boolean,
        val sessionId: String
    ) : ReviewUiState
    data class Finished(val correctCount: Int, val wrongCount: Int, val totalCount: Int) : ReviewUiState
    data class Error(val message: String) : ReviewUiState
}

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val getDueConceptsUseCase: GetDueConceptsUseCase,
    private val processReviewAnswerUseCase: ProcessReviewAnswerUseCase,
    private val reviewRepository: ReviewRepository,
    private val languagePairRepository: LanguagePairRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val reviewType: String = savedStateHandle["type"] ?: "DAILY"
    private var activePair: LanguagePair? = null

    private val _uiState = MutableStateFlow<ReviewUiState>(ReviewUiState.Loading)
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private var queue: List<Concept> = emptyList()
    private var sessionId: String = ""
    private var correctCount = 0
    private var wrongCount = 0
    private var startTimeOfCurrentCard = 0L

    init { loadSession() }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.value = ReviewUiState.Loading
            try {
                activePair = languagePairRepository.getActivePair()
                val stage = ReviewStage.valueOf(reviewType)
                queue = getDueConceptsUseCase(stage)
                if (queue.isEmpty()) {
                    _uiState.value = ReviewUiState.Empty
                    return@launch
                }
                val session = reviewRepository.startSession(reviewType)
                sessionId = session.id
                correctCount = 0
                wrongCount = 0
                showCard(0)
            } catch (e: Exception) {
                _uiState.value = ReviewUiState.Error(e.message ?: "خطای نامشخص")
            }
        }
    }

    private fun showCard(index: Int) {
        startTimeOfCurrentCard = System.currentTimeMillis()
        _uiState.value = ReviewUiState.InProgress(
            currentIndex = index,
            totalCount = queue.size,
            currentConcept = queue[index],
            showAnswer = false,
            sessionId = sessionId
        )
    }

    fun revealAnswer() {
        val state = _uiState.value
        if (state is ReviewUiState.InProgress) {
            _uiState.value = state.copy(showAnswer = true)
        }
    }

    fun frontContent(concept: Concept): Content? =
        concept.contents.firstOrNull { it.languageCode == activePair?.sourceLanguage }

    fun backContent(concept: Concept): Content? =
        concept.contents.firstOrNull { it.languageCode == activePair?.targetLanguage }

    fun submitAnswer(isCorrect: Boolean) {
        val state = _uiState.value as? ReviewUiState.InProgress ?: return
        val responseTime = System.currentTimeMillis() - startTimeOfCurrentCard

        viewModelScope.launch {
            processReviewAnswerUseCase(
                conceptId = state.currentConcept.id,
                sessionId = sessionId,
                isCorrect = isCorrect,
                responseTimeMs = responseTime
            )
            if (isCorrect) correctCount++ else wrongCount++

            val nextIndex = state.currentIndex + 1
            if (nextIndex < queue.size) {
                showCard(nextIndex)
            } else {
                reviewRepository.endSession(sessionId, System.currentTimeMillis())
                _uiState.value = ReviewUiState.Finished(correctCount, wrongCount, queue.size)
            }
        }
    }
}
