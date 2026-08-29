package com.app.flashlearn.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.model.Category
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.repository.CategoryRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * تعداد کلمات یک گزینه نوع مرور: total = کل کلمات این مرحله/سختی (با فیلتر دسته‌بندی فعلی
 * اگر انتخاب شده)، due = چند تای آن‌ها همین الان واقعاً آماده مرورند (فقط برای مراحل
 * زمان‌بندی‌شده DAILY/WEEKLY/MONTHLY/RANDOM معنی دارد؛ برای سختی‌ها و یادگرفته‌شده‌ها due
 * همیشه null است چون این‌ها اصلاً مبتنی بر تاریخ نیستند).
 */
data class ReviewTypeCounts(val total: Int, val due: Int? = null)

data class ReviewTypeSelectUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val counts: Map<String, ReviewTypeCounts> = emptyMap()
)

/**
 * فیلتر ترکیبی (بند 30): کاربر ابتدا (اختیاری) یک Category انتخاب می‌کند، سپس نوع مرور را؛
 * هر دو با هم به ReviewSession فرستاده می‌شوند (مثال دقیق بند 30: «سخت + سفر + اسپانیایی»).
 *
 * درخواست کاربر: جلوی هر گزینه نوع مرور، تعداد کل کلمات آن مرحله و تعداد واقعاً آماده
 * (Due) نمایش داده شود (مثلاً «۲۰۰ کلمه • ۳۰ آماده»)، طبق دسته‌بندی انتخاب‌شده فعلی.
 */
@HiltViewModel
class ReviewTypeSelectViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val learningStateRepository: LearningStateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewTypeSelectUiState())
    val uiState: StateFlow<ReviewTypeSelectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
        loadCounts()
    }

    fun onCategorySelected(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        loadCounts()
    }

    private fun loadCounts() {
        val categoryId = _uiState.value.selectedCategoryId
        viewModelScope.launch {
            val now = DateTimeUtils.now()

            val dailyTotal = learningStateRepository.countTotal(LearningStage.DAILY, categoryId)
            val dailyDue = learningStateRepository.countDue(LearningStage.DAILY, now, categoryId)
            val weeklyTotal = learningStateRepository.countTotal(LearningStage.WEEKLY, categoryId)
            val weeklyDue = learningStateRepository.countDue(LearningStage.WEEKLY, now, categoryId)
            val monthlyTotal = learningStateRepository.countTotal(LearningStage.MONTHLY, categoryId)
            val monthlyDue = learningStateRepository.countDue(LearningStage.MONTHLY, now, categoryId)

            val easy = learningStateRepository.countByDifficulty(Difficulty.EASY, categoryId)
            val medium = learningStateRepository.countByDifficulty(Difficulty.MEDIUM, categoryId)
            val hard = learningStateRepository.countByDifficulty(Difficulty.HARD, categoryId)
            val veryHard = learningStateRepository.countByDifficulty(Difficulty.VERY_HARD, categoryId)
            val learned = learningStateRepository.countLearned(categoryId)

            val counts = mapOf(
                "RANDOM" to ReviewTypeCounts(
                    total = dailyTotal + weeklyTotal + monthlyTotal,
                    due = dailyDue + weeklyDue + monthlyDue
                ),
                LearningStage.DAILY.name to ReviewTypeCounts(dailyTotal, dailyDue),
                LearningStage.WEEKLY.name to ReviewTypeCounts(weeklyTotal, weeklyDue),
                LearningStage.MONTHLY.name to ReviewTypeCounts(monthlyTotal, monthlyDue),
                Difficulty.EASY.name to ReviewTypeCounts(easy),
                Difficulty.MEDIUM.name to ReviewTypeCounts(medium),
                Difficulty.HARD.name to ReviewTypeCounts(hard),
                Difficulty.VERY_HARD.name to ReviewTypeCounts(veryHard),
                "LEARNED" to ReviewTypeCounts(learned)
            )

            _uiState.value = _uiState.value.copy(counts = counts)
        }
    }
}
