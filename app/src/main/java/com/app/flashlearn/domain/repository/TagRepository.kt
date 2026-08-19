package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Tag
import kotlinx.coroutines.flow.Flow

/** مدیریت Tag ها (بند 16). ساخت Tag هنگام ذخیره یک Concept در ConceptRepository انجام
 * می‌شود (Many-to-Many)؛ این Repository برای نمایش/فیلتر Tag های موجود در UI است. */
interface TagRepository {
    fun observeAll(): Flow<List<Tag>>
}
