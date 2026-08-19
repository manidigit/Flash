package com.app.flashlearn.presentation.vocabulary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.model.Category
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.repository.CategoryRepository
import com.app.flashlearn.domain.repository.ConceptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConceptDetailUiState(
    val concept: Concept? = null,
    val sourceLanguage: String = "",
    val targetLanguage: String = "",
    val sourceText: String = "",
    val targetText: String = "",
    val pronunciation: String = "",
    val example: String = "",
    val notes: String = "",
    val tagsText: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val favorite: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false
)

/**
 * ویرایش/حذف/Favorite یک Concept موجود (بند 38)، به‌همراه Category و Tags (بند 15-16).
 * حذف در واقع Archive است (active=false) تا تاریخچه مرور آن هرگز از بین نرود (بند 23 و 27).
 */
@HiltViewModel
class ConceptDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val conceptRepository: ConceptRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val conceptId: Long = checkNotNull(savedStateHandle.get<Long>("conceptId"))

    private val _uiState = MutableStateFlow(ConceptDetailUiState())
    val uiState: StateFlow<ConceptDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val concept = conceptRepository.getById(conceptId)
            if (concept != null) {
                val sourceContent = concept.contents.getOrNull(0)
                val targetContent = concept.contents.getOrNull(1)
                _uiState.value = ConceptDetailUiState(
                    concept = concept,
                    sourceLanguage = sourceContent?.languageCode ?: "",
                    targetLanguage = targetContent?.languageCode ?: "",
                    sourceText = sourceContent?.text ?: "",
                    targetText = targetContent?.text ?: "",
                    pronunciation = sourceContent?.pronunciation ?: "",
                    example = sourceContent?.example ?: targetContent?.example ?: "",
                    notes = concept.notes ?: "",
                    tagsText = concept.tags.joinToString(", "),
                    selectedCategoryId = concept.categoryId,
                    favorite = concept.favorite,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun onSourceTextChanged(value: String) { _uiState.value = _uiState.value.copy(sourceText = value) }
    fun onTargetTextChanged(value: String) { _uiState.value = _uiState.value.copy(targetText = value) }
    fun onPronunciationChanged(value: String) { _uiState.value = _uiState.value.copy(pronunciation = value) }
    fun onExampleChanged(value: String) { _uiState.value = _uiState.value.copy(example = value) }
    fun onNotesChanged(value: String) { _uiState.value = _uiState.value.copy(notes = value) }
    fun onTagsTextChanged(value: String) { _uiState.value = _uiState.value.copy(tagsText = value) }

    fun onCategorySelected(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    fun createCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = categoryRepository.getOrCreate(name.trim())
            _uiState.value = _uiState.value.copy(selectedCategoryId = id)
        }
    }

    fun toggleFavorite() {
        val newValue = !_uiState.value.favorite
        _uiState.value = _uiState.value.copy(favorite = newValue)
        viewModelScope.launch { conceptRepository.setFavorite(conceptId, newValue) }
    }

    fun save() {
        val state = _uiState.value
        val original = state.concept ?: return
        if (state.sourceText.isBlank() || state.targetText.isBlank() || state.isSaving) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val tags = state.tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val updated = original.copy(
                favorite = state.favorite,
                categoryId = state.selectedCategoryId,
                updatedAt = DateTimeUtils.now(),
                notes = state.notes.ifBlank { null },
                contents = listOf(
                    ContentItem(
                        languageCode = state.sourceLanguage,
                        text = state.sourceText.trim(),
                        pronunciation = state.pronunciation.ifBlank { null },
                        example = state.example.ifBlank { null }
                    ),
                    ContentItem(
                        languageCode = state.targetLanguage,
                        text = state.targetText.trim(),
                        example = state.example.ifBlank { null }
                    )
                ),
                tags = tags
            )

            conceptRepository.update(updated)
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }

    fun delete() {
        viewModelScope.launch {
            conceptRepository.archive(conceptId)
            _uiState.value = _uiState.value.copy(deleted = true)
        }
    }
}
