package com.app.flashlearn.presentation.vocabulary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Concept
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VocabularyViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState

    init {
        loadConcepts()
    }

    private fun loadConcepts() {
        viewModelScope.launch {
            // TODO: Load from repository
            _uiState.value = _uiState.value.copy(
                concepts = emptyList(),
                isLoading = false
            )
        }
    }
}

data class VocabularyUiState(
    val concepts: List<Concept> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
