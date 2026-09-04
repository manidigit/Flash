package com.app.flashlearn.data.backup.dto

import kotlinx.serialization.Serializable

@Serializable
data class ContentBackupDto(
    val languageCode: String,
    val text: String,
    val pronunciation: String? = null,
    val definition: String? = null,
    val example: String? = null,
    val grammarNote: String? = null,
    val usageNote: String? = null
)

@Serializable
data class ConceptBackupDto(
    val uuid: String,
    val contentType: String,
    val categoryName: String? = null,
    val favorite: Boolean = false,
    val contents: List<ContentBackupDto>,
    val tags: List<String> = emptyList()
)

@Serializable
data class WordsBackupFile(
    val version: Int = 1,
    val exportedAt: Long,
    val concepts: List<ConceptBackupDto>
)

@Serializable
data class LearningStateBackupDto(
    val conceptUuid: String,
    val stage: String,
    val difficulty: String,
    val nextReviewAt: Long? = null,
    val monthlyWrongCount: Int = 0,
    val totalCorrect: Int = 0,
    val totalWrong: Int = 0,
    val lastReviewedAt: Long? = null
)

@Serializable
data class SettingsBackupFile(
    val version: Int = 1,
    val exportedAt: Long,
    val theme: String,
    val activeSourceLanguage: String? = null,
    val activeTargetLanguage: String? = null,
    val learningStates: List<LearningStateBackupDto>
)
