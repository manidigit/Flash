package com.app.flashlearn.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.ReviewAnswer
import com.app.flashlearn.domain.usecase.SubmitReviewAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val submitReviewUseCase: SubmitReviewAnswerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun submitAnswer(conceptId: Long, answer: ReviewAnswer, sessionId: String, attemptId: String) {
        viewModelScope.launch {
            val now = Instant.now()
            val result = submitReviewUseCase(
                conceptId = conceptId,
                answer = answer,
                sessionId = sessionId,
                attemptId = attemptId,
                responseTimeMs = null, // می‌توانید زمان پاسخ را محاسبه کنید
                now = now
            )
            result.onSuccess { transition ->
                _uiState.value = _uiState.value.copy(
                    lastTransition = transition,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message
                )
            }
        }
    }
}

data class ReviewUiState(
    val currentConcept: com.app.flashlearn.domain.model.Concept? = null,
    val currentContent: com.app.flashlearn.domain.model.Content? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val lastTransition: com.app.flashlearn.domain.model.ReviewTransition? = null
)
