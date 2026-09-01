package com.app.flashlearn.presentation.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Concept
import com.app.flashlearn.domain.repository.ConceptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val conceptRepository: ConceptRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    init {
        loadConcepts()
    }

    private fun loadConcepts() {
        viewModelScope.launch {
            try {
                conceptRepository.getAllActiveConcepts().collect { concepts ->
                    _uiState.value = _uiState.value.copy(
                        concepts = concepts,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
}

data class VocabularyUiState(
    val concepts: List<Concept> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
