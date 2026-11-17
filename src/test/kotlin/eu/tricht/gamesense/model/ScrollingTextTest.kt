package eu.tricht.gamesense.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScrollingTextTest {

    private fun scrolling(text: String, separator: String = "") =
        ScrollingText(text, maxDisplayLength = 12, separator = separator, pauseTicks = 0, fixBidi = true)

    @Test
    fun `short text does not scroll`() {
        val s = scrolling("שיר")
        assertFalse(s.scrolls)
        repeat(5) { assertEquals("ריש", s.text) }
    }

    @Test
    fun `long english scrolls left from its start`() {
        val s = scrolling("abcdefghijklm")
        assertEquals("abcdefghijklm ", s.text) // pause at start
        assertEquals("bcdefghijklm a", s.text)
        assertEquals("cdefghijklm ab", s.text)
    }

    @Test
    fun `long hebrew starts at its right edge and scrolls right`() {
        val s = scrolling("אבגדהוזחטיכלמ") // 13 letters
        assertTrue(s.rtl)
        // The start of the text (א) is on the right, the overflowing end is clipped on the left.
        assertEquals("לכיטחזוהדגבא", s.text)
        // Content moves right: א leaves on the right, מ comes in on the left.
        assertEquals("מלכיטחזוהדגב", s.text)
        assertEquals(" מלכיטחזוהדג", s.text)
    }

    @Test
    fun `hebrew marquee wraps around back to the start`() {
        val s = scrolling("אבגדהוזחטיכלמ", separator = "| ")
        val first = s.text
        val frames = (1..40).map { s.text }
        assertTrue(frames.contains(first), "marquee should come back to its first frame")
        assertTrue(frames.any { it.contains("|") }, "separator should scroll into view")
    }

    @Test
    fun `mixed hebrew with english scrolls as rtl`() {
        val s = scrolling("שיר Love ארוך מאוד")
        assertTrue(s.rtl)
        val first = s.text
        assertTrue(first.endsWith("ריש"), "beginning of the hebrew text must be visible on the right: $first")
        assertEquals(12, first.length)
    }

    @Test
    fun `mixed english with hebrew scrolls as ltr`() {
        val s = scrolling("Love song שיר ארוך")
        assertFalse(s.rtl)
        assertTrue(s.text.startsWith("Love song "))
        assertTrue(s.text.startsWith("ove song "))
    }

    @Test
    fun `bidi fix can be disabled`() {
        val s = ScrollingText("שיר", maxDisplayLength = 12, separator = "", pauseTicks = 0, fixBidi = false)
        assertEquals("שיר", s.text)
    }
}
