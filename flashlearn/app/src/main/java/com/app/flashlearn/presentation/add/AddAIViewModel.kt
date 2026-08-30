package com.app.flashlearn.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.R
import com.app.flashlearn.core.util.DateTimeUtils
import com.app.flashlearn.core.util.UiText
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ContentItem
import com.app.flashlearn.domain.model.ContentType
import com.app.flashlearn.domain.model.LearningState
import com.app.flashlearn.domain.model.TranslationSuggestion
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.repository.LearningStateRepository
import com.app.flashlearn.domain.service.AITranslationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class AddAiUiState(
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa",
    val sourceText: String = "",
    val isTranslating: Boolean = false,
    val suggestion: TranslationSuggestion? = null,
    val editedTranslation: String = "",
    val editedPronunciation: String = "",
    val editedExample: String = "",
    val errorMessage: UiText? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

/**
 * افزودن با کمک AI (بند 41): کاربر فقط متن مبدا را می‌نویسد، AI پیشنهاد می‌دهد،
 * و فقط بعد از Approve صریح کاربر (با امکان Edit) در دیتابیس ذخیره می‌شود.
 * هرگز مستقیماً از خروجی AI ذخیره نمی‌شود (بند 78).
 */
@HiltViewModel
class AddAIViewModel @Inject constructor(
    private val aiTranslationService: AITranslationService,
    private val conceptRepository: ConceptRepository,
    private val learningStateRepository: LearningStateRepository,
    private val languagePairRepository: LanguagePairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAiUiState())
    val uiState: StateFlow<AddAiUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pair = languagePairRepository.observeActivePair().first()
            _uiState.value = _uiState.value.copy(
                sourceLanguage = pair?.sourceLanguage ?: "es",
                targetLanguage = pair?.targetLanguage ?: "fa"
            )
        }
    }

    fun onSourceTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(sourceText = value, suggestion = null, errorMessage = null)
    }

    fun translate() {
        val state = _uiState.value
        if (state.sourceText.isBlank() || state.isTranslating) return

        viewModelScope.launch {
            _uiState.value = state.copy(isTranslating = true, errorMessage = null)

            val result = aiTranslationService.translate(
                sourceText = state.sourceText.trim(),
                sourceLanguage = state.sourceLanguage,
                targetLanguage = state.targetLanguage
            )

            result.onSuccess { suggestion ->
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    suggestion = suggestion,
                    editedTranslation = suggestion.translation,
                    editedPronunciation = suggestion.pronunciation ?: "",
                    editedExample = suggestion.example ?: ""
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isTranslating = false,
                    errorMessage = error.message?.let { UiText.Dynamic(it) }
                        ?: UiText.of(R.string.add_ai_error_generic)
                )
            }
        }
    }

    fun onEditedTranslationChanged(value: String) {
        _uiState.value = _uiState.value.copy(editedTranslation = value)
    }

    fun onEditedPronunciationChanged(value: String) {
        _uiState.value = _uiState.value.copy(editedPronunciation = value)
    }

    fun onEditedExampleChanged(value: String) {
        _uiState.value = _uiState.value.copy(editedExample = value)
    }

    /** بند 41: Approve — فقط اینجا واقعاً در دیتابیس ذخیره می‌شود. */
    fun approve() {
        val state = _uiState.value
        if (state.editedTranslation.isBlank() || state.isSaving) return

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val now = DateTimeUtils.now()
            val sourceText = state.sourceText.trim()
            val targetText = state.editedTranslation.trim()

            // بند 64 (رفع باگ «کلمه با چند معنی»): همان منطق ادغام Manual/Import اینجا هم
            // اعمال می‌شود تا ترجمه AI برای یک کلمه از قبل موجود، Concept تکراری نسازد.
            val existingConceptId = conceptRepository.findActiveConceptIdByText(state.sourceLanguage, sourceText)
            if (existingConceptId != null) {
                val alreadyHasThisMeaning = conceptRepository.hasTranslation(existingConceptId, state.targetLanguage, targetText)
                if (!alreadyHasThisMeaning) {
                    conceptRepository.addTranslation(
                        existingConceptId,
                        ContentItem(
                            languageCode = state.targetLanguage,
                            text = targetText,
                            definition = state.suggestion?.definition,
                            example = state.editedExample.ifBlank { null }
                        )
                    )
                }
                _uiState.value = AddAiUiState(
                    sourceLanguage = state.sourceLanguage,
                    targetLanguage = state.targetLanguage,
                    saved = true
                )
                return@launch
            }

            val concept = Concept(
                id = 0,
                uuid = UUID.randomUUID().toString(),
                contentType = ContentType.WORD,
                categoryId = null,
                favorite = false,
                active = true,
                createdAt = now,
                updatedAt = now,
                notes = state.suggestion?.notes,
                contents = listOf(
                    ContentItem(
                        languageCode = state.sourceLanguage,
                        text = sourceText,
                        pronunciation = state.editedPronunciation.ifBlank { null },
                        example = state.editedExample.ifBlank { null }
                    ),
                    ContentItem(
                        languageCode = state.targetLanguage,
                        text = targetText,
                        definition = state.suggestion?.definition,
                        example = state.editedExample.ifBlank { null }
                    )
                ),
                tags = emptyList()
            )

            val conceptId = conceptRepository.insert(concept)
            learningStateRepository.save(LearningState(conceptId = conceptId))

            _uiState.value = AddAiUiState(
                sourceLanguage = state.sourceLanguage,
                targetLanguage = state.targetLanguage,
                saved = true
            )
        }
    }

    fun cancelSuggestion() {
        _uiState.value = _uiState.value.copy(suggestion = null)
    }
}
