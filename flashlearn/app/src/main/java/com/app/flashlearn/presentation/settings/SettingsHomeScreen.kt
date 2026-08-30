package com.app.flashlearn.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun SettingsHomeScreen(
    onChangeLanguagePair: () -> Unit,
    onOpenBackupRestore: () -> Unit,
    onOpenAISettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.fillMaxSize().padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineLarge)
        Text("FlashLearn را مطابق روش یادگیری خودت تنظیم کن.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.card)) {
            Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text("ظاهر برنامه", style = MaterialTheme.typography.titleLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    ThemeButton(ThemeMode.LIGHT, Icons.Default.LightMode, state.themeMode == ThemeMode.LIGHT, viewModel::setThemeMode, Modifier.weight(1f))
                    ThemeButton(ThemeMode.DARK, Icons.Default.DarkMode, state.themeMode == ThemeMode.DARK, viewModel::setThemeMode, Modifier.weight(1f))
                    ThemeButton(ThemeMode.SYSTEM, Icons.Default.BrightnessAuto, state.themeMode == ThemeMode.SYSTEM, viewModel::setThemeMode, Modifier.weight(1f))
                }
            }
        }

        val pair = state.activePair
        Card(onClick = onChangeLanguagePair, shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.card), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.fillMaxWidth().padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
                    Text("جفت زبان فعال", style = MaterialTheme.typography.titleMedium)
                    Text(if (pair != null) "${pair.sourceLanguage.uppercase()}  →  ${pair.targetLanguage.uppercase()}" else "تعیین نشده", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("تغییر", color = MaterialTheme.colorScheme.primary)
            }
        }

        SettingsActionCard(Icons.Default.Storage, "پشتیبان‌گیری و بازیابی", "Export، Import و محافظت از داده‌ها", onOpenBackupRestore)
        SettingsActionCard(Icons.Default.Security, "تنظیمات AI", "Endpoint، مدل و کلید API", onOpenAISettings)

        Text("نسخه ${com.app.flashlearn.BuildConfig.VERSION_CODE}  •  ${com.app.flashlearn.BuildConfig.BUILD_TIME}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun ThemeButton(mode: ThemeMode, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onSelect: (ThemeMode) -> Unit, modifier: Modifier) {
    OutlinedButton(onClick = { onSelect(mode) }, modifier = modifier) {
        Icon(icon, contentDescription = null)
        Text(when (mode) { ThemeMode.LIGHT -> "روشن"; ThemeMode.DARK -> "تیره"; ThemeMode.SYSTEM -> "سیستم" }, modifier = Modifier.padding(start = Spacing.xs))
    }
}

@Composable private fun SettingsActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.card)) {
        Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
