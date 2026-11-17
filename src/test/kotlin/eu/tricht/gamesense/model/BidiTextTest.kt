package eu.tricht.gamesense.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BidiTextTest {

    @Test
    fun `plain english is untouched`() {
        assertFalse(BidiText.isRtl("Hello World"))
        assertEquals("Hello World", BidiText.toVisual("Hello World"))
    }

    @Test
    fun `plain hebrew is reversed`() {
        assertTrue(BidiText.isRtl("שלום"))
        assertEquals("םולש", BidiText.toVisual("שלום"))
    }

    @Test
    fun `numbers inside hebrew keep their order`() {
        assertEquals("22 ריש", BidiText.toVisual("שיר 22"))
        assertEquals("2024 תנש לש ריש", BidiText.toVisual("שיר של שנת 2024"))
    }

    @Test
    fun `hebrew word inside english sentence`() {
        assertFalse(BidiText.isRtl("Love שיר song"))
        assertEquals("Love ריש song", BidiText.toVisual("Love שיר song"))
    }

    @Test
    fun `english word inside hebrew sentence`() {
        assertTrue(BidiText.isRtl("שיר Love אהבה"))
        assertEquals("הבהא Love ריש", BidiText.toVisual("שיר Love אהבה"))
    }

    @Test
    fun `multi word english inside hebrew keeps word order`() {
        assertEquals("Feat. Noa Kirel טיפ", BidiText.toVisual("פיט Feat. Noa Kirel"))
    }

    @Test
    fun `brackets are mirrored in hebrew`() {
        assertEquals("(סקימר) ריש", BidiText.toVisual("שיר (רמיקס)"))
    }

    @Test
    fun `english in brackets inside hebrew`() {
        assertEquals("(Remix) ריש", BidiText.toVisual("שיר (Remix)"))
    }

    @Test
    fun `niqqud stays attached to its letter`() {
        // שָׁלוֹם with niqqud
        val logical = "שָׁלוֹם"
        val visual = "םוֹלשָׁ"
        assertEquals(visual, BidiText.toVisual(logical))
    }

    @Test
    fun `arabic is reversed too`() {
        assertTrue(BidiText.isRtl("مرحبا"))
        assertEquals("ابحرم", BidiText.toVisual("مرحبا"))
    }

    @Test
    fun `emoji survive reversal`() {
        assertEquals("🎵 ריש", BidiText.toVisual("שיר 🎵"))
    }

    @Test
    fun `bidi control characters are stripped`() {
        assertEquals("םולש", BidiText.toVisual("‏שלום‎"))
    }

    @Test
    fun `direction comes from first strong character`() {
        assertTrue(BidiText.isRtl("2 שירים"))
        assertFalse(BidiText.isRtl("2Pac שיר"))
        assertFalse(BidiText.isRtl("12345"))
        assertFalse(BidiText.isRtl(""))
    }
}
