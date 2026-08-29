package com.app.flashlearn.core.util

/** Extracts parenthetical annotations from vocabulary text into notes. */
object ParenthesesUtils {
    private val parentheses = Regex("[\\(（]([^\\)）]*)[\\)）]")
    data class Extraction(val cleanText: String, val notes: List<String>)

    fun extract(text: String): Extraction {
        if (text.isBlank()) return Extraction(text, emptyList())
        val notes = parentheses.findAll(text).map { it.groupValues[1].trim() }.filter { it.isNotBlank() }.toList()
        if (notes.isEmpty()) return Extraction(text.trim(), emptyList())
        val clean = parentheses.replace(text, " ").replace(Regex("\\s{2,}"), " ").trim()
        return Extraction(clean, notes)
    }

    fun mergeNotes(existing: String?, additions: List<String>): String? {
        val all = buildList {
            existing?.split(Regex("\\s*[—|]\\s*"))?.map { it.trim() }?.filter { it.isNotBlank() }?.let(::addAll)
            additions.map { it.trim() }.filter { it.isNotBlank() }.let(::addAll)
        }.distinct()
        return all.takeIf { it.isNotEmpty() }?.joinToString(" — ")
    }
}
