package com.app.flashlearn.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "concept",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["categoryId"]),
        Index(value = ["active"]),
        Index(value = ["favorite"]),
        Index(value = ["contentType"])
    ]
)
data class ConceptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String,
    val contentType: String,
    val categoryId: Long?,
    val favorite: Boolean = false,
    val active: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)
