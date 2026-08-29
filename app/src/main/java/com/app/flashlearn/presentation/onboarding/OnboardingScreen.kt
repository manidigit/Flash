package com.app.flashlearn.presentation.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.domain.model.Language
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun OnboardingScreen(onFinished: () -> Unit, viewModel: OnboardingViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(state.completed) { if (state.completed) onFinished() }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)), RoundedCornerShape(Radius.card)).padding(Spacing.xl)) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Box(Modifier.size(54.dp).background(MaterialTheme.colorScheme.surface.copy(alpha = .18f), CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = androidx.compose.ui.graphics.Color.White) }
                Text("واژگان را ماندگار کن.", style = MaterialTheme.typography.displaySmall, color = androidx.compose.ui.graphics.Color.White)
                Text("FlashLearn زمان مرور را برایت مدیریت می‌کند؛ تو فقط یاد بگیر.", style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.White.copy(alpha = .9f))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Feature("یادگیری", Icons.Default.School, Modifier.weight(1f))
            Feature("مرور هوشمند", Icons.Default.Sync, Modifier.weight(1f))
        }
        Text("زبان یادگیری را انتخاب کن", style = MaterialTheme.typography.headlineMedium)
        Text("بعداً می‌توانی زبان‌های بیشتری اضافه کنی.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("زبان مبدا", style = MaterialTheme.typography.titleMedium)
        LanguageGrid(state.availableLanguages, state.sourceLanguage, viewModel::selectSource)
        Text("زبان مقصد", style = MaterialTheme.typography.titleMedium)
        LanguageGrid(state.availableLanguages, state.targetLanguage, viewModel::selectTarget)
        Button(onClick = viewModel::confirm, enabled = state.canConfirm && !state.isSaving, modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xl)) {
            Text(if (state.isSaving) "در حال آماده‌سازی…" else "شروع یادگیری")
        }
    }
}

@Composable private fun Feature(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(modifier, shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.smallCard), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(Spacing.md), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable private fun LanguageGrid(languages: List<Language>, selected: String?, onSelect: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        languages.forEach { language ->
            val chosen = language.code == selected
            Card(onClick = { onSelect(language.code) }, modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.smallCard), colors = CardDefaults.cardColors(containerColor = if (chosen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(38.dp).background(if (chosen) MaterialTheme.colorScheme.primary.copy(alpha = .12f) else MaterialTheme.colorScheme.surfaceVariant, CircleShape), contentAlignment = Alignment.Center) { Text(language.code.uppercase().take(2)) }
                    Text(language.displayName, Modifier.padding(horizontal = Spacing.md), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
