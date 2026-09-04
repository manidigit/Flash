package com.app.flashlearn.presentation.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ReviewStage
import com.app.flashlearn.domain.repository.ConceptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class VocabularyFilter { ALL, LEARNED, LEARNING, NEW }

data class VocabularyUiState(
    val concepts: List<Concept> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: VocabularyFilter = VocabularyFilter.ALL,
    val isLoading: Boolean = true
)

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    private val pageSize = 30

    init { loadPage() }

    private fun loadPage() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val items = conceptRepository.getConceptsPaged(pageSize, 0)
            _uiState.value = _uiState.value.copy(concepts = items, isLoading = false)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        viewModelScope.launch {
            val results = if (query.isBlank()) {
                conceptRepository.getConceptsPaged(pageSize, 0)
            } else {
                conceptRepository.searchConcepts(query)
            }
            _uiState.value = _uiState.value.copy(concepts = results)
        }
    }

    fun onFilterChanged(filter: VocabularyFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter, isLoading = true)
        viewModelScope.launch {
            val items = when (filter) {
                VocabularyFilter.ALL -> conceptRepository.getConceptsPaged(pageSize, 0)
                VocabularyFilter.LEARNED -> conceptRepository.getConceptsByStage(ReviewStage.LEARNED, pageSize, 0)
                VocabularyFilter.LEARNING -> conceptRepository.getLearningConcepts(pageSize, 0)
                VocabularyFilter.NEW -> conceptRepository.getNewConcepts(pageSize, 0)
            }
            _uiState.value = _uiState.value.copy(concepts = items, isLoading = false)
        }
    }

    fun toggleFavorite(conceptId: Long) {
        viewModelScope.launch {
            val concept = _uiState.value.concepts.find { it.id == conceptId } ?: return@launch
            conceptRepository.updateConcept(concept.copy(favorite = !concept.favorite))
            onFilterChanged(_uiState.value.selectedFilter)
        }
    }

    fun deleteConcept(conceptId: Long) {
        viewModelScope.launch {
            conceptRepository.deleteConcept(conceptId)
            onFilterChanged(_uiState.value.selectedFilter)
        }
    }
}
