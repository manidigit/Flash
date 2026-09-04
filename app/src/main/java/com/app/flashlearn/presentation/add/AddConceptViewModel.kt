package com.app.flashlearn.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.Content
import com.app.flashlearn.domain.model.ContentType
import com.app.flashlearn.domain.model.Tag
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.usecase.AddConceptUseCase
import com.app.flashlearn.domain.usecase.TranslateWithAIUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddConceptUiState(
    val contentType: ContentType = ContentType.WORD,
    val sourceText: String = "",
    val targetText: String = "",
    val pronunciation: String = "",
    val notes: String = "",
    val selectedCategoryId: Long? = null,
    val selectedTags: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

@HiltViewModel
class AddConceptViewModel @Inject constructor(
    private val addConceptUseCase: AddConceptUseCase,
    private val languagePairRepository: LanguagePairRepository,
    private val translateWithAIUseCase: TranslateWithAIUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddConceptUiState())
    val uiState: StateFlow<AddConceptUiState> = _uiState.asStateFlow()

    fun onSourceTextChanged(text: String) { _uiState.value = _uiState.value.copy(sourceText = text) }
    fun onTargetTextChanged(text: String) { _uiState.value = _uiState.value.copy(targetText = text) }
    fun onNotesChanged(text: String) { _uiState.value = _uiState.value.copy(notes = text) }
    fun onContentTypeChanged(type: ContentType) { _uiState.value = _uiState.value.copy(contentType = type) }

    fun translateWithAI() {
        val state = _uiState.value
        if (state.sourceText.isBlank()) return
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            translateWithAIUseCase(state.sourceText, state.contentType)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        targetText = result.translatedText,
                        pronunciation = result.pronunciation ?: "",
                        notes = listOfNotNull(result.definition, result.example, result.grammarNote).joinToString("\n"),
                        isSaving = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(isSaving = false, error = "خطا در ترجمه AI: ${e.message}")
                }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.sourceText.isBlank() || state.targetText.isBlank()) {
            _uiState.value = state.copy(error = "متن مبدا و مقصد الزامی است")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            try {
                val pair = languagePairRepository.getActivePair()
                    ?: run {
                        _uiState.value = _uiState.value.copy(isSaving = false, error = "زبان مبدا/مقصد انتخاب نشده")
                        return@launch
                    }
                val now = System.currentTimeMillis()
                val concept = Concept(
                    uuid = UUID.randomUUID().toString(),
                    contentType = state.contentType,
                    categoryId = state.selectedCategoryId,
                    favorite = false,
                    active = true,
                    createdAt = now,
                    updatedAt = now,
                    contents = listOf(
                        Content(conceptId = 0, languageCode = pair.sourceLanguage, text = state.sourceText, usageNote = state.notes.ifBlank { null }),
                        Content(conceptId = 0, languageCode = pair.targetLanguage, text = state.targetText, pronunciation = state.pronunciation.ifBlank { null })
                    ),
                    tags = state.selectedTags.map { Tag(name = it) }
                )
                addConceptUseCase(concept)
                _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message ?: "خطا در ذخیره")
            }
        }
    }
}
