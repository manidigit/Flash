package com.app.flashlearn.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.R
import com.app.flashlearn.core.util.UiText
import com.app.flashlearn.domain.model.ParsedVocabularyEntry
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.usecase.ImportParsedEntriesUseCase
import com.app.flashlearn.domain.usecase.ParsePasteTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddPasteTextUiState(
    val rawText: String = "",
    val entries: List<ParsedVocabularyEntry> = emptyList(),
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa",
    val isImporting: Boolean = false,
    val importedCount: Int? = null,
    val duplicateCount: Int = 0,
    val translationsAddedCount: Int = 0,
    val errorMessage: UiText? = null
) {
    val includedCount: Int get() = entries.count { it.included }
}

/**
 * Paste Text Import (بند 42): متن چندخطی -> Parse -> Preview Table قابل ویرایش -> Import All.
 * هیچ رکوردی بدون عبور از این Preview مستقیم ذخیره نمی‌شود.
 */
@HiltViewModel
class AddPasteTextViewModel @Inject constructor(
    private val parsePasteText: ParsePasteTextUseCase,
    private val importParsedEntries: ImportParsedEntriesUseCase,
    private val languagePairRepository: LanguagePairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPasteTextUiState())
    val uiState: StateFlow<AddPasteTextUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pair = languagePairRepository.observeActivePair().first()
            _uiState.value = _uiState.value.copy(
                sourceLanguage = pair?.sourceLanguage ?: "es",
                targetLanguage = pair?.targetLanguage ?: "fa"
            )
        }
    }

    fun onRawTextChanged(value: String) {
        _uiState.value = _uiState.value.copy(rawText = value)
    }

    fun parse() {
        val entries = parsePasteText(_uiState.value.rawText)
        _uiState.value = _uiState.value.copy(entries = entries, importedCount = null)
    }

    fun updateEntry(localId: Int, sourceText: String, targetText: String) {
        val updated = _uiState.value.entries.map {
            if (it.localId == localId) it.copy(sourceText = sourceText, targetText = targetText) else it
        }
        _uiState.value = _uiState.value.copy(entries = updated)
    }

    fun toggleIncluded(localId: Int) {
        val updated = _uiState.value.entries.map {
            if (it.localId == localId) it.copy(included = !it.included) else it
        }
        _uiState.value = _uiState.value.copy(entries = updated)
    }

    fun removeEntry(localId: Int) {
        _uiState.value = _uiState.value.copy(entries = _uiState.value.entries.filterNot { it.localId == localId })
    }

    fun importAll() {
        val state = _uiState.value
        val toImport = state.entries.filter { it.included }
        if (toImport.isEmpty() || state.isImporting) return

        viewModelScope.launch {
            _uiState.value = state.copy(isImporting = true, errorMessage = null)
            // بند 64: حتی اگر خطای غیرمنتظره‌ای رخ دهد (مثلاً قطع دیتابیس)، isImporting هرگز
            // برای همیشه true نمی‌ماند و کاربر در این صفحه گیر نمی‌کند.
            try {
                val outcome = importParsedEntries(toImport, state.sourceLanguage, state.targetLanguage)
                _uiState.value = AddPasteTextUiState(
                    sourceLanguage = state.sourceLanguage,
                    targetLanguage = state.targetLanguage,
                    importedCount = outcome.insertedCount,
                    duplicateCount = outcome.duplicateCount,
                    translationsAddedCount = outcome.translationsAddedCount
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isImporting = false,
                    errorMessage = UiText.of(R.string.add_paste_import_error, e.message ?: "")
                )
            }
        }
    }
}
