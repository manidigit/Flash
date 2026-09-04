package com.app.flashlearn.presentation.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.domain.model.ReviewAnswer

@Composable
fun ReviewScreen(
    reviewType: String,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Review Type: $reviewType", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        if (uiState.currentConcept != null) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = uiState.currentContent?.text ?: "No content",
                    modifier = Modifier.padding(16.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    viewModel.submitAnswer(
                        uiState.currentConcept!!.id,
                        ReviewAnswer.INCORRECT,
                        sessionId = "session-1",
                        attemptId = "attempt-1"
                    )
                }) {
                    Text("Incorrect")
                }
                Button(onClick = {
                    viewModel.submitAnswer(
                        uiState.currentConcept!!.id,
                        ReviewAnswer.CORRECT,
                        sessionId = "session-1",
                        attemptId = "attempt-1"
                    )
                }) {
                    Text("Correct")
                }
            }
        } else {
            Text("No more cards for this review.")
        }
        if (uiState.errorMessage != null) {
            Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
        }
    }
}
