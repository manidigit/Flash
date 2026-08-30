package com.app.flashlearn.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.flashlearn.core.util.ContentType
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.core.util.Difficulty
import com.app.flashlearn.core.util.LearningStage
import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.database.entity.LearningStateEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * این تست‌ها فقط رفتار Query سطح دیتابیس را بررسی می‌کنند (due-filtering طبق بند 20/22/31).
 * منطق کامل انتقال بین مراحل (UseCase) در فاز Domain و با تست جداگانه پوشش داده می‌شود.
 */
@RunWith(AndroidJUnit4::class)
class LearningStateDaoTest {

    private lateinit var db: FlashLearnDatabase

    @Before
    fun setUp() {
        db = DatabaseTestUtil.createInMemoryDb()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun insertConcept(): Long {
        val now = DateTimeUtils.now()
        return db.conceptDao().insert(
            ConceptEntity(
                uuid = UUID.randomUUID().toString(),
                contentType = ContentType.WORD,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    @Test
    fun getDueForStage_onlyReturnsItemsPastNextReviewAt() = runBlocking {
        val now = DateTimeUtils.now()

        val dueConceptId = insertConcept()
        db.learningStateDao().insert(
            LearningStateEntity(
                conceptId = dueConceptId,
                stage = LearningStage.WEEKLY,
                nextReviewAt = now - 1000
            )
        )

        val notDueConceptId = insertConcept()
        db.learningStateDao().insert(
            LearningStateEntity(
                conceptId = notDueConceptId,
                stage = LearningStage.WEEKLY,
                nextReviewAt = now + DateTimeUtils.addDays(0, 7)
            )
        )

        val due = db.learningStateDao().getDueForStage(LearningStage.WEEKLY, now, limit = 50)

        assertEquals(1, due.size)
        assertEquals(dueConceptId, due[0].conceptId)
    }

    @Test
    fun countDueForStage_matchesActualDueItems() = runBlocking {
        val now = DateTimeUtils.now()

        repeat(10) {
            val id = insertConcept()
            db.learningStateDao().insert(
                LearningStateEntity(
                    conceptId = id,
                    stage = LearningStage.MONTHLY,
                    nextReviewAt = now - 1000
                )
            )
        }
        repeat(5) {
            val id = insertConcept()
            db.learningStateDao().insert(
                LearningStateEntity(
                    conceptId = id,
                    stage = LearningStage.MONTHLY,
                    nextReviewAt = now + 999_999
                )
            )
        }

        val count = db.learningStateDao().countDueForStage(LearningStage.MONTHLY, now)
        assertEquals(10, count)
    }

    @Test
    fun difficultySummary_groupsCorrectly() = runBlocking {
        val id1 = insertConcept()
        val id2 = insertConcept()
        val id3 = insertConcept()

        db.learningStateDao().insert(LearningStateEntity(conceptId = id1, difficulty = Difficulty.EASY))
        db.learningStateDao().insert(LearningStateEntity(conceptId = id2, difficulty = Difficulty.EASY))
        db.learningStateDao().insert(LearningStateEntity(conceptId = id3, difficulty = Difficulty.HARD))

        val summary = db.learningStateDao().getDifficultySummary().associate { it.difficulty to it.count }

        assertEquals(2, summary[Difficulty.EASY])
        assertEquals(1, summary[Difficulty.HARD])
    }
}
