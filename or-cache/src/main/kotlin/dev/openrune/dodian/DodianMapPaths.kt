package dev.openrune.dodian

import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.cache.tools.tasks.impl.PackMaps
import java.io.File
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.io.path.writeText

public object DodianMapPaths {
    private val gameYml: Path = Path.of("../game.yml")
    private val gamemodeLine = Regex("""^gamemode:\s*["']?(\w+)["']?""")

    public val gamemode: String by lazy { resolveGamemode() }

    public val isDodian: Boolean
        get() = gamemode == "dodian"

    private val cacheGamemodeFile: Path = Path.of("../.data/cache/gamemode.txt")

    public fun mapDir(kind: String): Path {
        cacheGamemodeFile.writeText(gamemode)
        return if (isDodian) {
            Path.of("../.data/dodian/map/$kind")
        } else {
            Path.of("../.data/raw-cache/map/$kind")
        }
    }

    public fun mapTasks(): List<CacheTask> {
        if (!isDodian) {
            return listOf(DodianMapGuard())
        }
        val packDir = File("../.data/raw-cache/map/dodian")
        return listOf(DodianMapClear(packDir), PackMaps(packDir), DodianWorldMap(packDir))
    }

    private fun resolveGamemode(): String {
        val override = System.getProperty("gamemode")
        if (!override.isNullOrBlank()) {
            return validate(override.trim().lowercase())
        }
        if (!gameYml.exists()) {
            return "normal"
        }
        val mode =
            gameYml.readLines().firstNotNullOfOrNull { gamemodeLine.find(it.trim()) }?.groupValues?.get(1)
        return validate(mode?.lowercase() ?: "normal")
    }

    private fun validate(mode: String): String {
        require(mode == "normal" || mode == "dodian") {
            "Unknown gamemode '$mode' (expected \"normal\" or \"dodian\")."
        }
        return mode
    }
}
