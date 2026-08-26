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
 * حالت ۴ - متن مبدأ و ترجمه در همان خط، جدا شده با "=" یا ":":
 * ponerse algo = چیزی پوشیدن
 * solíamos: (از soler) قبلاً می‌کردیم ...
 *
 * حالت ۵ - یک کلمه به‌تنهایی (روی بلوک خودش)، و بعد از یک خط خالی، چند معنی جداگانه که
 * هرکدام باید یک ترجمه مستقل برای همان کلمه شوند (نه یک توضیح واحد):
 * quedar
 *
 * شدن / تبدیل شدن
 * قرار گرفتن
 * واقع شدن / واقع بودن
 * ...
 *
 * رفع باگ (نسخه قبل):
 * - علامت "=" و ":" اصلاً به‌عنوان جداکننده مبدأ/ترجمه تشخیص داده نمی‌شدند؛ این تشخیص فقط
 *   وقتی خط قبلی هنوز معلق نمانده اعمال می‌شود، تا خط توضیحیِ چندخطی که تصادفاً ":" دارد
 *   (مثلاً «فعل: apagar...») به‌اشتباه یک ردیف جدا شکافته نشود.
 * - خط‌هایی که با نشانه Bullet شروع می‌شوند (مثل «* کلمات مشتق شده:») یا حاوی فلش
 *   ارجاع دستوری‌اند (مثل «brille ← فعل brillar»)، به‌عنوان یادداشت اضافه (extraLabel) در
 *   نظر گرفته می‌شوند، نه یک معنی مستقل.
 * - وقتی یک کلمه به‌تنهایی در بلوک خودش می‌آید و بلوک بعدی چند خط معنی دارد، هرکدام از آن
 *   خط‌ها یک ParsedVocabularyEntry جدا (با همان متن مبدأ) می‌شود؛ منطق ادغام موجود در
 *   ImportParsedEntriesUseCase این چند ردیف را در واردسازی نهایی به یک Concept با چند
 *   ترجمه تبدیل می‌کند.
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
    private val inlineColonLine = Regex("^(.+?):\\s*(.+)$")
    private val noteArrowMarker = Regex("←|→|->|<-")
    // یادداشت ریشه‌ای مستقل که در بلوک خودش می‌آید (نه به‌عنوان خط اضافه همان بلوک)،
    // مثل «از admitir» یا «مشتق از permitir». باید به ردیف قبلی متصل شود، نه به‌عنوان یک
    // کلمه تنهای منتظر لیست معنی در نظر گرفته شود (که باعث ادغام اشتباه با بلوک بعدی می‌شد).
    private val standaloneNoteLine = Regex("^(مشتق\\s+)?از\\s+\\S")

    operator fun invoke(rawText: String): List<ParsedVocabularyEntry> {
        val normalizedText = headingEntryStart.replace(rawText) { match -> "\n\n" + match.value }

        val chunks = normalizedText
            .split(Regex("\\n\\s*\\n"))
            .map { block -> block.lines().map { it.trim() }.filter { it.isNotEmpty() } }
            .filter { it.isNotEmpty() }

        var localId = 0
        val results = mutableListOf<ParsedVocabularyEntry>()

        fun addEntry(source: String, target: String, extra: String?) {
            if (source.isNotBlank() && target.isNotBlank()) {
                results.add(ParsedVocabularyEntry(localId++, source, target, extra))
            }
        }

        // خط‌هایی که پس از حذف نشانه‌های تزئینی، معنی مستقل هستند از خط‌های یادداشت/توضیح
        // (شروع با Bullet یا حاوی فلش ارجاع دستوری) جدا می‌شوند.
        fun emitSourceWithMeanings(source: String, meaningAndNoteLines: List<String>) {
            val meaningLines = mutableListOf<String>()
            val noteLines = mutableListOf<String>()
            for (line in meaningAndNoteLines) {
                val isNote = noteArrowMarker.containsMatchIn(line) || bulletPrefix.find(line)?.range?.first == 0
                if (isNote) noteLines.add(line) else meaningLines.add(line)
            }
            if (meaningLines.isEmpty()) return
            val extra = noteLines.takeIf { it.isNotEmpty() }?.joinToString(" — ")
            meaningLines.forEachIndexed { index, meaning ->
                addEntry(source, meaning, if (index == 0) extra else null)
            }
        }

        // اگر همان خط با "=" یا ":" یک جفت کامل مبدأ/ترجمه باشد، بلافاصله ثبت می‌شود.
        fun tryInlinePair(line: String): Boolean {
            val match = inlineEqualsLine.find(line) ?: inlineColonLine.find(line) ?: return false
            val source = bulletPrefix.replace(match.groupValues[1], "").trim()
            val target = match.groupValues[2].trim()
            addEntry(source, target, null)
            return true
        }

        // یک یادداشت ریشه‌ای که خودش در یک بلوک جداگانه آمده (نه به‌عنوان خط اضافه همان
        // بلوک) را به آخرین ردیف ثبت‌شده متصل می‌کند، به‌جای این‌که به اشتباه یک کلمه تنهای
        // منتظر لیست معنی در بلوک بعدی در نظر گرفته شود.
        fun attachNoteToLastEntry(note: String) {
            if (results.isEmpty()) return
            val lastIndex = results.size - 1
            val last = results[lastIndex]
            val mergedExtra = if (last.extraLabel.isNullOrBlank()) note else "${last.extraLabel} — $note"
            results[lastIndex] = last.copy(extraLabel = mergedExtra)
        }

        fun processNormalChunk(cleanedLines: List<String>) {
            val pending = mutableListOf<String>()
            for (line in cleanedLines) {
                val matchedInline = pending.isEmpty() && tryInlinePair(line)
                if (!matchedInline) pending.add(line)
            }
            if (pending.size >= 2) {
                emitSourceWithMeanings(pending[0], pending.drop(1))
            }
        }

        // کلمه‌ای که تنها در بلوک خودش آمده و منتظر بلوک بعدی (لیست معنی‌ها) است.
        var pendingSourceOnly: String? = null

        for (rawLines in chunks) {
            val cleaned = cleanBlockLines(rawLines)
            if (cleaned.isEmpty()) continue

            val sourceOnly = pendingSourceOnly
            if (sourceOnly != null) {
                emitSourceWithMeanings(sourceOnly, cleaned)
                pendingSourceOnly = null
                continue
            }

            if (cleaned.size == 1) {
                val line = cleaned[0]
                if (standaloneNoteLine.containsMatchIn(line)) {
                    attachNoteToLastEntry(line)
                } else if (!tryInlinePair(line)) {
                    pendingSourceOnly = line
                }
                continue
            }

            processNormalChunk(cleaned)
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
