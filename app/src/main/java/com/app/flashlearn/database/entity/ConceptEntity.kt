package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * موجودیت مرکزی دیتابیس. هر Concept یک واحد یادگیری است (کلمه/عبارت/جمله/...)
 * که می‌تواند چند ترجمه (ContentEntity) در زبان‌های مختلف داشته باشد.
 * uuid برای Import/Export بین دستگاه‌ها و جلوگیری از Collision استفاده می‌شود.
 */
@Entity(
    tableName = "concepts",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["categoryId"]),
        Index(value = ["active"]),
        Index(value = ["contentType"])
    ]
)
data class ConceptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String,
    // WORD, PHRASE, SENTENCE, IDIOM, VERB, EXPRESSION, DIALOGUE
    val contentType: String,
    val categoryId: Long? = null,
    val favorite: Boolean = false,
    val active: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String? = null
)
