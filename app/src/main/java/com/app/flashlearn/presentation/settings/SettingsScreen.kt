package com.app.flashlearn.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SettingsScreen(
    onNavigateToAI: () -> Unit,
    onNavigateToBackup: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ListItem(
            headlineContent = { Text("تنظیمات ظاهری") },
            leadingContent = { Icon(Icons.Filled.Palette, contentDescription = null) }
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("زبان مبدا / مقصد") },
            leadingContent = { Icon(Icons.Filled.Language, contentDescription = null) }
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("تنظیمات هوش مصنوعی") },
            leadingContent = { Icon(Icons.Filled.AutoAwesome, contentDescription = null) },
            modifier = Modifier.clickable { onNavigateToAI() }
        )
        HorizontalDivider()
        ListItem(
            headlineContent = { Text("پشتیبان‌گیری و بازیابی") },
            leadingContent = { Icon(Icons.Filled.Backup, contentDescription = null) },
            modifier = Modifier.clickable { onNavigateToBackup() }
        )
    }
}
