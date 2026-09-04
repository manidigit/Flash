package com.app.flashlearn.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.flashlearn.ui.theme.SuccessLight

@Composable
fun AISettingsScreen(viewModel: AISettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("تنظیمات هوش مصنوعی", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (state.hasApiKey) {
            Text("✅ کلید API تنظیم شده", color = SuccessLight)
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = viewModel::clearApiKey) { Text("حذف کلید") }
        } else {
            OutlinedTextField(
                value = state.apiKeyInput,
                onValueChange = viewModel::onApiKeyChanged,
                label = { Text("API Key") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = viewModel::saveApiKey,
                enabled = state.apiKeyInput.isNotBlank() && !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) { Text("ذخیره") }
        }

        state.message?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, style = MaterialTheme.typography.bodySmall)
        }
    }
}
