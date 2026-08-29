package com.app.flashlearn.domain.repository

import com.app.flashlearn.domain.model.Difficulty
import com.app.flashlearn.domain.model.LearningStage
import com.app.flashlearn.domain.model.LearningState

interface LearningStateRepository {
    suspend fun get(conceptId: Long): LearningState?
    suspend fun save(state: LearningState)
    suspend fun getDue(stage: LearningStage, now: Long, limit: Int, categoryId: Long? = null): List<LearningState>
    suspend fun countDue(stage: LearningStage, now: Long, categoryId: Long? = null): Int
    /**
     * درخواست کاربر: نمایش «چند کلمه در این مرحله هست» و «چند تا از الان آماده مرورند»
     * جلوی هر گزینه در صفحه انتخاب نوع مرور، تا فرق مثلاً «۲۰۰ کلمه در هفتگی» و «۳۰ تای
     * آماده الان» مشخص باشد.
     */
    suspend fun countTotal(stage: LearningStage, categoryId: Long? = null): Int
    suspend fun getByDifficulty(difficulty: Difficulty, limit: Int, offset: Int, categoryId: Long? = null): List<LearningState>
    suspend fun countByDifficulty(difficulty: Difficulty, categoryId: Long? = null): Int
    suspend fun getLearned(limit: Int, offset: Int, categoryId: Long? = null): List<LearningState>
    suspend fun countLearned(categoryId: Long? = null): Int
    suspend fun getDifficultySummary(): Map<Difficulty, Int>
    suspend fun getStageSummary(): Map<LearningStage, Int>
}
