package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.ParsedVocabularyEntry
import javax.inject.Inject

/**
 * Import File - فرمت CSV (بند 43). ستون‌های مورد انتظار: source,target,notes(اختیاری).
 * ردیف اول در صورتی که شبیه Header باشد (مثلاً شامل کلمه "source" یا "text") نادیده گرفته می‌شود.
 * این یک Parser ساده خطی است؛ کاما داخل فیلد با Quote (`"a,b"`) پشتیبانی نمی‌شود —
 * برای فایل‌های CSV پیچیده‌تر، Import via AI یا یک کتابخانه CSV کامل در آینده اضافه شود (Gap).
 */
class ParseCsvVocabularyUseCase @Inject constructor() {

    operator fun invoke(csvText: String): List<ParsedVocabularyEntry> {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val startIndex = if (looksLikeHeader(lines.first())) 1 else 0

        return lines.drop(startIndex).mapIndexedNotNull { index, line ->
            val columns = line.split(",").map { it.trim().trim('"') }
            if (columns.size < 2) return@mapIndexedNotNull null

            val source = columns[0]
            val target = columns[1]
            val notes = columns.getOrNull(2)?.ifBlank { null }

            if (source.isBlank() || target.isBlank()) return@mapIndexedNotNull null

            ParsedVocabularyEntry(localId = index, sourceText = source, targetText = target, extraLabel = notes)
        }
    }

    private fun looksLikeHeader(firstLine: String): Boolean {
        val lower = firstLine.lowercase()
        return lower.contains("source") || lower.contains("target") || lower.contains("translation")
    }
}
