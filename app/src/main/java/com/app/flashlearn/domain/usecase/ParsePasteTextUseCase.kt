package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.ParsedVocabularyEntry
import javax.inject.Inject

/**
 * Parser متن Paste شده (بند 42). فرمت پیش‌فرض مورد انتظار (مطابق نمونه سند اولیه):
 *
 * 1. manzana
 *    سیب
 *    میوه
 *
 * 2. viajar
 *    سفر کردن
 *    فعل
 *
 * یعنی بلوک‌ها با خط خالی از هم جدا می‌شوند؛ خط اول = متن مبدا (با حذف شماره‌گذاری ابتدایی
 * مثل "1." یا "2)")، خط دوم = ترجمه، خط سوم اختیاری = برچسب اضافی (نوع کلمه/دسته).
 * این یک Parser ساده و قطعی است (بدون AI)؛ نسخه هوشمندتر مبتنی بر AI به‌عنوان بهبود بعدی
 * ثبت شده (Gap شناخته‌شده).
 */
class ParsePasteTextUseCase @Inject constructor() {

    operator fun invoke(rawText: String): List<ParsedVocabularyEntry> {
        val blocks = rawText
            .split(Regex("\\n\\s*\\n"))
            .map { block -> block.lines().map { it.trim() }.filter { it.isNotEmpty() } }
            .filter { it.isNotEmpty() }

        return blocks.mapIndexedNotNull { index, lines ->
            if (lines.size < 2) return@mapIndexedNotNull null

            val source = stripLeadingNumbering(lines[0])
            val target = lines[1]
            val extra = lines.getOrNull(2)

            if (source.isBlank() || target.isBlank()) return@mapIndexedNotNull null

            ParsedVocabularyEntry(
                localId = index,
                sourceText = source,
                targetText = target,
                extraLabel = extra
            )
        }
    }

    private fun stripLeadingNumbering(line: String): String =
        line.replace(Regex("^\\s*\\d+[.)\\-]\\s*"), "").trim()
}
