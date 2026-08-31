package com.app.flashlearn.domain.model

data class Category(
    val id: Long,
    val name: String,
    val isCustom: Boolean = false
)
