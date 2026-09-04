package com.app.flashlearn.data.backup

import com.app.flashlearn.data.backup.dto.ConceptBackupDto
import com.app.flashlearn.data.backup.dto.ContentBackupDto
import com.app.flashlearn.data.backup.dto.LearningStateBackupDto
import com.app.flashlearn.data.backup.dto.SettingsBackupFile
import com.app.flashlearn.data.backup.dto.WordsBackupFile
import com.app.flashlearn.database.dao.ConceptDao
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.Content
import com.app.flashlearn.domain.model.ContentType
import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.ImportSummary
import com.app.flashlearn.domain.model.LanguagePair
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.domain.model.Tag
import com.app.flashlearn.domain.repository.AppSettingsRepository
import com.app.flashlearn.domain.repository.CategoryRepository
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.service.BackupService
import com.app.flashlearn.domain.usecase.AddConceptUseCase
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

// نکته مهم معماری: رفرنس بین فایل Backup و دیتابیس با uuid است نه id داخلی،
// چون id های autoincrement بین دستگاه‌ها/نصب مجدد فرق می‌کنند.
class JsonBackupServiceImpl @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val conceptDao: ConceptDao,
    private val learningStateRepository: LearningStateRepository,
    private val categoryRepository: CategoryRepository,
    private val languagePairRepository: LanguagePairRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val addConceptUseCase: AddConceptUseCase
) : BackupService {

    private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

    private suspend fun loadAllConcepts(): List<Concept> {
        val all = mutableListOf<Concept>()
        var offset = 0
        val pageSize = 200
        while (true) {
            val page = conceptRepository.getConceptsPaged(pageSize, offset)
            if (page.isEmpty()) break
            all += page
            offset += pageSize
        }
        return all
    }

    override suspend fun exportWordsOnly(): String {
        val allConcepts = loadAllConcepts()
        val categories = categoryRepository.getAllCategories().associateBy { it.id }

        val dtos = allConcepts.map { concept ->
            ConceptBackupDto(
                uuid = concept.uuid,
                contentType = concept.contentType.name,
                categoryName = concept.categoryId?.let { categories[it]?.name },
                favorite = concept.favorite,
                contents = concept.contents.map {
                    ContentBackupDto(it.languageCode, it.text, it.pronunciation, it.definition, it.example, it.grammarNote, it.usageNote)
                },
                tags = concept.tags.map { it.name }
            )
        }

        return json.encodeToString(WordsBackupFile(exportedAt = System.currentTimeMillis(), concepts = dtos))
    }

    override suspend fun exportSettingsAndProgress(): String {
        val allConcepts = loadAllConcepts()

        val states = allConcepts.mapNotNull { concept ->
            concept.learningState?.let { state ->
                LearningStateBackupDto(
                    conceptUuid = concept.uuid,
                    stage = state.stage.name,
                    difficulty = state.difficulty.name,
                    nextReviewAt = state.nextReviewAt,
                    monthlyWrongCount = state.monthlyWrongCount,
                    totalCorrect = state.totalCorrect,
                    totalWrong = state.totalWrong,
                    lastReviewedAt = state.lastReviewedAt
                )
            }
        }

        val settings = appSettingsRepository.getSettings()
        val activePair = languagePairRepository.getActivePair()

        return json.encodeToString(
            SettingsBackupFile(
                exportedAt = System.currentTimeMillis(),
                theme = settings.theme,
                activeSourceLanguage = activePair?.sourceLanguage,
                activeTargetLanguage = activePair?.targetLanguage,
                learningStates = states
            )
        )
    }

    override suspend fun importWordsOnly(json: String): Result<ImportSummary> = try {
        val file = this.json.decodeFromString(WordsBackupFile.serializer(), json)
        var imported = 0
        var skipped = 0
        var failed = 0

        file.concepts.forEach { dto ->
            try {
                if (conceptRepository.isDuplicate(dto.uuid)) {
                    skipped++
                } else {
                    val now = System.currentTimeMillis()
                    val concept = Concept(
                        uuid = dto.uuid,
                        contentType = ContentType.valueOf(dto.contentType),
                        categoryId = null,
                        favorite = dto.favorite,
                        active = true,
                        createdAt = now,
                        updatedAt = now,
                        contents = dto.contents.map {
                            Content(conceptId = 0, languageCode = it.languageCode, text = it.text,
                                pronunciation = it.pronunciation, definition = it.definition,
                                example = it.example, grammarNote = it.grammarNote, usageNote = it.usageNote)
                        },
                        tags = dto.tags.map { Tag(name = it) }
                    )
                    addConceptUseCase(concept)
                    imported++
                }
            } catch (e: Exception) {
                failed++
            }
        }
        Result.success(ImportSummary(imported, skipped, failed))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun importSettingsAndProgress(json: String): Result<ImportSummary> = try {
        val file = this.json.decodeFromString(SettingsBackupFile.serializer(), json)
        var imported = 0
        var skipped = 0
        var failed = 0

        file.learningStates.forEach { dto ->
            try {
                val concept = conceptDao.getByUuid(dto.conceptUuid)
                if (concept == null) {
                    skipped++
                } else {
                    learningStateRepository.upsertState(
                        LearningState(
                            conceptId = concept.id,
                            stage = ReviewStage.valueOf(dto.stage),
                            difficulty = Difficulty.valueOf(dto.difficulty),
                            nextReviewAt = dto.nextReviewAt,
                            monthlyWrongCount = dto.monthlyWrongCount,
                            totalCorrect = dto.totalCorrect,
                            totalWrong = dto.totalWrong,
                            lastReviewedAt = dto.lastReviewedAt
                        )
                    )
                    imported++
                }
            } catch (e: Exception) {
                failed++
            }
        }

        if (file.activeSourceLanguage != null && file.activeTargetLanguage != null) {
            val id = languagePairRepository.addPair(
                LanguagePair(sourceLanguage = file.activeSourceLanguage, targetLanguage = file.activeTargetLanguage, isActive = true)
            )
            languagePairRepository.setActivePair(id)
        }

        Result.success(ImportSummary(imported, skipped, failed))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
