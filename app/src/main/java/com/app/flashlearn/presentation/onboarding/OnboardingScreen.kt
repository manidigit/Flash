package com.app.flashlearn.presentation.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onFinished: () -> Unit
) {
    val source by viewModel.selectedSource.collectAsStateWithLifecycle()
    val target by viewModel.selectedTarget.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val availableLanguages = listOf(
        "fa" to "فارسی", "en" to "English", "es" to "Español", "fr" to "Français", "de" to "Deutsch"
    )

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("زبان مبدا خودت رو انتخاب کن", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LanguagePicker(availableLanguages, source) { viewModel.selectSource(it) }

        Spacer(Modifier.height(24.dp))

        Text("می‌خوای چه زبانی یاد بگیری؟", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LanguagePicker(availableLanguages, target) { viewModel.selectTarget(it) }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { scope.launch { if (viewModel.finish()) onFinished() } },
            enabled = source != null && target != null && source != target,
            modifier = Modifier.fillMaxWidth()
        ) { Text("شروع کن") }
    }
}

@Composable
private fun LanguagePicker(languages: List<Pair<String, String>>, selected: String?, onSelect: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(languages) { (code, label) ->
            FilterChip(selected = selected == code, onClick = { onSelect(code) }, label = { Text(label) })
        }
    }
}
