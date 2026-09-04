package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Category

interface CategoryRepository {
    suspend fun getAllCategories(): List<Category>
    suspend fun addCategory(category: Category): Long
}
