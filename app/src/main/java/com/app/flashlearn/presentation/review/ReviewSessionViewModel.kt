package com.app.flashlearn.presentation.review

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewMode
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.repository.ReviewHistoryRepository
import com.app.flashlearn.domain.repository.ReviewSessionRepository
import com.app.flashlearn.domain.usecase.ProcessReviewAnswerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
    val targetLanguage: String = "fa",
    val reviewMode: ReviewMode = ReviewMode.FLASHCARD,
    val choiceOptions: List<String> = emptyList(),
    val isLoadingChoices: Boolean = false,
    // بند: بعد از انتخاب یک گزینه در تست چهارگزینه‌ای، تا پایان تأخیر بازخورد (بلافاصله
    // جواب پردازش نمی‌شود) این مقدار نگه داشته می‌شود تا UI بتواند گزینه انتخاب‌شده و
    // گزینه درست را رنگ بزند، پیش از رفتن به کارت بعدی.
    val selectedChoiceText: String? = null
) {
    val currentConcept: Concept?
        get() = queue.getOrNull(currentIndex)

    val totalCount: Int get() = queue.size
    val progressLabel: String get() = "${currentIndex.coerceAtMost(totalCount)} / $totalCount"
}

/**
 * منطق کامل یک جلسه مرور: گرفتن کارت‌های آماده (طبق due-date)، پردازش هر پاسخ با
 * ProcessReviewAnswerUseCase، ذخیره LearningState جدید، و ثبت ReviewHistory (بند 32-35 و 65).
 *
 * ویژگی جدید (تست چهارگزینه‌ای): وقتی reviewMode == MULTIPLE_CHOICE است، به‌جای Flip
 * کردن کارت، برای هر کلمه چند گزینه ترجمه (یکی درست، بقیه از کلمات دیگر کتابخانه به‌صورت
 * تصادفی) ساخته می‌شود؛ انتخاب گزینه درست/غلط دقیقاً همان مسیر answer(isCorrect) موجود را
 * صدا می‌زند، پس الگوریتم مرور (بند 32-35) بدون تغییر برای هر دو حالت کار می‌کند.
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

    companion object {
        // مدت زمان نمایش بازخورد رنگی (سبز/قرمز) قبل از رفتن به کارت بعدی در تست چهارگزینه‌ای.
        private const val CHOICE_FEEDBACK_DELAY_MS = 2000L
    }

    private val reviewType: String = savedStateHandle.get<String>("reviewType") ?: "DAILY"
    private val categoryId: Long? = savedStateHandle.get<String>("categoryId")?.toLongOrNull()
    private val reviewMode: ReviewMode = when (savedStateHandle.get<String>("reviewMode")) {
        ReviewMode.MULTIPLE_CHOICE.name -> ReviewMode.MULTIPLE_CHOICE
        else -> ReviewMode.FLASHCARD
    }

    private val _uiState = MutableStateFlow(ReviewSessionUiState(reviewMode = reviewMode))
    val uiState: StateFlow<ReviewSessionUiState> = _uiState.asStateFlow()

    private var sessionId: String? = null
    private var lastCardShownAt: Long = DateTimeUtils.now()

    init {
        loadQueue()
    }

    /**
     * درخواست کاربر: بعد از پایان یک دسته مرور، امکان ادامه‌دادن به دسته بعدی کارت‌های
     * Due (همان نوع/دسته‌بندی/حالت مرور) بدون خروج از این صفحه. اگر کارت آماده دیگری
     * نباشد، همان صفحه «چیزی برای مرور نبود» دوباره نمایش داده می‌شود.
     */
    fun continueReviewing() {
        _uiState.value = ReviewSessionUiState(reviewMode = reviewMode, isLoading = true)
        loadQueue()
    }

    private fun loadQueue() {
        viewModelScope.launch {
            // رفع باگ (Crash در مرور تصادفی): هر خطای غیرمنتظره‌ای اینجا (مثلاً یک ردیف
            // ناسازگار در دیتابیس) قبلاً بدون هیچ try/catch مستقیم به بیرون از برنامه
            // پرتاب می‌شد و کل اپ Force Close می‌شد. حالا چنین خطایی فقط باعث می‌شود
            // صفحه با فهرست خالی («چیزی برای مرور نبود») بسته شود، نه Crash کامل برنامه.
            try {
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
                    // بند 64 (رفع دو باگ):
                    // ۱) قبلاً از conceptRepository.getPage() استفاده می‌شد که کل کتابخانه
                    //    را به‌ترتیب ثابت (بدون توجه به nextReviewAt) برمی‌گرداند؛ یعنی
                    //    کلماتی که تازه به مرحله هفتگی/ماهانه رفته و هنوز موعد مرورشان
                    //    نرسیده هم دوباره نشان داده می‌شدند - برخلاف هدف Spaced Repetition.
                    // ۲) نتیجه هیچ‌وقت Shuffle نمی‌شد، پس هر بار دقیقاً همان ترتیب قبلی
                    //    تکرار می‌شد و اصلاً "تصادفی" نبود.
                    else -> loadRandomDueConcepts(now)
                }

                sessionId = reviewSessionRepository.startSession(reviewType, now)
                lastCardShownAt = DateTimeUtils.now()

                val sourceLanguage = activePair?.sourceLanguage ?: "es"
                val targetLanguage = activePair?.targetLanguage ?: "fa"

                _uiState.value = _uiState.value.copy(
                    queue = concepts,
                    isLoading = false,
                    isFinished = concepts.isEmpty(),
                    sourceLanguage = sourceLanguage,
                    targetLanguage = targetLanguage
                )

                if (reviewMode == ReviewMode.MULTIPLE_CHOICE && concepts.isNotEmpty()) {
                    loadChoiceOptionsForCurrent(targetLanguage)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    queue = emptyList(),
                    isLoading = false,
                    isFinished = true
                )
            }
        }
    }

    private suspend fun dueConceptsFor(stage: LearningStage, now: Long): List<Concept> {
        val due = learningStateRepository.getDue(stage, now, limit = 50, categoryId = categoryId)
        return due.mapNotNull { conceptRepository.getById(it.conceptId) }
    }

    /**
     * مرور تصادفی واقعی: کارت‌های Due در هر سه مرحله را جمع می‌کند، به‌هم می‌ریزد
     * (Shuffle)، و حداکثر ۳۰ مورد اول را برمی‌گرداند. ترتیب هر بار متفاوت است چون
     * shuffled() هر فراخوانی یک ترتیب تصادفی جدید تولید می‌کند.
     */
    private suspend fun loadRandomDueConcepts(now: Long): List<Concept> {
        val dueStages = listOf(LearningStage.DAILY, LearningStage.WEEKLY, LearningStage.MONTHLY)
        val allDueStates = dueStages.flatMap { stage ->
            learningStateRepository.getDue(stage, now, limit = 50, categoryId = categoryId)
        }
        return allDueStates
            .shuffled()
            .take(30)
            .mapNotNull { conceptRepository.getById(it.conceptId) }
    }

    private suspend fun conceptsForDifficulty(difficulty: Difficulty): List<Concept> {
        val states = learningStateRepository.getByDifficulty(difficulty, limit = 50, offset = 0, categoryId = categoryId)
        return states.mapNotNull { conceptRepository.getById(it.conceptId) }
    }

    fun flipCard() {
        _uiState.value = _uiState.value.copy(isFlipped = !_uiState.value.isFlipped)
    }

    /**
     * ساخت گزینه‌های تست چهارگزینه‌ای برای کارت فعلی: یک گزینه درست (اولین ترجمه معتبر
     * در زبان مقصد) به‌همراه حداکثر ۳ گزینه غلط تصادفی از کلمات دیگر. اگر کتابخانه آن‌قدر
     * کوچک باشد که ۳ گزینه غلط متمایز پیدا نشود (مثلاً تازه چند کلمه اضافه شده)، همان
     * تعداد کمتر نمایش داده می‌شود؛ بهتر از این است که برنامه Crash کند یا گزینه تکراری
     * نشان بدهد.
     */
    private suspend fun loadChoiceOptionsForCurrent(targetLanguage: String) {
        val concept = _uiState.value.currentConcept
        if (concept == null) {
            _uiState.value = _uiState.value.copy(choiceOptions = emptyList(), isLoadingChoices = false)
            return
        }

        _uiState.value = _uiState.value.copy(isLoadingChoices = true)

        // رفع باگ Crash: اگر ساخت گزینه‌ها به هر دلیلی (مثلاً خطای دیتابیس) شکست بخورد،
        // به‌جای پرتاب Exception و بستن کل برنامه، فقط این یک کارت بدون گزینه (خالی) رد
        // می‌شود؛ کاربر می‌تواند با دکمه خروج از جلسه خارج شود، ولی برنامه Crash نمی‌کند.
        try {
            val correctText = concept.contentFor(targetLanguage)?.text
            if (correctText.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(choiceOptions = emptyList(), isLoadingChoices = false)
                return
            }

            val validMeanings = concept.contentsFor(targetLanguage).map { it.text.trim().lowercase() }.toSet()
            val rawDistractors = conceptRepository.getRandomTranslations(
                languageCode = targetLanguage,
                excludeConceptId = concept.id,
                limit = 8
            )
            val distractors = rawDistractors
                .filter { it.trim().lowercase() !in validMeanings }
                .distinctBy { it.trim().lowercase() }
                .take(3)

            val options = (distractors + correctText).shuffled()
            _uiState.value = _uiState.value.copy(choiceOptions = options, isLoadingChoices = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(choiceOptions = emptyList(), isLoadingChoices = false)
        }
    }

    /**
     * انتخاب یک گزینه در حالت تست چهارگزینه‌ای (رفع باگ: قبلاً بلافاصله و بدون هیچ بازخورد
     * بصری به کارت بعدی می‌رفت). حالا اول فقط گزینه انتخاب‌شده در State ثبت می‌شود تا UI
     * گزینه درست را سبز و گزینه غلط انتخاب‌شده را قرمز نشان دهد، و پردازش واقعی پاسخ
     * (answer) با ۲ ثانیه تأخیر انجام می‌شود. تا وقتی این تأخیر تمام نشده، انتخاب دوباره
     * نادیده گرفته می‌شود (در UI هم دکمه‌ها Disable می‌شوند) تا با تپ سریع دوباره، پاسخ دوبار
     * ثبت نشود.
     */
    fun selectChoice(selectedText: String) {
        val state = _uiState.value
        if (state.selectedChoiceText != null) return
        val concept = state.currentConcept ?: return
        val validMeanings = concept.contentsFor(state.targetLanguage).map { it.text.trim().lowercase() }
        val isCorrect = selectedText.trim().lowercase() in validMeanings

        _uiState.value = state.copy(selectedChoiceText = selectedText)

        viewModelScope.launch {
            delay(CHOICE_FEEDBACK_DELAY_MS)
            answer(isCorrect)
        }
    }

    fun answer(isCorrect: Boolean) {
        val state = _uiState.value
        val concept = state.currentConcept ?: return

        viewModelScope.launch {
            try {
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
                val isFinished = nextIndex >= state.queue.size

                _uiState.value = state.copy(
                    currentIndex = nextIndex,
                    correctCount = state.correctCount + if (isCorrect) 1 else 0,
                    wrongCount = state.wrongCount + if (!isCorrect) 1 else 0,
                    isFlipped = false,
                    isFinished = isFinished,
                    choiceOptions = emptyList(),
                    selectedChoiceText = null
                )

                if (isFinished) {
                    sessionId?.let { reviewSessionRepository.endSession(it, DateTimeUtils.now()) }
                } else if (reviewMode == ReviewMode.MULTIPLE_CHOICE) {
                    loadChoiceOptionsForCurrent(state.targetLanguage)
                }
            } catch (e: Exception) {
                // رفع باگ Crash: خطای غیرمنتظره وسط ثبت پاسخ نباید کل اپ را ببندد؛ جلسه را
                // با نتیجه فعلی (تا همین‌جا) به‌صورت کنترل‌شده تمام می‌کنیم.
                _uiState.value = state.copy(isFinished = true, choiceOptions = emptyList(), selectedChoiceText = null)
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
