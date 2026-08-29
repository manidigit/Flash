package com.app.flashlearn.presentation.add

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.app.flashlearn.ui.theme.Radius
import com.app.flashlearn.ui.theme.Spacing

@Composable
fun AddHomeScreen(
    onManualSelected: () -> Unit,
    onAISelected: () -> Unit,
    onPasteTextSelected: () -> Unit,
    onImportFileSelected: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text("یک واژه جدید اضافه کن", style = MaterialTheme.typography.headlineLarge)
        Text("روش مناسب خودت را انتخاب کن؛ داده‌ها روی دستگاهت می‌مانند.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AddMethod(Icons.Default.Edit, "افزودن دستی", "خودت معنی، مثال و یادداشت را وارد کن", onManualSelected)
        AddMethod(Icons.Default.AutoAwesome, "ترجمه با AI", "چند معنی و مثال پیشنهادی بگیر", onAISelected)
        AddMethod(Icons.Default.ContentPaste, "جای‌گذاری متن", "چندین واژه را یکجا وارد کن", onPasteTextSelected)
        AddMethod(Icons.Default.FileUpload, "Import فایل", "CSV یا JSON را با پیش‌نمایش وارد کن", onImportFileSelected)
    }
}

@Composable
private fun AddMethod(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().height(104.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.card), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        androidx.compose.foundation.layout.Row(Modifier.padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.layout.Box(Modifier.size(52.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.padding(horizontal = Spacing.md)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
