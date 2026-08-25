package com.app.flashlearn.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.R
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun SettingsHomeScreen(
    onChangeLanguagePair: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onOpenAISettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Text(stringResource(R.string.settings_theme_section), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            ThemeMode.values().forEach { mode ->
                OutlinedButton(
                    onClick = { viewModel.setThemeMode(mode) },
                    colors = if (mode == state.themeMode) {
                        androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    } else {
                        androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                    }
                ) {
                    Text(stringResource(mode.labelRes()))
                }
            }
        }

        Text(stringResource(R.string.settings_language_direction_section), style = MaterialTheme.typography.titleMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                val pair = state.activePair
                Text(
                    text = if (pair != null) {
                        stringResource(
                            R.string.settings_language_pair_format,
                            pair.sourceLanguage.uppercase(),
                            pair.targetLanguage.uppercase()
                        )
                    } else {
                        stringResource(R.string.settings_language_pair_not_set)
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Row(
                    modifier = Modifier.padding(top = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    OutlinedButton(onClick = viewModel::swapLanguageDirection, enabled = pair != null) {
                        Text(stringResource(R.string.settings_swap_direction_button))
                    }
                    OutlinedButton(onClick = onChangeLanguagePair) {
                        Text(stringResource(R.string.settings_change_languages_button))
                    }
                }
            }
        }

        Card(onClick = onOpenBackupRestore, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.settings_backup_restore_card),
                modifier = Modifier.padding(Spacing.md),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Card(onClick = onOpenAISettings, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.settings_ai_settings_card),
                modifier = Modifier.padding(Spacing.md),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // درخواست کاربر: بعد از هر Update، بتواند مطمئن شود نسخه جدید واقعاً نصب شده
        // (مثلاً از نسخه ۴ به ۵ رفته یا نه). BuildConfig.VERSION_CODE و BUILD_TIME هر دو
        // خودکار در زمان Build محاسبه می‌شوند (بند build.gradle.kts)، نیازی به یادآوری
        // دستی برای بالا بردن شماره نسخه نیست.
        Text(
            text = stringResource(
                R.string.settings_app_version,
                com.app.flashlearn.BuildConfig.VERSION_CODE,
                com.app.flashlearn.BuildConfig.BUILD_TIME
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm)
        )
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.LIGHT -> R.string.theme_mode_light
    ThemeMode.DARK -> R.string.theme_mode_dark
    ThemeMode.SYSTEM -> R.string.theme_mode_system
}
