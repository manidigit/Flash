package com.app.flashlearn.presentation.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.R
import com.app.flashlearn.core.util.UiText
import com.app.flashlearn.domain.model.ParsedVocabularyEntry
import com.app.flashlearn.domain.repository.LanguagePairRepository
import com.app.flashlearn.domain.usecase.ImportParsedEntriesUseCase
import com.app.flashlearn.domain.usecase.ParseCsvVocabularyUseCase
import com.app.flashlearn.domain.usecase.ParseJsonVocabularyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddImportFileUiState(
    val fileName: String? = null,
    val entries: List<ParsedVocabularyEntry> = emptyList(),
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa",
    val errorMessage: UiText? = null,
    val isImporting: Boolean = false,
    val importedCount: Int? = null,
    val duplicateCount: Int = 0,
    val translationsAddedCount: Int = 0
) {
    val includedCount: Int get() = entries.count { it.included }
}

/**
 * Import File - CSV و JSON (بند 43). XLSX/SQLite در مرحله جداگانه اضافه می‌شوند (نیاز به
 * Parser دودویی/کتابخانه اضافه دارند). فرمت بر اساس پسوند نام فایل تشخیص داده می‌شود.
 */
@HiltViewModel
class AddImportFileViewModel @Inject constructor(
    private val parseCsv: ParseCsvVocabularyUseCase,
    private val parseJson: ParseJsonVocabularyUseCase,
    private val importParsedEntries: ImportParsedEntriesUseCase,
    private val languagePairRepository: LanguagePairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddImportFileUiState())
    val uiState: StateFlow<AddImportFileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pair = languagePairRepository.observeActivePair().first()
            _uiState.value = _uiState.value.copy(
                sourceLanguage = pair?.sourceLanguage ?: "es",
                targetLanguage = pair?.targetLanguage ?: "fa"
            )
        }
    }

    fun onFilePicked(fileName: String, content: String) {
        try {
            val entries = if (fileName.endsWith(".json", ignoreCase = true)) {
                parseJson(content)
            } else {
                parseCsv(content)
            }

            if (entries.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    fileName = fileName,
                    entries = emptyList(),
                    errorMessage = UiText.of(R.string.add_import_file_no_valid_records)
                )
            } else {
                _uiState.value = _uiState.value.copy(fileName = fileName, entries = entries, errorMessage = null)
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = UiText.of(R.string.add_import_file_read_error, e.message ?: "")
            )
        }
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
            // بند 64: اگر خطای غیرمنتظره‌ای وسط Import رخ دهد، isImporting برای همیشه true
            // نمی‌ماند؛ ردیف‌های تک‌تک ناموفق هم داخل ImportParsedEntriesUseCase به‌صورت
            // جداگانه Skip می‌شوند، نه اینکه کل Import را متوقف کنند.
            try {
                val outcome = importParsedEntries(toImport, state.sourceLanguage, state.targetLanguage)
                _uiState.value = AddImportFileUiState(
                    sourceLanguage = state.sourceLanguage,
                    targetLanguage = state.targetLanguage,
                    importedCount = outcome.insertedCount,
                    duplicateCount = outcome.duplicateCount,
                    translationsAddedCount = outcome.translationsAddedCount
                )
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isImporting = false,
                    errorMessage = UiText.of(R.string.add_import_file_import_error, e.message ?: "")
                )
            }
        }
    }
}
