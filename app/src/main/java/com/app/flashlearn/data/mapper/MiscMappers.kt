package com.app.flashlearn.data.mapper

import com.app.flashlearn.database.entity.CategoryEntity
import com.app.flashlearn.database.entity.LanguagePairEntity
import com.app.flashlearn.database.entity.TagEntity
import com.app.flashlearn.domain.model.Category
import com.app.flashlearn.domain.model.LanguagePair
import com.app.flashlearn.domain.model.Tag

fun CategoryEntity.toDomain() = Category(id, name, isCustom)
fun Category.toEntity() = CategoryEntity(id, name, isCustom)

fun TagEntity.toDomain() = Tag(id, name)
fun Tag.toEntity() = TagEntity(id, name)

fun LanguagePairEntity.toDomain() = LanguagePair(id, sourceLanguage, targetLanguage, isActive)
fun LanguagePair.toEntity() = LanguagePairEntity(id, sourceLanguage, targetLanguage, isActive)
