package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Tag

interface TagRepository {
    suspend fun getTagsForConcept(conceptId: Long): List<Tag>
    suspend fun addTagToConcept(conceptId: Long, tagName: String)
}
