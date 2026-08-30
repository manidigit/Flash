package com.app.flashlearn.presentation.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DuplicateGroupUi(
    val concepts: List<Concept>,
    // شناسه‌های Concept هایی که برای حذف علامت خورده‌اند؛ پیش‌فرض همه به‌جز قدیمی‌ترین
    // (اولین عضو هر گروه) علامت‌خورده‌اند، چون معمولاً کاربر می‌خواهد نسخه اصلی را نگه دارد.
    val markedForDeletion: Set<Long>
)

data class DuplicateWordsUiState(
    val isLoading: Boolean = true,
    val groups: List<DuplicateGroupUi> = emptyList(),
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa",
    val isDeleting: Boolean = false,
    val deletedCount: Int? = null
) {
    val totalMarkedCount: Int get() = groups.sumOf { it.markedForDeletion.size }
}

/**
 * پیدا کردن و پاک کردن کلمات تکراری در واژگان (بند 64، رفع درخواست کاربر). فقط بر اساس
 * زبان مبدأ فعلی گروه‌بندی می‌شود؛ در هر گروه به‌طور پیش‌فرض همه به‌جز قدیمی‌ترین عضو
 * (که معمولاً نسخه اصلی/درست‌تر است) برای حذف علامت می‌خورند، ولی کاربر می‌تواند هرکدام
 * را دستی تغییر دهد.
 */
@HiltViewModel
class DuplicateWordsViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val languagePairRepository: LanguagePairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DuplicateWordsUiState())
    val uiState: StateFlow<DuplicateWordsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val pair = languagePairRepository.observeActivePair().first()
            val sourceLanguage = pair?.sourceLanguage ?: "es"
            val targetLanguage = pair?.targetLanguage ?: "fa"

            val rawGroups = conceptRepository.findDuplicateGroups(sourceLanguage)
            val groups = rawGroups.map { concepts ->
                val sortedByCreatedAt = concepts.sortedBy { it.createdAt }
                val defaultMarked = sortedByCreatedAt.drop(1).map { it.id }.toSet()
                DuplicateGroupUi(concepts = sortedByCreatedAt, markedForDeletion = defaultMarked)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                groups = groups,
                sourceLanguage = sourceLanguage,
                targetLanguage = targetLanguage
            )
        }
    }

    fun toggleMarked(groupIndex: Int, conceptId: Long) {
        val state = _uiState.value
        val group = state.groups.getOrNull(groupIndex) ?: return
        val updatedMarks = if (conceptId in group.markedForDeletion) {
            group.markedForDeletion - conceptId
        } else {
            group.markedForDeletion + conceptId
        }
        val updatedGroups = state.groups.toMutableList()
        updatedGroups[groupIndex] = group.copy(markedForDeletion = updatedMarks)
        _uiState.value = state.copy(groups = updatedGroups)
    }

    fun deleteMarked() {
        val state = _uiState.value
        val idsToDelete = state.groups.flatMap { it.markedForDeletion }
        if (idsToDelete.isEmpty() || state.isDeleting) return

        viewModelScope.launch {
            _uiState.value = state.copy(isDeleting = true)
            idsToDelete.forEach { conceptRepository.archive(it) }
            _uiState.value = DuplicateWordsUiState(
                isLoading = false,
                sourceLanguage = state.sourceLanguage,
                targetLanguage = state.targetLanguage,
                deletedCount = idsToDelete.size
            )
            load()
        }
    }
}
