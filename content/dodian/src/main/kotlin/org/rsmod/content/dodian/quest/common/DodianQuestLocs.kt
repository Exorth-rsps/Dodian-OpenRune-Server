package org.rsmod.content.dodian.quest.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.github.michaelbull.logging.InlineLogger
import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCMType
import java.nio.file.Path
import kotlin.io.path.exists
import org.rsmod.map.CoordGrid

/** The quest objects listed in `.data/dodian/quest-locs.toml`; unknown loc names are skipped. */
object DodianQuestLocs {
    private val logger = InlineLogger()
    private val file: Path = Path.of(".data/dodian/quest-locs.toml")

    private val tree: JsonNode? by lazy {
        if (file.exists()) TomlMapper().readTree(file.toFile()) else null
    }

    fun locs(key: String): List<String> {
        val names = tree?.path(key)?.map { it.asText() }.orEmpty()
        return names.filter { name ->
            val known = RSCM.getRSCMOrNull(name, RSCMType.LOC) != null
            if (!known) {
                logger.warn { "$file: '$key' lists unknown loc '$name', skipped." }
            }
            known
        }
    }

    fun coord(key: String): CoordGrid? {
        val text = tree?.path(key)?.asText().orEmpty().trim()
        if (text.isEmpty()) {
            return null
        }
        val parts = text.split('_').map { it.toInt() }
        require(parts.size == 5) { "$file: '$key' must be level_mapsquareX_mapsquareZ_localX_localZ" }
        val (level, mx, mz, lx, lz) = parts
        return CoordGrid(mx * 64 + lx, mz * 64 + lz, level)
    }
}
