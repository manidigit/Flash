package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Category
import kotlinx.coroutines.flow.Flow

/** مدیریت Category ها (بند 15): لیست پیش‌فرض + امکان ساخت Category جدید توسط کاربر. */
interface CategoryRepository {
    fun observeAll(): Flow<List<Category>>
    suspend fun getOrCreate(name: String): Long
}
