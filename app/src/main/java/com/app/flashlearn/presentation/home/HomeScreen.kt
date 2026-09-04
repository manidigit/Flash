package com.app.flashlearn.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.app.flashlearn.navigation.Screen

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("FlashLearn", style = MaterialTheme.typography.headlineLarge)
        Button(
            onClick = { navController.navigate(Screen.ReviewTypeSelect.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Review")
        }
        Button(
            onClick = { navController.navigate(Screen.VocabularyList.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Vocabulary")
        }
        Button(
            onClick = { navController.navigate(Screen.AddManual.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add New")
        }
        Button(
            onClick = { navController.navigate(Screen.Settings.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Settings")
        }
    }
}
