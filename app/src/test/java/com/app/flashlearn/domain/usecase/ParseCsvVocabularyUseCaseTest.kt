package com.app.flashlearn.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParseCsvVocabularyUseCaseTest {

    private val useCase = ParseCsvVocabularyUseCase()

    @Test
    fun parsesRowsWithoutHeader() {
        val csv = "manzana,سیب\nviajar,سفر کردن,فعل"
        val result = useCase(csv)

        assertEquals(2, result.size)
        assertEquals("manzana", result[0].sourceText)
        assertEquals("سیب", result[0].targetText)
        assertNull(result[0].extraLabel)
        assertEquals("فعل", result[1].extraLabel)
    }

    @Test
    fun skipsHeaderRow() {
        val csv = "source,target,notes\nmanzana,سیب,میوه"
        val result = useCase(csv)

        assertEquals(1, result.size)
        assertEquals("manzana", result[0].sourceText)
    }

    @Test
    fun skipsMalformedRows() {
        val csv = "manzana\nviajar,سفر کردن"
        val result = useCase(csv)

        assertEquals(1, result.size)
        assertEquals("viajar", result[0].sourceText)
    }

    @Test
    fun emptyInput_returnsEmptyList() {
        assertEquals(0, useCase("").size)
    }
}
