package com.app.flashlearn.data.importexport

import androidx.room.withTransaction
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.database.FlashLearnDatabase
import com.app.flashlearn.database.dao.AppSettingsDao
import com.app.flashlearn.database.dao.CategoryDao
import com.app.flashlearn.database.dao.ConceptDao
import com.app.flashlearn.database.dao.ContentDao
import com.app.flashlearn.database.dao.LanguageDao
import com.app.flashlearn.database.dao.LearningStateDao
import com.app.flashlearn.database.dao.ReviewHistoryDao
import com.app.flashlearn.database.dao.ReviewSessionDao
import com.app.flashlearn.database.dao.TagDao
import com.app.flashlearn.database.entity.AppSettingsEntity
import com.app.flashlearn.database.entity.CategoryEntity
import com.app.flashlearn.database.entity.ConceptEntity
import com.app.flashlearn.database.entity.ConceptTagEntity
import com.app.flashlearn.database.entity.ContentEntity
import com.app.flashlearn.database.entity.LanguageEntity
import com.app.flashlearn.database.entity.LearningStateEntity
import com.app.flashlearn.database.entity.ReviewHistoryEntity
import com.app.flashlearn.database.entity.ReviewSessionEntity
import com.app.flashlearn.database.entity.TagEntity
import com.app.flashlearn.domain.model.ConflictResolution
import com.app.flashlearn.domain.model.ImportPreview
import com.app.flashlearn.domain.model.ImportResult
import com.app.flashlearn.domain.repository.BackupRepository
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

private const val SCHEMA_VERSION = 1

/**
 * پیاده‌سازی کامل Export/Import به JSON (بند 44-47).
 * Concept ها بر اساس uuid شناسایی می‌شوند (نه id داخلی) تا بین دستگاه‌های مختلف قابل انتقال باشند.
 * Category با نام (نه id) ارجاع داده می‌شود چون id بین دستگاه‌ها یکسان نیست.
 */
