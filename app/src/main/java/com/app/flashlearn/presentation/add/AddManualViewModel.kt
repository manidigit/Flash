package com.app.flashlearn.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.domain.model.Category
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.model.ContentType
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.repository.CategoryRepository
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddManualUiState(
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa",
    val sourceText: String = "",
    val targetText: String = "",
    val example: String = "",
    val pronunciation: String = "",
    val notes: String = "",
    val tagsText: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
) {
    val canSave: Boolean
        get() = sourceText.isNotBlank() && targetText.isNotBlank()
}

/**
 * افزودن دستی کلمه (بند 40) به‌همراه Category و Tags (بند 15-16). فقط با تأیید کاربر
 * ذخیره می‌شود. وضعیت یادگیری اولیه DAILY ساخته می‌شود تا بلافاصله وارد چرخه مرور شود.
 */
@HiltViewModel
class AddManualViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val languagePairRepository: LanguagePairRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddManualUiState())
    val uiState: StateFlow<AddManualUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pair = languagePairRepository.observeActivePair().first()
            _uiState.value = _uiState.value.copy(
                sourceLanguage = pair?.sourceLanguage ?: "es",
                targetLanguage = pair?.targetLanguage ?: "fa"
            )
        }
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun onSourceTextChanged(value: String) { _uiState.value = _uiState.value.copy(sourceText = value) }
    fun onTargetTextChanged(value: String) { _uiState.value = _uiState.value.copy(targetText = value) }
    fun onExampleChanged(value: String) { _uiState.value = _uiState.value.copy(example = value) }
    fun onNotesChanged(value: String) { _uiState.value = _uiState.value.copy(notes = value) }
    fun onPronunciationChanged(value: String) { _uiState.value = _uiState.value.copy(pronunciation = value) }
    fun onTagsTextChanged(value: String) { _uiState.value = _uiState.value.copy(tagsText = value) }

    fun onCategorySelected(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }

    /** ساخت Category جدید توسط کاربر (بند 15). */
    fun createCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = categoryRepository.getOrCreate(name.trim())
            _uiState.value = _uiState.value.copy(selectedCategoryId = id)
        }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val now = DateTimeUtils.now()
            val tags = state.tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            val concept = Concept(
                id = 0,
                uuid = UUID.randomUUID().toString(),
                contentType = ContentType.WORD,
                categoryId = state.selectedCategoryId,
                favorite = false,
                active = true,
                createdAt = now,
                updatedAt = now,
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

            val conceptId = conceptRepository.insert(concept)
            learningStateRepository.save(LearningState(conceptId = conceptId))

            _uiState.value = AddManualUiState(
                sourceLanguage = state.sourceLanguage,
                targetLanguage = state.targetLanguage,
                categories = state.categories,
                saved = true
            )
        }
    }
}
