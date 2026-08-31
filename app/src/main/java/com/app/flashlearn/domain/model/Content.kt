package com.app.flashlearn.domain.model

data class Content(
    val id: Long,
    val conceptId: Long,
    val languageCode: String,
    val text: String,
    val pronunciation: String? = null,
    val definition: String? = null,
    val example: String? = null,
    val grammarNote: String? = null,
    val usageNote: String? = null
)
