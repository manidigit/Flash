package com.app.flashlearn.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.flashlearn.domain.model.Category
import com.app.flashlearn.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewTypeSelectUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null
)

/**
 * فیلتر ترکیبی (بند 30): کاربر ابتدا (اختیاری) یک Category انتخاب می‌کند، سپس نوع مرور را؛
 * هر دو با هم به ReviewSession فرستاده می‌شوند (مثال دقیق بند 30: «سخت + سفر + اسپانیایی»).
 */
@HiltViewModel
class ReviewTypeSelectViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewTypeSelectUiState())
    val uiState: StateFlow<ReviewTypeSelectUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            categoryRepository.observeAll().collect { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun onCategorySelected(categoryId: Long?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
    }
}
