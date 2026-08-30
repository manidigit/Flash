package com.app.flashlearn.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.flashlearn.database.DatabaseTestUtil
import com.app.flashlearn.database.FlashLearnDatabase
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.model.ContentType
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

/**
 * تست Round-Trip: Domain Model -> Repository -> Room -> Repository -> Domain Model.
 * هدف اطمینان از صحت Mapper هاست، نه منطق الگوریتم مرور (که در ProcessReviewAnswerUseCaseTest پوشش داده شده).
 */
@RunWith(AndroidJUnit4::class)
class RepositoryRoundTripTest {

    private lateinit var db: FlashLearnDatabase
    private lateinit var conceptRepo: ConceptRepositoryImpl
    private lateinit var learningStateRepo: LearningStateRepositoryImpl

    @Before
    fun setUp() {
        db = DatabaseTestUtil.createInMemoryDb()
        conceptRepo = ConceptRepositoryImpl(db.conceptDao(), db.contentDao(), db.tagDao())
        learningStateRepo = LearningStateRepositoryImpl(db.learningStateDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun concept_withContentsAndTags_roundTripsCorrectly() = runBlocking {
        val now = System.currentTimeMillis()
        val concept = Concept(
            id = 0,
            uuid = UUID.randomUUID().toString(),
            contentType = ContentType.WORD,
            categoryId = null,
            favorite = true,
            active = true,
            createdAt = now,
            updatedAt = now,
            notes = null,
            contents = listOf(
                ContentItem(languageCode = "es", text = "manzana"),
                ContentItem(languageCode = "fa", text = "سیب")
            ),
            tags = listOf("food", "fruit")
        )

        val id = conceptRepo.insert(concept)
        val loaded = conceptRepo.getById(id)

        assertTrue(loaded != null)
        assertEquals(2, loaded!!.contents.size)
        assertEquals("سیب", loaded.contentFor("fa")?.text)
        assertEquals(setOf("food", "fruit"), loaded.tags.toSet())
        assertTrue(loaded.favorite)
    }

    @Test
    fun learningState_roundTripsWithEverFailedFlag() = runBlocking {
        val state = LearningState(
            conceptId = 1,
            stage = LearningStage.DAILY,
            difficulty = Difficulty.HARD,
            everFailed = true,
            monthlyWrongCount = 2
        )

        learningStateRepo.save(state)
        val loaded = learningStateRepo.get(1)

        assertTrue(loaded != null)
        assertEquals(LearningStage.DAILY, loaded!!.stage)
        assertEquals(Difficulty.HARD, loaded.difficulty)
        assertTrue(loaded.everFailed)
        assertEquals(2, loaded.monthlyWrongCount)
    }
}
