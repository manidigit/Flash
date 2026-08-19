package com.app.flashlearn.presentation.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.app.flashlearn.R
import com.app.flashlearn.ui.theme.Spacing

/**
 * انتخاب روش افزودن کلمه (بند 40-43 و 53). Manual و AI فعال‌اند؛
 * Paste/Import در مرحله بعد اضافه می‌شوند.
 * تمام متن‌های این صفحه از strings.xml خوانده می‌شوند (بند 83).
 */
@Composable
fun AddHomeScreen(
    onManualSelected: () -> Unit,
    onAISelected: () -> Unit,
    onPasteTextSelected: () -> Unit,
    onImportFileSelected: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        Card(onClick = onManualSelected) {
            Text(stringResource(R.string.add_home_manual), modifier = Modifier.padding(Spacing.md), style = MaterialTheme.typography.bodyLarge)
        }
        Card(onClick = onAISelected) {
            Text(stringResource(R.string.add_home_ai), modifier = Modifier.padding(Spacing.md), style = MaterialTheme.typography.bodyLarge)
        }
        Card(onClick = onPasteTextSelected) {
            Text(stringResource(R.string.add_home_paste_text), modifier = Modifier.padding(Spacing.md), style = MaterialTheme.typography.bodyLarge)
        }
        Card(onClick = onImportFileSelected) {
            Text(stringResource(R.string.add_home_import_file), modifier = Modifier.padding(Spacing.md), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
