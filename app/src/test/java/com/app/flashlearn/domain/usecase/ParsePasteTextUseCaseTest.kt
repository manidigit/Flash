package com.app.flashlearn.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParsePasteTextUseCaseTest {

    private val useCase = ParsePasteTextUseCase()

    @Test
    fun parsesNumberedBlocksSeparatedByBlankLines() {
        val text = """
            1. manzana
               سیب
               میوه

            2. viajar
               سفر کردن
               فعل

            3. hotel
               هتل
               مکان اقامت
        """.trimIndent()

        val result = useCase(text)

        assertEquals(3, result.size)
        assertEquals("manzana", result[0].sourceText)
        assertEquals("سیب", result[0].targetText)
        assertEquals("میوه", result[0].extraLabel)
        assertEquals("viajar", result[1].sourceText)
        assertEquals("hotel", result[2].sourceText)
    }

    @Test
    fun worksWithoutNumbering() {
        val text = """
            gracias
            ممنون
        """.trimIndent()

        val result = useCase(text)

        assertEquals(1, result.size)
        assertEquals("gracias", result[0].sourceText)
        assertEquals("ممنون", result[0].targetText)
        assertNull(result[0].extraLabel)
    }

    @Test
    fun skipsBlocksWithOnlyOneLine() {
        val text = """
            1. manzana

            2. viajar
               سفر کردن
        """.trimIndent()

        val result = useCase(text)

        assertEquals(1, result.size)
        assertEquals("viajar", result[0].sourceText)
    }

    @Test
    fun emptyInput_returnsEmptyList() {
        assertEquals(0, useCase("").size)
        assertEquals(0, useCase("   \n\n  ").size)
    }

    @Test
    fun handlesDifferentNumberingStyles() {
        val text = """
            1) casa
               خانه

            2- comer
               خوردن
        """.trimIndent()

        val result = useCase(text)

        assertEquals(2, result.size)
        assertEquals("casa", result[0].sourceText)
        assertEquals("comer", result[1].sourceText)
    }
}
