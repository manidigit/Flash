package com.app.flashlearn.presentation.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Category
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.VocabularySortOrder
import com.app.flashlearn.domain.repository.CategoryRepository
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.LanguagePairRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 40

data class VocabularyListUiState(
    val items: List<Concept> = emptyList(),
    val searchQuery: String = "",
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val sortOrder: VocabularySortOrder = VocabularySortOrder.RECENT,
    val isLoading: Boolean = true,
    val sourceLanguage: String = "es",
    val targetLanguage: String = "fa",
    val endReached: Boolean = false
)

/**
 * صفحه All Words (بند 38-39): Search واقعی، فیلتر Category (بند 30)، و ترتیب نمایش
 * (جدیدترین یا الفبایی — الفبایی فقط وقتی جستجو خالی است اعمال می‌شود، چون نتایج جستجو
 * بر اساس تازگی نمایش داده می‌شوند).
 */
@HiltViewModel
class VocabularyListViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val languagePairRepository: LanguagePairRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyListUiState())
    val uiState: StateFlow<VocabularyListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pair = languagePairRepository.observeActivePair().first()
            _uiState.value = _uiState.value.copy(
                sourceLanguage = pair?.sourceLanguage ?: "es",
                targetLanguage = pair?.targetLanguage ?: "fa"
            )
            loadFirstPage()
        }
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch { loadFirstPage() }
    }

    fun onCategorySelected(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        viewModelScope.launch { loadFirstPage() }
    }

    fun onSortOrderSelected(sortOrder: VocabularySortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
        viewModelScope.launch { loadFirstPage() }
    }

    private suspend fun loadFirstPage() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        val state = _uiState.value
        val items = fetchPage(state, offset = 0)
        _uiState.value = _uiState.value.copy(
            items = items,
            isLoading = false,
            endReached = items.size < PAGE_SIZE
        )
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.endReached) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            val nextPage = fetchPage(state, offset = state.items.size)
            _uiState.value = _uiState.value.copy(
                items = state.items + nextPage,
                isLoading = false,
                endReached = nextPage.size < PAGE_SIZE
            )
        }
    }

    /** به‌روزرسانی خوش‌بینانه محلی؛ لیست را دوباره از دیتابیس نمی‌خواند تا اسکرول جابه‌جا نشود. */
    fun toggleFavorite(concept: Concept) {
        val newValue = !concept.favorite
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items.map {
                if (it.id == concept.id) it.copy(favorite = newValue) else it
            }
        )
        viewModelScope.launch {
            conceptRepository.setFavorite(concept.id, newValue)
        }
    }

    private suspend fun fetchPage(state: VocabularyListUiState, offset: Int): List<Concept> =
        if (state.searchQuery.isBlank()) {
            conceptRepository.getPage(
                limit = PAGE_SIZE,
                offset = offset,
                categoryId = state.selectedCategoryId,
                sortOrder = state.sortOrder,
                sortLanguageCode = state.sourceLanguage
            )
        } else {
            conceptRepository.search(
                state.searchQuery,
                limit = PAGE_SIZE,
                offset = offset,
                categoryId = state.selectedCategoryId
            )
        }
}
