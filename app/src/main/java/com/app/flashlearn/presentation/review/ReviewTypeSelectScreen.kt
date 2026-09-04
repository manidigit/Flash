package com.app.flashlearn.presentation.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.flashlearn.navigation.Screen

@Composable
fun ReviewTypeSelectScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Select Review Type", style = MaterialTheme.typography.headlineMedium)
        Button(
            onClick = { navController.navigate(Screen.ReviewSession.pass("DAILY")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Daily Review")
        }
        Button(
            onClick = { navController.navigate(Screen.ReviewSession.pass("WEEKLY")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Weekly Review")
        }
        Button(
            onClick = { navController.navigate(Screen.ReviewSession.pass("MONTHLY")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Monthly Review")
        }
    }
}