class JsonBackupServiceImpl @Inject constructor(
    private val database: FlashLearnDatabase,
    private val languageDao: LanguageDao,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao,
    private val conceptDao: ConceptDao,
    private val contentDao: ContentDao,
    private val learningStateDao: LearningStateDao,
    private val reviewHistoryDao: ReviewHistoryDao,
    private val reviewSessionDao: ReviewSessionDao,
    private val appSettingsDao: AppSettingsDao
) : BackupRepository {

    override suspend fun exportToJson(): String {
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAt", DateTimeUtils.now())

        root.put("languages", JSONArray().apply {
            languageDao.getAll().forEach {
                put(JSONObject().put("code", it.code).put("displayName", it.displayName))
            }
        })

        val categories = categoryDaoAll()
        val categoryNameById = categories.associate { it.id to it.name }
        root.put("categories", JSONArray().apply {
            categories.forEach { put(JSONObject().put("name", it.name).put("isCustom", it.isCustom)) }
        })

        val conceptsJson = JSONArray()
        val learningStatesJson = JSONArray()
        val historyJson = JSONArray()

        var offset = 0
        val pageSize = 200
        while (true) {
            val page = conceptDao.getPage(pageSize, offset)
            if (page.isEmpty()) break

            for (concept in page) {
                val contents = contentDao.getForConcept(concept.id)
                val tags = tagDao.getTagsForConcept(concept.id).map { it.name }

                val conceptObj = JSONObject()
                    .put("uuid", concept.uuid)
                    .put("contentType", concept.contentType)
                    .put("categoryName", categoryNameById[concept.categoryId])
                    .put("favorite", concept.favorite)
                    .put("active", concept.active)
                    .put("createdAt", concept.createdAt)
                    .put("updatedAt", concept.updatedAt)
                    .put("notes", concept.notes)
                    .put("tags", JSONArray(tags))
                    .put("contents", JSONArray().apply {
                        contents.forEach { c ->
                            put(
                                JSONObject()
                                    .put("languageCode", c.languageCode)
                                    .put("text", c.text)
                                    .put("pronunciation", c.pronunciation)
                                    .put("definition", c.definition)
                                    .put("example", c.example)
                                    .put("grammarNote", c.grammarNote)
                                    .put("usageNote", c.usageNote)
                            )
                        }
                    })
                conceptsJson.put(conceptObj)

                learningStateDao.getForConcept(concept.id)?.let { state ->
                    learningStatesJson.put(
                        JSONObject()
                            .put("conceptUuid", concept.uuid)
                            .put("stage", state.stage)
                            .put("difficulty", state.difficulty)
                            .put("nextReviewAt", state.nextReviewAt)
                            .put("monthlyWrongCount", state.monthlyWrongCount)
                            .put("totalCorrect", state.totalCorrect)
                            .put("totalWrong", state.totalWrong)
                            .put("lastReviewedAt", state.lastReviewedAt)
                            .put("everFailed", state.everFailed)
                    )
                }

                reviewHistoryDao.getForConcept(concept.id).forEach { h ->
                    historyJson.put(
                        JSONObject()
                            .put("conceptUuid", concept.uuid)
                            .put("sessionId", h.sessionId)
                            .put("reviewStage", h.reviewStage)
                            .put("reviewDate", h.reviewDate)
                            .put("isCorrect", h.isCorrect)
                            .put("previousStatus", h.previousStatus)
                            .put("newStatus", h.newStatus)
                            .put("previousDifficulty", h.previousDifficulty)
                            .put("newDifficulty", h.newDifficulty)
                            .put("responseTimeMs", h.responseTimeMs)
                    )
                }
            }
            offset += pageSize
        }

        root.put("concepts", conceptsJson)
        root.put("learningStates", learningStatesJson)
        root.put("reviewHistory", historyJson)

        return root.toString()
    }

    override suspend fun previewImport(json: String): ImportPreview {
        val root = JSONObject(json)
        val conceptsJson = root.optJSONArray("concepts") ?: JSONArray()

        var newCount = 0
        var identicalCount = 0
        val conflicting = mutableListOf<String>()

        for (i in 0 until conceptsJson.length()) {
            val obj = conceptsJson.getJSONObject(i)
            val uuid = obj.getString("uuid")
            val existing = conceptDao.getByUuid(uuid)

            if (existing == null) {
                newCount++
            } else {
                val existingContents = contentDao.getForConcept(existing.id)
                if (isSameContent(existingContents, obj.getJSONArray("contents"))) {
                    identicalCount++
                } else {
                    conflicting.add(uuid)
                }
            }
        }

        return ImportPreview(newCount, identicalCount, conflicting)
    }

    /**
     * بند 64 (Edge Case «Import ناقص»): برخلاف ImportParsedEntriesUseCase، اینجا رکوردها
     * (Concept، LearningState، ReviewHistory) به هم وابسته‌اند و یک uuidToConceptId مشترک
     * بین آن‌ها به اشتراک گذاشته می‌شود. اگر این عملیات وسط راه (کرش برنامه، خطای Parse یک
     * رکورد بعدی، پر شدن حافظه) قطع شود، بدون Transaction ممکن است مثلاً چند Concept درج
     * شده باشند اما LearningState یا ReviewHistoryِ متناظرشان نه — یعنی دیتابیس در وضعیتی
     * ناقص و ناسازگار می‌ماند که با UI برنامه (که همیشه انتظار LearningState برای هر Concept
     * فعال را دارد) جور درنمی‌آید. با withTransaction کل apply یا کامل انجام می‌شود یا اصلاً
     * انجام نمی‌شود (Rollback خودکار Room روی هر Exception)، و AutoBackupWriter هم از قبل
     * یک نسخه پشتیبان کامل گرفته تا در بدترین حالت هم داده‌ای گم نشود.
     */
    override suspend fun applyImport(json: String, resolution: ConflictResolution): ImportResult =
        database.withTransaction { applyImportInternal(json, resolution) }

    private suspend fun applyImportInternal(json: String, resolution: ConflictResolution): ImportResult {
        val root = JSONObject(json)

        // زبان‌ها و دسته‌بندی‌ها را از قبل آماده می‌کنیم تا Foreign Key های Content/Concept برقرار باشند.
        root.optJSONArray("languages")?.let { languages ->
            val entities = (0 until languages.length()).map { idx ->
                val obj = languages.getJSONObject(idx)
                LanguageEntity(code = obj.getString("code"), displayName = obj.getString("displayName"))
            }
            languageDao.insertAll(entities)
        }

        val categoryIdByName = mutableMapOf<String, Long>()
        root.optJSONArray("categories")?.let { cats ->
            for (idx in 0 until cats.length()) {
                val obj = cats.getJSONObject(idx)
                val name = obj.getString("name")
                val existing = categoryDao.findByName(name)
                val id = existing?.id ?: categoryDao.insert(
                    CategoryEntity(name = name, isCustom = obj.optBoolean("isCustom", true))
                )
                categoryIdByName[name] = id
            }
        }

        var inserted = 0
        var updated = 0
        var skipped = 0

        val conceptsJson = root.optJSONArray("concepts") ?: JSONArray()
        val uuidToConceptId = mutableMapOf<String, Long>()

        for (i in 0 until conceptsJson.length()) {
            val obj = conceptsJson.getJSONObject(i)
            val uuid = obj.getString("uuid")
            val existing = conceptDao.getByUuid(uuid)
            val categoryName = if (obj.isNull("categoryName")) null else obj.optString("categoryName")
            val categoryId = categoryName?.let { categoryIdByName[it] }

            if (existing == null) {
                val newId = conceptDao.insert(
                    ConceptEntity(
                        uuid = uuid,
                        contentType = obj.getString("contentType"),
                        categoryId = categoryId,
                        favorite = obj.optBoolean("favorite", false),
                        active = obj.optBoolean("active", true),
                        createdAt = obj.getLong("createdAt"),
                        updatedAt = obj.getLong("updatedAt"),
                        notes = if (obj.isNull("notes")) null else obj.optString("notes")
                    )
                )
                writeContentsAndTags(newId, obj)
                uuidToConceptId[uuid] = newId
                inserted++
            } else {
                val existingContents = contentDao.getForConcept(existing.id)
                val identical = isSameContent(existingContents, obj.getJSONArray("contents"))

                when {
                    identical -> {
                        uuidToConceptId[uuid] = existing.id
                        skipped++
                    }
                    resolution == ConflictResolution.USE_IMPORTED -> {
                        conceptDao.update(
                            existing.copy(
                                contentType = obj.getString("contentType"),
                                categoryId = categoryId,
                                favorite = obj.optBoolean("favorite", existing.favorite),
                                active = obj.optBoolean("active", existing.active),
                                updatedAt = obj.getLong("updatedAt"),
                                notes = if (obj.isNull("notes")) null else obj.optString("notes")
                            )
                        )
                        writeContentsAndTags(existing.id, obj)
                        uuidToConceptId[uuid] = existing.id
                        updated++
                    }
                    resolution == ConflictResolution.MERGE -> {
                        mergeConcept(existing, existingContents, obj, categoryId)
                        uuidToConceptId[uuid] = existing.id
                        updated++
                    }
                    else -> { // KEEP_EXISTING یا SKIP
                        uuidToConceptId[uuid] = existing.id
                        skipped++
                    }
                }
            }
        }

        // Review Session ها (قبل از ReviewHistory چون Foreign Key دارد)
        root.optJSONArray("reviewHistory")?.let { historyArray ->
            val sessionIds = (0 until historyArray.length())
                .mapNotNull { idx -> historyArray.getJSONObject(idx).optString("sessionId", null) }
                .distinct()
            for (sessionId in sessionIds) {
                if (reviewSessionDao.getById(sessionId) == null) {
                    reviewSessionDao.insert(
                        ReviewSessionEntity(id = sessionId, startedAt = DateTimeUtils.now(), reviewType = "IMPORTED")
                    )
                }
            }
        }

        // LearningState (فقط برای Concept هایی که همین حالا Insert/Update شدند وارد می‌شود)
        root.optJSONArray("learningStates")?.let { statesArray ->
            for (idx in 0 until statesArray.length()) {
                val obj = statesArray.getJSONObject(idx)
                val conceptId = uuidToConceptId[obj.getString("conceptUuid")] ?: continue
                learningStateDao.insert(
                    LearningStateEntity(
                        conceptId = conceptId,
                        stage = obj.getString("stage"),
                        difficulty = obj.getString("difficulty"),
                        nextReviewAt = if (obj.isNull("nextReviewAt")) null else obj.getLong("nextReviewAt"),
                        monthlyWrongCount = obj.optInt("monthlyWrongCount", 0),
                        totalCorrect = obj.optInt("totalCorrect", 0),
                        totalWrong = obj.optInt("totalWrong", 0),
                        lastReviewedAt = if (obj.isNull("lastReviewedAt")) null else obj.getLong("lastReviewedAt"),
                        everFailed = obj.optBoolean("everFailed", false)
                    )
                )
            }
        }

        // Review History هرگز رونویسی نمی‌شود، فقط درج (بند 27: هرگز حذف/جایگزین نمی‌شود)
        root.optJSONArray("reviewHistory")?.let { historyArray ->
            for (idx in 0 until historyArray.length()) {
                val obj = historyArray.getJSONObject(idx)
                val conceptId = uuidToConceptId[obj.getString("conceptUuid")] ?: continue
                reviewHistoryDao.insert(
                    ReviewHistoryEntity(
                        conceptId = conceptId,
                        sessionId = if (obj.isNull("sessionId")) null else obj.optString("sessionId"),
                        reviewStage = obj.getString("reviewStage"),
                        reviewDate = obj.getLong("reviewDate"),
                        isCorrect = obj.getBoolean("isCorrect"),
                        previousStatus = obj.getString("previousStatus"),
                        newStatus = obj.getString("newStatus"),
                        previousDifficulty = obj.getString("previousDifficulty"),
                        newDifficulty = obj.getString("newDifficulty"),
                        responseTimeMs = if (obj.isNull("responseTimeMs")) null else obj.getLong("responseTimeMs")
                    )
                )
            }
        }

        return ImportResult(inserted = inserted, updated = updated, skipped = skipped)
    }

    /**
     * Merge فیلد-به-فیلد (بند 47، چهارمین استراتژی): مقدار موجود اولویت دارد؛ فقط فیلدهای
     * خالی از نسخه Import پر می‌شوند. Tags اجتماع دو مجموعه است. Category: اگر Concept موجود
     * از قبل Category داشت همان می‌ماند، وگرنه Category نسخه Import استفاده می‌شود.
     */
    private suspend fun mergeConcept(
        existing: ConceptEntity,
        existingContents: List<ContentEntity>,
        importedObj: JSONObject,
        importedCategoryId: Long?
    ) {
        val mergedCategoryId = existing.categoryId ?: importedCategoryId
        val mergedNotes = existing.notes ?: (if (importedObj.isNull("notes")) null else importedObj.optString("notes"))
        val mergedFavorite = existing.favorite || importedObj.optBoolean("favorite", false)

        conceptDao.update(
            existing.copy(
                categoryId = mergedCategoryId,
                notes = mergedNotes,
                favorite = mergedFavorite,
                updatedAt = DateTimeUtils.now()
            )
        )

        val existingByLanguage = existingContents.associateBy { it.languageCode }
        val importedContentsArray = importedObj.getJSONArray("contents")
        val mergedContents = mutableListOf<ContentEntity>()

        for (idx in 0 until importedContentsArray.length()) {
            val importedContent = importedContentsArray.getJSONObject(idx)
            val lang = importedContent.getString("languageCode")
            val existingContent = existingByLanguage[lang]

            mergedContents.add(
                ContentEntity(
                    conceptId = existing.id,
                    languageCode = lang,
                    text = existingContent?.text?.ifBlank { null } ?: importedContent.getString("text"),
                    pronunciation = existingContent?.pronunciation ?: importedContent.optStringOrNull("pronunciation"),
                    definition = existingContent?.definition ?: importedContent.optStringOrNull("definition"),
                    example = existingContent?.example ?: importedContent.optStringOrNull("example"),
                    grammarNote = existingContent?.grammarNote ?: importedContent.optStringOrNull("grammarNote"),
                    usageNote = existingContent?.usageNote ?: importedContent.optStringOrNull("usageNote")
                )
            )
        }
        // زبان‌هایی که فقط در نسخه موجود بودند (در Import نیامده‌اند) هم حفظ می‌شوند.
        for (existingContent in existingContents) {
            if (mergedContents.none { it.languageCode == existingContent.languageCode }) {
                mergedContents.add(existingContent)
            }
        }

        contentDao.deleteAllForConcept(existing.id)
        contentDao.insertAll(mergedContents)

        val existingTags = tagDao.getTagsForConcept(existing.id).map { it.name }.toSet()
        val importedTags = (importedObj.optJSONArray("tags") ?: JSONArray()).let { arr ->
            (0 until arr.length()).map { arr.getString(it) }.toSet()
        }
        val unionTags = existingTags + importedTags
        for (name in unionTags - existingTags) {
            val existingTag = tagDao.findByName(name)
            val tagId = existingTag?.id ?: tagDao.insertTag(TagEntity(name = name))
            tagDao.insertConceptTag(ConceptTagEntity(conceptId = existing.id, tagId = tagId))
        }
    }

    private fun JSONObject.optStringOrNull(key: String): String? {
        if (isNull(key)) return null
        return optString(key, "").ifBlank { null }
    }

    private suspend fun writeContentsAndTags(conceptId: Long, obj: JSONObject) {
        contentDao.deleteAllForConcept(conceptId)
        val contentsArray = obj.getJSONArray("contents")
        val contents = (0 until contentsArray.length()).map { idx ->
            val c = contentsArray.getJSONObject(idx)
            ContentEntity(
                conceptId = conceptId,
                languageCode = c.getString("languageCode"),
                text = c.getString("text"),
                pronunciation = if (c.isNull("pronunciation")) null else c.optString("pronunciation"),
                definition = if (c.isNull("definition")) null else c.optString("definition"),
                example = if (c.isNull("example")) null else c.optString("example"),
                grammarNote = if (c.isNull("grammarNote")) null else c.optString("grammarNote"),
                usageNote = if (c.isNull("usageNote")) null else c.optString("usageNote")
            )
        }
        contentDao.insertAll(contents)

        val tagsArray = obj.optJSONArray("tags") ?: JSONArray()
        for (idx in 0 until tagsArray.length()) {
            val name = tagsArray.getString(idx)
            val existingTag = tagDao.findByName(name)
            val tagId = existingTag?.id ?: tagDao.insertTag(TagEntity(name = name))
            tagDao.insertConceptTag(ConceptTagEntity(conceptId = conceptId, tagId = tagId))
        }
    }

    private fun isSameContent(existing: List<ContentEntity>, importedArray: JSONArray): Boolean {
        if (existing.size != importedArray.length()) return false
        val existingMap = existing.associate { it.languageCode to it.text }
        for (idx in 0 until importedArray.length()) {
            val obj = importedArray.getJSONObject(idx)
            val lang = obj.getString("languageCode")
            val text = obj.getString("text")
            if (existingMap[lang] != text) return false
        }
        return true
    }

    private suspend fun categoryDaoAll(): List<CategoryEntity> {
        // CategoryDao فقط observeAll (Flow) دارد؛ برای Export یک خواندن لحظه‌ای کافی است.
        return categoryDao.observeAll().first()
    }
}
