package com.app.flashlearn.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.ui.theme.Spacing

/** تنظیمات AI (بند 76): Endpoint/Model/API Key سازگار با هر سرویس Chat-Completions-like. */
@Composable
fun AISettingsScreen(
    viewModel: AISettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(stringResource(R.string.ai_settings_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.ai_settings_security_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = state.endpoint,
            onValueChange = viewModel::onEndpointChanged,
            label = { Text(stringResource(R.string.ai_settings_endpoint_label)) },
            placeholder = { Text("https://api.example.com/v1/chat/completions") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.model,
            onValueChange = viewModel::onModelChanged,
            label = { Text(stringResource(R.string.ai_settings_model_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = state.apiKey,
            onValueChange = viewModel::onApiKeyChanged,
            label = { Text(stringResource(R.string.ai_settings_api_key_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Button(onClick = viewModel::save, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (state.isSaving) R.string.ai_settings_saving else R.string.action_save))
        }

        if (state.saved) {
            Text(stringResource(R.string.ai_settings_saved), color = MaterialTheme.colorScheme.primary)
        }
    }
}
