package com.app.flashlearn.domain.model

data class ImportResult(
    val inserted: Int,
    val updated: Int,
    val skipped: Int
)
