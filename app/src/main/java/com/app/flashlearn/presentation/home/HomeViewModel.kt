package com.app.flashlearn.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.database.dao.AppSettingsDao
import com.app.flashlearn.database.dao.ConceptDao
import com.app.flashlearn.database.dao.LearningStateDao
import com.app.flashlearn.database.dao.ReviewHistoryDao
import com.app.flashlearn.domain.model.Difficulty
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

data class HomeUiState(
    val sourceLanguage: String = "fa",
    val targetLanguage: String = "en",
    val totalWords: Int = 0,
    val dueDaily: Int = 0,
    val dueWeekly: Int = 0,
    val dueMonthly: Int = 0,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0,
    val practicedCount: Int = 0,
    val learnedCount: Int = 0,
    val streakDays: Int = 0,
    val difficultySummary: Map<Difficulty, Int> = emptyMap()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val conceptDao: ConceptDao,
    private val learningStateDao: LearningStateDao,
    private val reviewHistoryDao: ReviewHistoryDao,
    private val appSettingsDao: AppSettingsDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val group1 = combine(
            appSettingsDao.getSettings(),
            conceptDao.getActiveCount(),
            learningStateDao.getCountByStage("WEEKLY"),
            learningStateDao.getCountByStage("MONTHLY"),
            learningStateDao.getReadyForReview("DAILY", System.currentTimeMillis()).map { it.size }
        ) { settings, total, weekly, monthly, dailyDue ->
            HomeGroup1(settings?.sourceLanguage ?: "fa", settings?.targetLanguage ?: "en", total, weekly, monthly, dailyDue)
        }

        val group2 = combine(
            learningStateDao.getReadyForReview("WEEKLY", System.currentTimeMillis()).map { it.size },
            learningStateDao.getReadyForReview("MONTHLY", System.currentTimeMillis()).map { it.size },
            reviewHistoryDao.getPracticedConceptCount(),
            learningStateDao.getLearnedCount(),
            reviewHistoryDao.getReviewDates()
        ) { weeklyDue, monthlyDue, practiced, learned, dates ->
            HomeGroup2(weeklyDue, monthlyDue, practiced, learned, dates)
        }

        combine(group1, group2) { g1, g2 ->
            HomeUiState(
                sourceLanguage = g1.sourceLanguage,
                targetLanguage = g1.targetLanguage,
                totalWords = g1.total,
                dueDaily = g1.dailyDue,
                dueWeekly = g2.weeklyDue,
                dueMonthly = g2.monthlyDue,
                weeklyCount = g1.weekly,
                monthlyCount = g1.monthly,
                practicedCount = g2.practiced,
                learnedCount = g2.learned,
                streakDays = calculateStreak(g2.dates)
            )
        }.onEach { _uiState.value = it }
            .launchIn(viewModelScope)
    }

    private data class HomeGroup1(
        val sourceLanguage: String,
        val targetLanguage: String,
        val total: Int,
        val weekly: Int,
        val monthly: Int,
        val dailyDue: Int
    )

    private data class HomeGroup2(
        val weeklyDue: Int,
        val monthlyDue: Int,
        val practiced: Int,
        val learned: Int,
        val dates: List<Long>
    )

    fun reverseLanguagePair() {
        viewModelScope.launch {
            val s = appSettingsDao.getSettingsSync() ?: return@launch
            appSettingsDao.updateLanguagePair(s.targetLanguage, s.sourceLanguage)
        }
    }

    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0
        val days = timestamps.map { dayKey(it) }.toSet()
        var cursor = dayKey(System.currentTimeMillis())
        // If today has no activity, streak is still allowed to represent yesterday's
        // ongoing streak; it only becomes zero after a full inactive day.
        if (!days.contains(cursor)) cursor -= 1L
        var streak = 0
        while (days.contains(cursor)) {
            streak++
            cursor--
        }
        return streak
    }

    private fun dayKey(ms: Long): Long {
        val c = Calendar.getInstance()
        c.timeInMillis = ms
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis / 86_400_000L
    }
}
