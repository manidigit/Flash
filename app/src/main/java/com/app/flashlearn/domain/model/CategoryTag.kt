package com.app.flashlearn.domain.model

data class Category(val id: Long = 0, val name: String, val isCustom: Boolean = true)
data class Tag(val id: Long = 0, val name: String)
