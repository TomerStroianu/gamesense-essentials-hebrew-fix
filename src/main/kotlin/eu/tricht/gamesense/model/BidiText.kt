package eu.tricht.gamesense.model

import java.text.BreakIterator
import java.text.Bidi

/**
 * The SteelSeries OLED renders characters strictly left-to-right and has no notion of
 * bidirectional text. Hebrew/Arabic (and any mix of them with Latin text and numbers)
 * therefore has to be converted from logical order (the order it is typed/stored in)
 * to visual order (the order it should appear on screen, left to right) before sending.
 *
 * This uses the full Unicode Bidirectional Algorithm (java.text.Bidi), so it handles:
 *  - pure RTL text, pure LTR text, and any mix of the two in either direction
 *  - numbers inside RTL text (they stay left-to-right: "שיר 22" -> "22 ריש")
 *  - punctuation and brackets, which are mirrored inside RTL runs ("(" <-> ")")
 *  - combining marks (Hebrew niqqud, Arabic harakat) and emoji, which are kept
 *    attached to their base character instead of being reversed separately
 */
object BidiText {

    private val BIDI_CONTROLS = Regex("[؜‎‏‪-‮⁦-⁩]")

    private val MIRRORS: Map<Char, Char> = mapOf(
        '(' to ')', ')' to '(',
        '[' to ']', ']' to '[',
        '{' to '}', '}' to '{',
        '<' to '>', '>' to '<',
        '«' to '»', '»' to '«',
        '‹' to '›', '›' to '‹',
        '⁅' to '⁆', '⁆' to '⁅',
        '〈' to '〉', '〉' to '〈',
        '⟨' to '⟩', '⟩' to '⟨',
        '≤' to '≥', '≥' to '≤',
    )

    /** True when the paragraph direction of [text] is right-to-left (first strong character is RTL). */
    fun isRtl(text: String): Boolean {
        if (text.isEmpty() || !Bidi.requiresBidi(text.toCharArray(), 0, text.length)) {
            return false
        }
        return !Bidi(text, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT).baseIsLeftToRight()
    }

    /** Converts logical-order [text] to a list of grapheme clusters in visual (left-to-right) order. */
    fun toVisualClusters(text: String, rtl: Boolean = isRtl(text)): List<String> {
        if (text.isEmpty()) {
            return emptyList()
        }
        if (!Bidi.requiresBidi(text.toCharArray(), 0, text.length)) {
            return clusters(text)
        }
        val bidi = Bidi(text, if (rtl) Bidi.DIRECTION_RIGHT_TO_LEFT else Bidi.DIRECTION_LEFT_TO_RIGHT)
        val runCount = bidi.runCount
        val levels = ByteArray(runCount) { bidi.getRunLevel(it).toByte() }
        // Run indices, reordered into visual order.
        val runs: Array<Any> = Array(runCount) { it }
        Bidi.reorderVisually(levels, 0, runs, 0, runCount)

        val result = ArrayList<String>(text.length)
        for (run in runs) {
            val i = run as Int
            val runClusters = clusters(text.substring(bidi.getRunStart(i), bidi.getRunLimit(i)))
            if (bidi.getRunLevel(i) % 2 == 1) {
                runClusters.asReversed().forEach { result.add(mirror(it)) }
            } else {
                result.addAll(runClusters)
            }
        }
        return result.map { it.replace(BIDI_CONTROLS, "") }.filter { it.isNotEmpty() }
    }

    fun toVisual(text: String, rtl: Boolean = isRtl(text)): String =
        toVisualClusters(text, rtl).joinToString("")

    /** Number of user-perceived characters (grapheme clusters) in [text]. */
    fun length(text: String): Int = clusters(text).size

    private fun mirror(cluster: String): String {
        val first = cluster[0]
        val mirrored = MIRRORS[first] ?: return cluster
        return mirrored + cluster.substring(1)
    }

    /** Splits [text] into grapheme clusters (base character + combining marks, emoji sequences...). */
    fun clusters(text: String): List<String> {
        val iterator = BreakIterator.getCharacterInstance()
        iterator.setText(text)
        val result = ArrayList<String>(text.length)
        var start = iterator.first()
        var end = iterator.next()
        while (end != BreakIterator.DONE) {
            result.add(text.substring(start, end))
            start = end
            end = iterator.next()
        }
        return result
    }
}
