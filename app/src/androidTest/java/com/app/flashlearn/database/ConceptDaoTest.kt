package com.app.flashlearn.database

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.flashlearn.core.util.ContentType
import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.database.entity.ContentEntity
import com.app.flashlearn.database.entity.LanguageEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ConceptDaoTest {

    private lateinit var db: FlashLearnDatabase

    @Before
    fun setUp() {
        db = DatabaseTestUtil.createInMemoryDb()
        runBlocking {
            db.languageDao().insertAll(
                listOf(
                    LanguageEntity(code = "es", displayName = "Spanish"),
                    LanguageEntity(code = "fa", displayName = "Persian")
                )
            )
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertConceptWithContents_readsBackCorrectly() = runBlocking {
        val now = System.currentTimeMillis()
        val conceptId = db.conceptDao().insert(
            ConceptEntity(
                uuid = UUID.randomUUID().toString(),
                contentType = ContentType.WORD,
                createdAt = now,
                updatedAt = now
            )
        )

        db.contentDao().insertAll(
            listOf(
                ContentEntity(conceptId = conceptId, languageCode = "es", text = "manzana"),
                ContentEntity(conceptId = conceptId, languageCode = "fa", text = "سیب")
            )
        )

        val contents = db.contentDao().getForConcept(conceptId)
        assertEquals(2, contents.size)

        val esContent = db.contentDao().getForConceptAndLanguage(conceptId, "es")
        assertNotNull(esContent)
        assertEquals("manzana", esContent!!.text)
    }

    @Test
    fun uuid_mustBeUnique() = runBlocking {
        val now = System.currentTimeMillis()
        val uuid = UUID.randomUUID().toString()

        db.conceptDao().insert(
            ConceptEntity(uuid = uuid, contentType = ContentType.WORD, createdAt = now, updatedAt = now)
        )

        var threw = false
        try {
            db.conceptDao().insert(
                ConceptEntity(uuid = uuid, contentType = ContentType.WORD, createdAt = now, updatedAt = now)
            )
        } catch (e: Exception) {
            threw = true
        }
        assertEquals(true, threw)
    }

    @Test
    fun search_findsConceptByTranslatedText() = runBlocking {
        val now = System.currentTimeMillis()
        val conceptId = db.conceptDao().insert(
            ConceptEntity(
                uuid = UUID.randomUUID().toString(),
                contentType = ContentType.WORD,
                createdAt = now,
                updatedAt = now
            )
        )
        db.contentDao().insertAll(
            listOf(
                ContentEntity(conceptId = conceptId, languageCode = "es", text = "casa"),
                ContentEntity(conceptId = conceptId, languageCode = "fa", text = "خانه")
            )
        )

        val results = db.conceptDao().search("خانه", limit = 10, offset = 0)
        assertEquals(1, results.size)
        assertEquals(conceptId, results[0].id)
    }
}
