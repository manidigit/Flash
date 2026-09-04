package com.app.flashlearn.data.mapper

import com.app.flashlearn.database.entity.ContentEntity
import com.app.flashlearn.domain.model.Content

fun ContentEntity.toDomain(): Content = Content(
    id = id, conceptId = conceptId, languageCode = languageCode, text = text,
    pronunciation = pronunciation, definition = definition, example = example,
    grammarNote = grammarNote, usageNote = usageNote
)

fun Content.toEntity(): ContentEntity = ContentEntity(
    id = id, conceptId = conceptId, languageCode = languageCode, text = text,
    pronunciation = pronunciation, definition = definition, example = example,
    grammarNote = grammarNote, usageNote = usageNote
)
