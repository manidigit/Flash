package com.app.flashlearn.presentation.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ReviewSessionScreen(
    viewModel: ReviewViewModel = hiltViewModel(),
    onFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = state) {
        is ReviewUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        is ReviewUiState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("چیزی برای مرور نیست \ud83c\udf89", style = MaterialTheme.typography.titleMedium)
        }

        is ReviewUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(s.message, color = MaterialTheme.colorScheme.error)
        }

        is ReviewUiState.InProgress -> ReviewCardContent(
            viewModel = viewModel,
            state = s,
            onReveal = viewModel::revealAnswer,
            onAnswer = viewModel::submitAnswer
        )

        is ReviewUiState.Finished -> ReviewFinishedContent(
            state = s,
            onDone = onFinished
        )
    }
}

@Composable
private fun ReviewCardContent(
    viewModel: ReviewViewModel,
    state: ReviewUiState.InProgress,
    onReveal: () -> Unit,
    onAnswer: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        LinearProgressIndicator(
            progress = { (state.currentIndex + 1f) / state.totalCount },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Text("${state.currentIndex + 1} از ${state.totalCount}", style = MaterialTheme.typography.labelMedium)

        Spacer(Modifier.weight(1f))

        Card(
            modifier = Modifier.fillMaxWidth().clickable(enabled = !state.showAnswer) { onReveal() },
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                val frontContent = viewModel.frontContent(state.currentConcept)
                Text(frontContent?.text ?: "", style = MaterialTheme.typography.headlineMedium)

                if (state.showAnswer) {
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))
                    val backContent = viewModel.backContent(state.currentConcept)
                    Text(backContent?.text ?: "", style = MaterialTheme.typography.titleLarge)
                    backContent?.pronunciation?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (state.showAnswer) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onAnswer(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("غلط") }

                Button(onClick = { onAnswer(true) }, modifier = Modifier.weight(1f)) { Text("درست") }
            }
        } else {
            Button(onClick = onReveal, modifier = Modifier.fillMaxWidth()) { Text("نمایش جواب") }
        }
    }
}

@Composable
private fun ReviewFinishedContent(state: ReviewUiState.Finished, onDone: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("پایان مرور", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text("درست: ${state.correctCount} / ${state.totalCount}")
        Text("غلط: ${state.wrongCount} / ${state.totalCount}")
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone) { Text("بازگشت") }
    }
}
