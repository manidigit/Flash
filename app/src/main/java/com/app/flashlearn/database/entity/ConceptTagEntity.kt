package com.app.flashlearn.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "concept_tags",
    primaryKeys = ["conceptId", "tagId"],
    foreignKeys = [
        ForeignKey(entity = ConceptEntity::class, parentColumns = ["id"], childColumns = ["conceptId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TagEntity::class, parentColumns = ["id"], childColumns = ["tagId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class ConceptTagEntity(
    val conceptId: Long,
    val tagId: Long
)
