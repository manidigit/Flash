package com.app.flashlearn.domain.model

data class TranslationRequest(
    val sourceText: String,
    val sourceLanguageCode: String,
    val targetLanguageCode: String,
    val contentType: ContentType
)

data class TranslationResult(
    val translatedText: String,
    val pronunciation: String? = null,
    val definition: String? = null,
    val example: String? = null,
    val grammarNote: String? = null
)
