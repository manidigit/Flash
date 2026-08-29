package com.app.flashlearn.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.flashlearn.core.util.ContentType
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.core.util.Difficulty
import com.app.flashlearn.core.util.LearningStage
import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.database.entity.ReviewHistoryEntity
import com.app.flashlearn.database.entity.ReviewSessionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ReviewHistoryDaoTest {

    private lateinit var db: FlashLearnDatabase

    @Before
    fun setUp() {
        db = DatabaseTestUtil.createInMemoryDb()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun reviewHistory_isNeverLostAndOrderedByDate() = runBlocking {
        val now = DateTimeUtils.now()
        val conceptId = db.conceptDao().insert(
            ConceptEntity(
                uuid = UUID.randomUUID().toString(),
                contentType = ContentType.WORD,
                createdAt = now,
                updatedAt = now
            )
        )

        val sessionId = "2026-08-16-001"
        db.reviewSessionDao().insert(
            ReviewSessionEntity(id = sessionId, startedAt = now, reviewType = LearningStage.DAILY)
        )

        db.reviewHistoryDao().insert(
            ReviewHistoryEntity(
                conceptId = conceptId,
                sessionId = sessionId,
                reviewStage = LearningStage.DAILY,
                reviewDate = now,
                isCorrect = true,
                previousStatus = LearningStage.DAILY,
                newStatus = LearningStage.WEEKLY,
                previousDifficulty = Difficulty.MEDIUM,
                newDifficulty = Difficulty.MEDIUM
            )
        )
        db.reviewHistoryDao().insert(
            ReviewHistoryEntity(
                conceptId = conceptId,
                sessionId = sessionId,
                reviewStage = LearningStage.WEEKLY,
                reviewDate = now + 1,
                isCorrect = false,
                previousStatus = LearningStage.WEEKLY,
                newStatus = LearningStage.DAILY,
                previousDifficulty = Difficulty.MEDIUM,
                newDifficulty = Difficulty.MEDIUM
            )
        )

        val history = db.reviewHistoryDao().getForConcept(conceptId)
        assertEquals(2, history.size)
        // جدیدترین اول (ORDER BY reviewDate DESC)
        assertEquals(false, history[0].isCorrect)
        assertEquals(true, history[1].isCorrect)
    }

    @Test
    fun countCorrectBetween_onlyCountsWithinRange() = runBlocking {
        val now = DateTimeUtils.now()
        val conceptId = db.conceptDao().insert(
            ConceptEntity(
                uuid = UUID.randomUUID().toString(),
                contentType = ContentType.WORD,
                createdAt = now,
                updatedAt = now
            )
        )

        db.reviewHistoryDao().insert(
            ReviewHistoryEntity(
                conceptId = conceptId, reviewStage = LearningStage.DAILY, reviewDate = now,
                isCorrect = true, previousStatus = "DAILY", newStatus = "WEEKLY",
                previousDifficulty = "MEDIUM", newDifficulty = "MEDIUM"
            )
        )
        db.reviewHistoryDao().insert(
            ReviewHistoryEntity(
                conceptId = conceptId, reviewStage = LearningStage.DAILY, reviewDate = now - 999_999_999,
                isCorrect = true, previousStatus = "DAILY", newStatus = "WEEKLY",
                previousDifficulty = "MEDIUM", newDifficulty = "MEDIUM"
            )
        )

        val correctToday = db.reviewHistoryDao().countCorrectBetween(now - 1000, now + 1000)
        assertEquals(1, correctToday)
    }
}
