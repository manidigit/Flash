package com.app.flashlearn.domain.usecase

import com.app.flashlearn.database.dao.ReviewHistoryDao
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class GetStreakDaysUseCase @Inject constructor(
    private val reviewHistoryDao: ReviewHistoryDao,
    private val clock: () -> Long = { System.currentTimeMillis() }
) {
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getDefault()
    }

    suspend operator fun invoke(): Int {
        val days = reviewHistoryDao.getDistinctReviewDays()
        if (days.isEmpty()) return 0

        val today = formatter.format(Date(clock()))
        val yesterday = formatter.format(Date(clock() - 24 * 60 * 60 * 1000L))

        if (days.first() != today && days.first() != yesterday) return 0

        var streak = 0
        val expectedDate = Calendar.getInstance().apply { timeInMillis = clock() }

        if (days.first() != today) {
            expectedDate.add(Calendar.DAY_OF_YEAR, -1)
        }

        for (day in days) {
            val expected = formatter.format(expectedDate.time)
            if (day == expected) {
                streak++
                expectedDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }
}
