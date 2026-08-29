package com.app.flashlearn.domain.usecase

import com.app.flashlearn.domain.model.ParsedVocabularyEntry
import org.json.JSONArray
import javax.inject.Inject

/**
 * Import File - فرمت JSON (بند 43). فرمت مورد انتظار: آرایه‌ای از آبجکت با کلیدهای
 * source/target (یا text/translation به‌عنوان نام‌های جایگزین رایج) و notes اختیاری.
 * مثال: [{"source":"manzana","target":"سیب","notes":"fruit"}, ...]
 */
class ParseJsonVocabularyUseCase @Inject constructor() {

    operator fun invoke(jsonText: String): List<ParsedVocabularyEntry> {
        val array = JSONArray(jsonText)
        val result = mutableListOf<ParsedVocabularyEntry>()

        for (i in 0 until array.length()) {
            val obj = array.optJSONObject(i) ?: continue
            val source = firstNonBlank(obj.optString("source"), obj.optString("text"), obj.optString("word"))
            val target = firstNonBlank(obj.optString("target"), obj.optString("translation"))
            val notes = obj.optString("notes").ifBlank { null }

            if (source.isNullOrBlank() || target.isNullOrBlank()) continue

            result.add(ParsedVocabularyEntry(localId = i, sourceText = source, targetText = target, extraLabel = notes))
        }

        return result
    }

    private fun firstNonBlank(vararg values: String?): String? =
        values.firstOrNull { !it.isNullOrBlank() }
}
