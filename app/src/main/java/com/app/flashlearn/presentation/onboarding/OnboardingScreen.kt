package com.app.flashlearn.presentation.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.domain.model.Language
import com.app.flashlearn.ui.theme.Spacing

/**
 * صفحه اولین اجرا (بند 7 و 70): انتخاب زبان مبدا و مقصد.
 * onFinished فقط وقتی صدا زده می‌شود که ذخیره‌سازی LanguagePair کامل شده باشد.
 *
 * نکته مهم: کل صفحه باید Scroll بخورد، چون با ~9 زبان در هر لیست، ارتفاع محتوا از
 * صفحه‌های کوچک بیشتر می‌شود و دکمه «شروع کنید» باید همیشه با اسکرول در دسترس بماند
 * (قبلاً این صفحه Scroll نداشت و دکمه از دید کاربر خارج می‌شد).
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.completed) {
        if (state.completed) onFinished()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(
            text = stringResource(R.string.onboarding_welcome),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.onboarding_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(stringResource(R.string.onboarding_source_language), style = MaterialTheme.typography.titleMedium)
        LanguageGrid(
            languages = state.availableLanguages,
            selected = state.sourceLanguage,
            onSelect = viewModel::selectSource
        )

        Text(stringResource(R.string.onboarding_target_language), style = MaterialTheme.typography.titleMedium)
        LanguageGrid(
            languages = state.availableLanguages,
            selected = state.targetLanguage,
            onSelect = viewModel::selectTarget
        )

        Button(
            onClick = viewModel::confirm,
            enabled = state.canConfirm && !state.isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (state.isSaving) {
                    stringResource(R.string.onboarding_saving)
                } else {
                    stringResource(R.string.onboarding_start)
                }
            )
        }
    }
}

@Composable
private fun LanguageGrid(
    languages: List<Language>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    // Column ساده (نه LazyColumn) عمداً استفاده شده: چون این خودش داخل یک Column با
    // verticalScroll در والد قرار دارد، یک LazyColumn تودرتو با Constraint نامحدود
    // Crash می‌کند (IllegalStateException). تعداد زبان‌ها هم کم است (~9 مورد)، پس
    // نیازی به Lazy بودن نیست.
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        languages.forEach { language ->
            val isSelected = language.code == selected
            Card(
                onClick = { onSelect(language.code) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                )
            ) {
                Text(
                    text = language.displayName,
                    modifier = Modifier.padding(Spacing.md),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
