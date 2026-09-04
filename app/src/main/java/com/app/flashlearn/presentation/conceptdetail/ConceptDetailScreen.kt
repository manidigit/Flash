package com.app.flashlearn.presentation.conceptdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConceptDetailScreen(
    viewModel: ConceptDetailViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val concept by viewModel.concept.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    if (concept == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    val c = concept!!

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "بازگشت") }
            IconButton(onClick = viewModel::toggleFavorite) {
                Icon(if (c.favorite) Icons.Filled.Star else Icons.Outlined.StarOutline, contentDescription = "علاقه‌مندی")
            }
        }

        c.contents.forEach { content ->
            Spacer(Modifier.height(12.dp))
            Text(content.languageCode.uppercase(), style = MaterialTheme.typography.labelSmall)
            Text(content.text, style = MaterialTheme.typography.titleLarge)
            content.pronunciation?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            content.example?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }

        c.learningState?.let { state ->
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            Text("وضعیت مرور: ${state.stage}", style = MaterialTheme.typography.bodyMedium)
            Text("سختی: ${state.difficulty}", style = MaterialTheme.typography.bodyMedium)
            Text("درست: ${state.totalCorrect} — غلط: ${state.totalWrong}", style = MaterialTheme.typography.bodyMedium)
        }

        if (history.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            Text("تاریخچه مرور", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp))
            history.take(10).forEach { entry ->
                Text(
                    "${if (entry.isCorrect) "✅" else "❌"} ${entry.reviewStage} — ${entry.previousDifficulty} → ${entry.newDifficulty}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
