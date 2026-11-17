Gamesense Essentials with proper **Hebrew / Arabic (RTL) support** for the song name and artist on the SteelSeries OLED.

### What's fixed
- Hebrew and Arabic display in the correct direction, including any mix with English, numbers, brackets, niqqud and emoji (uses the full Unicode Bidirectional Algorithm instead of just reversing text).
- Song name and artist are handled separately, each with its own direction.
- Long names scroll in the reading direction: English starts on the left and scrolls left, Hebrew starts on the right and scrolls right.
- Titles containing " - " (e.g. "Song - Remastered") are no longer cut off.
- New tray option **"Fix Hebrew/Arabic text direction"** (on by default).
- Includes all upstream updates from mtricht/gamesense-essentials.

### Downloads
- **`...-setup.exe`** – Windows installer, Java is bundled. Recommended.
- **`...-portable.zip`** – no install: unzip and run `gamesense-essentials.exe`.
- **`.jar`** – needs Java 21+ installed: `java -jar gamesense-essentials-hebrew-<version>.jar`.
