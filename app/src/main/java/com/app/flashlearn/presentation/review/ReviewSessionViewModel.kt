package com.app.flashlearn.presentation.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.repository.ReviewHistoryRepository
import com.app.flashlearn.domain.repository.ReviewSessionRepository
import com.app.flashlearn.domain.usecase.ProcessReviewAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewSessionUiState(
    val queue: List<Concept> = emptyList(),
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val isFlipped: Boolean = false,
    val isLoading: Boolean = true,
    val isFinished: Boolean = false,
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa"
) {
    val currentConcept: Concept?
        get() = queue.getOrNull(currentIndex)

    val totalCount: Int get() = queue.size
    val progressLabel: String get() = "${currentIndex.coerceAtMost(totalCount)} / $totalCount"
}

/**
 * منطق کامل یک جلسه مرور: گرفتن کارت‌های آماده (طبق due-date)، پردازش هر پاسخ با
 * ProcessReviewAnswerUseCase، ذخیره LearningState جدید، و ثبت ReviewHistory (بند 32-35 و 65).
 */
@HiltViewModel
class ReviewSessionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val reviewHistoryRepository: ReviewHistoryRepository,
    private val reviewSessionRepository: ReviewSessionRepository,
    private val languagePairRepository: LanguagePairRepository,
    private val processReviewAnswer: ProcessReviewAnswerUseCase
) : ViewModel() {

    private val reviewType: String = savedStateHandle.get<String>("reviewType") ?: "DAILY"
    private val categoryId: Long? = savedStateHandle.get<String>("categoryId")?.toLongOrNull()

    private val _uiState = MutableStateFlow(ReviewSessionUiState())
    val uiState: StateFlow<ReviewSessionUiState> = _uiState.asStateFlow()

    private var sessionId: String? = null
    private var lastCardShownAt: Long = DateTimeUtils.now()

    init {
        loadQueue()
    }

    private fun loadQueue() {
        viewModelScope.launch {
            val activePair = languagePairRepository.observeActivePair().first()
            val now = DateTimeUtils.now()

            val concepts: List<Concept> = when (reviewType) {
                LearningStage.DAILY.name -> dueConceptsFor(LearningStage.DAILY, now)
                LearningStage.WEEKLY.name -> dueConceptsFor(LearningStage.WEEKLY, now)
                LearningStage.MONTHLY.name -> dueConceptsFor(LearningStage.MONTHLY, now)
                Difficulty.EASY.name -> conceptsForDifficulty(Difficulty.EASY)
                Difficulty.MEDIUM.name -> conceptsForDifficulty(Difficulty.MEDIUM)
                Difficulty.HARD.name -> conceptsForDifficulty(Difficulty.HARD)
                Difficulty.VERY_HARD.name -> conceptsForDifficulty(Difficulty.VERY_HARD)
                "LEARNED" -> learningStateRepository.getLearned(limit = 50, offset = 0, categoryId = categoryId)
                    .mapNotNull { conceptRepository.getById(it.conceptId) }
                else -> conceptRepository.getPage(limit = 30, offset = 0, categoryId = categoryId) // RANDOM
            }

            sessionId = reviewSessionRepository.startSession(reviewType, now)
            lastCardShownAt = DateTimeUtils.now()

            _uiState.value = _uiState.value.copy(
                queue = concepts,
                isLoading = false,
                isFinished = concepts.isEmpty(),
                sourceLanguage = activePair?.sourceLanguage ?: "es",
                targetLanguage = activePair?.targetLanguage ?: "fa"
            )
        }
    }

    private suspend fun dueConceptsFor(stage: LearningStage, now: Long): List<Concept> {
        val due = learningStateRepository.getDue(stage, now, limit = 50, categoryId = categoryId)
        return due.mapNotNull { conceptRepository.getById(it.conceptId) }
    }

    private suspend fun conceptsForDifficulty(difficulty: Difficulty): List<Concept> {
        val states = learningStateRepository.getByDifficulty(difficulty, limit = 50, offset = 0, categoryId = categoryId)
        return states.mapNotNull { conceptRepository.getById(it.conceptId) }
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(isFlipped = !_uiState.value.isFlipped)
    }

    fun answer(isCorrect: Boolean) {
        val state = _uiState.value
        val concept = state.currentConcept ?: return

        viewModelScope.launch {
            val now = DateTimeUtils.now()
            val currentState = learningStateRepository.get(concept.id) ?: LearningState(conceptId = concept.id)
            val outcome = processReviewAnswer(currentState, isCorrect, now)

            learningStateRepository.save(outcome.newState)
            reviewHistoryRepository.record(
                conceptId = concept.id,
                sessionId = sessionId,
                outcome = outcome,
                isCorrect = isCorrect,
                reviewDate = now,
                responseTimeMs = now - lastCardShownAt
            )

            val nextIndex = state.currentIndex + 1
            lastCardShownAt = DateTimeUtils.now()

            _uiState.value = state.copy(
                currentIndex = nextIndex,
                correctCount = state.correctCount + if (isCorrect) 1 else 0,
                wrongCount = state.wrongCount + if (!isCorrect) 1 else 0,
                isFlipped = false,
                isFinished = nextIndex >= state.queue.size
            )

            if (_uiState.value.isFinished) {
                sessionId?.let { reviewSessionRepository.endSession(it, DateTimeUtils.now()) }
            }
        }
    }

    /**
     * بند 64 (Edge Case «بستن اپ وسط Review»): جواب هر کارت بلافاصله در همان [answer] با
     * LearningState و ReviewHistory ذخیره می‌شود، پس هیچ داده‌ای در صورت بستن اپ وسط جلسه
     * از دست نمی‌رود و فهرست کارت‌های آماده در دفعه بعد دوباره درست محاسبه می‌شود. تنها اثر
     * جانبی این است که ردیف ReviewSession همان جلسه با endedAt=null (باز) باقی می‌ماند.
     * اگر کاربر با دکمه Back یا ناوبری از این صفحه خارج شود (نه Kill شدن کامل پردازه توسط
     * سیستم‌عامل)، onCleared فراخوانی می‌شود؛ اینجا سعی می‌کنیم جلسه را به‌طور تمیز ببندیم.
     * از viewModelScope استفاده نمی‌شود چون در همین لحظه Cancel شده؛ GlobalScope برای این
     * یک نوشتن کوتاه و بی‌ضرر (idempotent - اگر جلسه از قبل بسته شده بود getById هم null
     * برنمی‌گرداند و کاری انجام نمی‌شود) قابل قبول است.
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCleared() {
        super.onCleared()
        val state = _uiState.value
        if (!state.isFinished) {
            val idToClose = sessionId
            if (idToClose != null) {
                GlobalScope.launch {
                    reviewSessionRepository.endSession(idToClose, DateTimeUtils.now())
                }
            }
        }
    }
}
