package com.app.flashlearn.data.repository

import com.app.flashlearn.database.dao.TagDao
import com.app.flashlearn.domain.model.Tag
import com.app.flashlearn.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val dao: TagDao
) : TagRepository {

    override fun observeAll(): Flow<List<Tag>> =
        dao.observeAll().map { list -> list.map { Tag(it.id, it.name) } }
}
