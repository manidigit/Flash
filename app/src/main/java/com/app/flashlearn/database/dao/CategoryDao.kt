package com.app.flashlearn.database.dao

import androidx.room.*
import com.app.flashlearn.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: CategoryEntity): Long
    @Update
    suspend fun update(category: CategoryEntity)
    @Delete
    suspend fun delete(category: CategoryEntity)
    @Query("SELECT * FROM category ORDER BY name")
    fun observeAll(): Flow<List<CategoryEntity>>
}
