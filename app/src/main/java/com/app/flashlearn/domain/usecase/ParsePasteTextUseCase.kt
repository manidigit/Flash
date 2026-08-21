package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.ParsedVocabularyEntry
import javax.inject.Inject

/**
 * Parser متن Paste شده (بند 42). چند شکل ورودی پشتیبانی می‌شود:
 *
 * حالت ۱ - شماره چسبیده به متن مبدأ، با خط خالی بین ردیف‌ها:
 * 1. manzana
 *    سیب
 *
 * حالت ۲ - شماره روی خط جداگانه، با خط خالی بین ردیف‌ها:
 * ۸۸.
 * estaba fuera de control
 * از کنترل خارج بود
 *
 * حالت ۳ - عنوان Markdown‌ای و بدون خط خالی بین ردیف‌ها:
 * ### ۳۰. apaga la televisión
 * تلویزیون را خاموش کن
 * ### ۳۱. con una condición ...
 * به یک شرط ...
 *
 * حالت ۴ - متن مبدأ و ترجمه در همان خط با علامت "=" جدا شده‌اند (با یا بدون Bullet ابتدایی
 * مثل 🔷 یا *)، حتی چند مورد از این خط‌ها پشت‌سرهم بدون خط خالی بین‌شان:
 * ponerse algo = چیزی پوشیدن
 * 🔷 algo espectacular = چیزی خیلی تماشایی / فوق‌العاده
 *
 * رفع باگ (نسخه قبل): علامت "=" اصلاً به‌عنوان جداکننده متن مبدأ/ترجمه تشخیص داده نمی‌شد و
 * چند خط «=»ای پشت‌سرهم بدون خط خالی، به‌اشتباه به‌عنوان یک ردیف چندخطی واحد (خط اول=مبدأ،
 * خط دوم=ترجمه) در نظر گرفته می‌شدند. حالا هر خطی که با الگوی «متن = ترجمه» مطابقت داشته
 * باشد، مستقل از خط‌های اطرافش، بلافاصله یک ردیف کامل در نظر گرفته می‌شود.
 *
 * این یک Parser ساده و قطعی است (بدون AI)؛ نسخه هوشمندتر مبتنی بر AI به‌عنوان بهبود بعدی
 * ثبت شده (Gap شناخته‌شده).
 */
class ParsePasteTextUseCase @Inject constructor() {

    private val headingEntryStart = Regex("(?m)^\\s*#{1,6}\\s*[0-9۰-۹٠-٩]")
    private val headingPrefix = Regex("^#{1,6}\\s*")
    private val numberOnlyLine = Regex("^[0-9۰-۹٠-٩]+[.)\\-]?\\s*$")
    private val leadingNumberPrefix = Regex("^[0-9۰-۹٠-٩]+[.)\\-]\\s*")
    private val bulletPrefix = Regex("^[\\s]*[•*\\-–—▪●○◦‣·🔷🔶🔹🔸]+\\s*")
    private val inlineEqualsLine = Regex("^(.+?)\\s*=\\s*(.+)$")

    operator fun invoke(rawText: String): List<ParsedVocabularyEntry> {
        // مرز بلوک اجباری جلوی هر خط "### شماره ..." تا حالت ۳ هم مثل بقیه با خط خالی جدا شود.
        val normalizedText = headingEntryStart.replace(rawText) { match -> "\n\n" + match.value }

        val blocks = normalizedText
            .split(Regex("\\n\\s*\\n"))
            .map { block -> block.lines().map { it.trim() }.filter { it.isNotEmpty() } }
            .filter { it.isNotEmpty() }

        var localId = 0
        val results = mutableListOf<ParsedVocabularyEntry>()

        for (rawLines in blocks) {
            val cleanedLines = cleanBlockLines(rawLines)
            if (cleanedLines.isEmpty()) continue

            val pending = mutableListOf<String>()

            fun flushPending() {
                if (pending.size >= 2) {
                    val source = pending[0]
                    val target = pending[1]
                    val extra = pending.drop(2).takeIf { it.isNotEmpty() }?.joinToString(" — ")
                    if (source.isNotBlank() && target.isNotBlank()) {
                        results.add(ParsedVocabularyEntry(localId++, source, target, extra))
                    }
                }
                pending.clear()
            }

            for (line in cleanedLines) {
                val equalsMatch = inlineEqualsLine.find(line)
                if (equalsMatch != null) {
                    // این خط خودش یک ردیف کامل (متن = ترجمه) است؛ اول هر گروه چندخطی معلق را ببند.
                    flushPending()
                    val source = bulletPrefix.replace(equalsMatch.groupValues[1], "").trim()
                    val target = equalsMatch.groupValues[2].trim()
                    if (source.isNotBlank() && target.isNotBlank()) {
                        results.add(ParsedVocabularyEntry(localId++, source, target, null))
                    }
                } else {
                    pending.add(line)
                }
            }
            flushPending()
        }

        return results
    }

    private fun cleanBlockLines(rawLines: List<String>): List<String> {
        if (rawLines.isEmpty()) return rawLines
        val withoutHeading = rawLines.mapIndexed { i, line ->
            if (i == 0) line.replace(headingPrefix, "").trim() else line
        }
        val withoutStandaloneNumber = if (numberOnlyLine.matches(withoutHeading[0])) {
            withoutHeading.drop(1)
        } else {
            withoutHeading
        }
        if (withoutStandaloneNumber.isEmpty()) return withoutStandaloneNumber
        return withoutStandaloneNumber.mapIndexed { i, line ->
            if (i == 0) line.replace(leadingNumberPrefix, "").trim() else line
        }
    }
}
