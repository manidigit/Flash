package com.app.flashlearn.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    Column(Modifier.fillMaxSize().padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text("تنظیمات", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("ظاهر و رفتار فلش‌لرن را تنظیم کن.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(shape = RoundedCornerShape(Radius.card), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                // Deliberately compact so the three options stay on one line on small phones.
                Text("ظاهر برنامه", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Clip)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ThemeButton(ThemeMode.LIGHT, Icons.Default.LightMode, state.themeMode == ThemeMode.LIGHT, viewModel::setThemeMode, Modifier.weight(1f))
                    ThemeButton(ThemeMode.DARK, Icons.Default.DarkMode, state.themeMode == ThemeMode.DARK, viewModel::setThemeMode, Modifier.weight(1f))
                    ThemeButton(ThemeMode.SYSTEM, Icons.Default.BrightnessAuto, state.themeMode == ThemeMode.SYSTEM, viewModel::setThemeMode, Modifier.weight(1f))
                }
            }
        }

        val pair = state.activePair
        Card(onClick = onChangeLanguagePair, shape = RoundedCornerShape(Radius.card), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.fillMaxWidth().padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Language, null, tint = Color(0xFF3B82F6), modifier = Modifier.size(25.dp))
                Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
                    Text("جفت زبان فعال", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(if (pair != null) "${pair.sourceLanguage.uppercase()}  →  ${pair.targetLanguage.uppercase()}" else "تعیین نشده", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("تغییر", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
        }

        SettingsActionCard(Icons.Default.Storage, "پشتیبان‌گیری و بازیابی", "Export، Import و محافظت از داده‌ها", onOpenBackupRestore, Color(0xFF14B8A6))
        SettingsActionCard(Icons.Default.Security, "تنظیمات AI", "Endpoint، مدل و کلید API", onOpenAISettings, Color(0xFF8B5CF6))
        Text("نسخه ${com.app.flashlearn.BuildConfig.VERSION_CODE}  •  ${com.app.flashlearn.BuildConfig.BUILD_TIME}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun ThemeButton(mode: ThemeMode, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onSelect: (ThemeMode) -> Unit, modifier: Modifier) {
    OutlinedButton(onClick = { onSelect(mode) }, modifier = modifier, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 5.dp), shape = RoundedCornerShape(10.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        Text(when (mode) { ThemeMode.LIGHT -> "روشن"; ThemeMode.DARK -> "تیره"; ThemeMode.SYSTEM -> "سیستم" }, modifier = Modifier.padding(start = 3.dp), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Clip)
    }
}

@Composable private fun SettingsActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit, tint: Color) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(Radius.card), colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = .07f))) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
            Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
