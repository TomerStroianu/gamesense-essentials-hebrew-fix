package eu.tricht.gamesense.model

data class SongInformation(
    val fullSongName: String
) {
    // Split on the first " - " only, so titles like "Song - Remastered 2011" stay intact.
    // Artist and song are handled independently, each with its own text direction.
    private val parts = fullSongName.split(" - ", limit = 2)
    private val artistText = ScrollingText(parts[0].trim())
    private val songText = ScrollingText(parts.getOrElse(1) { "" }.trim())
    fun artist() = artistText.text
    fun song() = songText.text
}
