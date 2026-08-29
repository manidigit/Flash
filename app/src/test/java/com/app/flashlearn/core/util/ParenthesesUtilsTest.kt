package com.app.flashlearn.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ParenthesesUtilsTest {
    @Test fun extractsNormalParentheses() {
        val x = ParenthesesUtils.extract("ponerse (algo) una prenda")
        assertEquals("ponerse una prenda", x.cleanText)
        assertEquals(listOf("algo"), x.notes)
    }

    @Test fun extractsPersianParentheses() {
        val x = ParenthesesUtils.extract("banco（نیمکت）")
        assertEquals("banco", x.cleanText)
        assertEquals(listOf("نیمکت"), x.notes)
    }
}
