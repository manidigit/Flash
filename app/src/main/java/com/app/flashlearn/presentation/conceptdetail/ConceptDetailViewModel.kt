package com.app.flashlearn.presentation.conceptdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.model.ReviewHistoryEntry
import com.app.flashlearn.domain.repository.ConceptRepository
import com.app.flashlearn.domain.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConceptDetailViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository,
    private val reviewRepository: ReviewRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val conceptId: Long = checkNotNull(savedStateHandle["id"])

    private val _concept = MutableStateFlow<Concept?>(null)
    val concept: StateFlow<Concept?> = _concept.asStateFlow()

    private val _history = MutableStateFlow<List<ReviewHistoryEntry>>(emptyList())
    val history: StateFlow<List<ReviewHistoryEntry>> = _history.asStateFlow()

    init {
        viewModelScope.launch {
            _concept.value = conceptRepository.getConceptById(conceptId)
            _history.value = reviewRepository.getHistoryForConcept(conceptId)
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val current = _concept.value ?: return@launch
            val updated = current.copy(favorite = !current.favorite)
            conceptRepository.updateConcept(updated)
            _concept.value = updated
        }
    }
}
