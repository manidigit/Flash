package com.app.flashlearn.domain.model

data class LanguagePair(
    val id: Long,
    val sourceLanguage: String,
    val targetLanguage: String,
    val isActive: Boolean
)
