package com.app.flashlearn.domain.model

data class AppSettings(
    val theme: String,
    val activeLanguagePairId: Long?,
    val aiProvider: String?,
    val reviewSettingsJson: String?
)
