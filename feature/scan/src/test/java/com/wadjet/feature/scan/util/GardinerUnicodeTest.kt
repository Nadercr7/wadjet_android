package com.wadjet.feature.scan.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for [gardinerToUnicode] — Gardiner code → Unicode hieroglyph mapping.
 */
class GardinerUnicodeTest {

    // ── Helper ──

    private fun codepoint(gardinerCode: String): Int {
        val result = gardinerToUnicode(gardinerCode)
        assertTrue(
            "Expected supplementary character for $gardinerCode but got '${result}'",
            result.length == 2 && Character.isHighSurrogate(result[0])
        )
        return Character.codePointAt(result, 0)
    }

    // ══════════════════════════════════════════════════
    //  COMMON_GLYPHS — known code lookups
    // ══════════════════════════════════════════════════

    @Test fun `A1 is U+13000 Seated man`() =
        assertEquals(0x13000, codepoint("A1"))

    @Test fun `D21 is Mouth`() =
        assertEquals(0x1308B, codepoint("D21"))

    @Test fun `G1 is Vulture`() =
        assertEquals(0x1313F, codepoint("G1"))

    @Test fun `G17 is Owl`() =
        assertEquals(0x13153, codepoint("G17"))

    @Test fun `M17 is Reed`() =
        assertEquals(0x131CB, codepoint("M17"))

    @Test fun `N5 is Sun`() =
        assertEquals(0x131F3, codepoint("N5"))

    @Test fun `N35 is Water`() =
        assertEquals(0x13216, codepoint("N35"))

    @Test fun `S34 is Ankh`() =
        assertEquals(0x132F9, codepoint("S34"))

    @Test fun `X1 is Bread loaf`() =
        assertEquals(0x133CF, codepoint("X1"))

    @Test fun `Z1 is Single stroke`() =
        assertEquals(0x133E4, codepoint("Z1"))

    @Test fun `Aa1 is Placenta`() =
        assertEquals(0x1340D, codepoint("Aa1"))

    // ══════════════════════════════════════════════════
    //  Algorithmic fallback via UNICODE_MAP
    // ══════════════════════════════════════════════════

    @Test fun `uncommon code A3 resolved via UNICODE_MAP`() {
        val result = gardinerToUnicode("A3")
        // Should resolve to a valid hieroglyph (not returned as-is)
        assertNotEquals("A3", result)
        val cp = Character.codePointAt(result, 0)
        assertTrue("Codepoint for A3 should be in hieroglyph block", cp in 0x13000..0x1342E)
    }

    @Test fun `uncommon code D2 resolved via UNICODE_MAP`() {
        val result = gardinerToUnicode("D2")
        assertNotEquals("D2", result)
    }

    // ══════════════════════════════════════════════════
    //  GARDINER_REGEX pattern matching
    // ══════════════════════════════════════════════════

    @Test fun `simple code like A1 matches regex`() {
        val result = gardinerToUnicode("A1")
        assertNotEquals("A1", result)
    }

    @Test fun `multi-letter prefix like Aa1 matches regex`() {
        val result = gardinerToUnicode("Aa1")
        assertNotEquals("Aa1", result)
    }

    @Test fun `two-digit number like D21 matches regex`() {
        val result = gardinerToUnicode("D21")
        assertNotEquals("D21", result)
    }

    @Test fun `three-digit number like A100 attempts lookup`() {
        // A100 may or may not exist, but the regex should still match
        val result = gardinerToUnicode("A100")
        // If not found in Unicode block, returns as-is
        // Just verify no crash
        assertTrue(result.isNotEmpty())
    }

    // ══════════════════════════════════════════════════
    //  Unknown / invalid codes — fallback behavior
    // ══════════════════════════════════════════════════

    @Test fun `unknown code returns as-is`() {
        assertEquals("ZZ999", gardinerToUnicode("ZZ999"))
    }

    @Test fun `non-Gardiner text returns as-is`() {
        assertEquals("hello", gardinerToUnicode("hello"))
    }

    @Test fun `empty string returns as-is`() {
        assertEquals("", gardinerToUnicode(""))
    }

    @Test fun `pure number returns as-is`() {
        assertEquals("123", gardinerToUnicode("123"))
    }

    // ══════════════════════════════════════════════════
    //  COMMON_GLYPHS consistency — all produce valid hieroglyphs
    // ══════════════════════════════════════════════════

    @Test fun `all COMMON_GLYPHS produce supplementary characters`() {
        val codes = listOf(
            "A1", "A2", "D1", "D4", "D21", "D36", "D46", "D58",
            "G1", "G5", "G17", "G43",
            "I9", "I10",
            "M17", "M23",
            "N1", "N5", "N29", "N35",
            "O1", "O4", "O34",
            "Q3",
            "R4",
            "S29", "S34",
            "T14",
            "U1",
            "V13", "V28", "V31",
            "W11",
            "X1",
            "Y1",
            "Z1", "Z4",
            "Aa1",
        )
        for (code in codes) {
            val result = gardinerToUnicode(code)
            val cp = Character.codePointAt(result, 0)
            assertTrue(
                "$code: U+${cp.toString(16).uppercase()} should be in Egyptian Hieroglyph block (U+13000–U+1342E)",
                cp in 0x13000..0x1342E,
            )
        }
    }
}
