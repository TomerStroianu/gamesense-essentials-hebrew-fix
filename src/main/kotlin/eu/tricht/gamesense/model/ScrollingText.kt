package eu.tricht.gamesense.model

import eu.tricht.gamesense.Tick
import eu.tricht.gamesense.preferences

/**
 * Text for one OLED line. Text that doesn't fit is scrolled as a marquee.
 *
 * Bidirectional text (Hebrew, Arabic, mixed with Latin/numbers) is converted to visual order,
 * because the OLED only draws left to right. The scroll direction follows the reading
 * direction of the text:
 *  - left-to-right text starts at its left edge and moves left (like the original behaviour)
 *  - right-to-left text starts at its right edge (where it begins) and moves right
 */
class ScrollingText(
    original: String,
    private val maxDisplayLength: Int = defaultMaxDisplayLength(),
    separator: String = defaultSeparator(),
    private val pauseTicks: Int = Tick.msToTicks(200),
    fixBidi: Boolean = preferences.get("fixRtl", "true")!!.toBoolean(),
) {
    val rtl: Boolean = fixBidi && BidiText.isRtl(original)
    val scrolls: Boolean = BidiText.length(original) > maxDisplayLength

    private val strip: List<String>
    private var offset = 0
    private var allowedToPause = true
    private var ticksToWait = pauseTicks

    init {
        val logical = if (scrolls) "$original $separator" else original
        strip = if (fixBidi) BidiText.toVisualClusters(logical, rtl) else BidiText.clusters(logical)
    }

    /** Current frame, advancing the marquee by one step where appropriate. */
    val text: String
        get() {
            if (!scrolls) {
                return frame()
            }
            if (ticksToWait > 0) {
                ticksToWait--
                allowedToPause = false
                return frame()
            }
            if (offset == 0 && allowedToPause) {
                ticksToWait = pauseTicks
                allowedToPause = false
                return frame()
            }
            allowedToPause = true
            offset = (offset + 1) % strip.size
            return frame()
        }

    /** The frame currently shown, without advancing. */
    fun frame(): String {
        if (!scrolls) {
            return strip.joinToString("")
        }
        val size = strip.size
        return if (!rtl) {
            // Rotate left: the visible window is the left part of the string.
            (0 until size).joinToString("") { strip[(it + offset) % size] }
        } else {
            // Rotate right, and only send the right-most window so the beginning of the text
            // (which is on the right for RTL) is what's visible.
            val window = minOf(maxDisplayLength, size)
            (size - window until size).joinToString("") { strip[((it - offset) % size + size) % size] }
        }
    }

    companion object {
        fun defaultMaxDisplayLength(): Int =
            if (preferences.get("songIcon", "true")!!.toBoolean()) 12 else 21

        fun defaultSeparator(): String =
            if (preferences.get("songSeparator", "false")!!.toBoolean()) "| " else ""
    }
}
