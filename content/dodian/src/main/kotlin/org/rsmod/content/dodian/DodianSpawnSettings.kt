package org.rsmod.content.dodian

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.github.michaelbull.logging.InlineLogger
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.extension
import org.rsmod.api.script.onEvent
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.npc.NpcStateEvents
import org.rsmod.game.map.Direction
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Applies the optional `face` and `wander` values of `[[spawn]]` entries in
 * `.data/dodian/map/npcs/` to the npcs spawned there. The cache only stores npc type and tile, so
 * these are read from the spawn files at boot and matched on npc type + spawn tile.
 */
class DodianSpawnSettings : PluginScript() {
    private val logger = InlineLogger()
    private val settings = HashMap<Key, Setting>()

    override fun ScriptContext.startup() {
        load()
        onEvent<NpcStateEvents.Create> { apply(npc) }
        onEvent<NpcStateEvents.Respawn> { apply(npc) }
    }

    private fun apply(npc: Npc) {
        val setting = settings[Key(npc.id, npc.spawnCoords)] ?: return
        setting.wander?.let { npc.wanderRangeOverride = it }
        setting.face?.let {
            npc.respawnDir = it
            npc.faceDirection(it)
        }
    }

    private fun load() {
        if (!spawnDir.exists()) {
            return
        }
        val files = Files.list(spawnDir).use { paths -> paths.filter { it.extension == "toml" }.toList() }
        for (file in files) {
            for (spawn in TomlMapper().readTree(file.toFile()).path("spawn")) {
                val face = spawn.path("face").asText("").takeIf { it.isNotEmpty() }
                val wander = spawn.get("wander")?.asInt()
                if (face == null && wander == null) {
                    continue
                }
                val direction =
                    face?.let { name -> Direction.entries.firstOrNull { it.name.equals(name.replace("_", ""), true) } }
                if (face != null && direction == null) {
                    logger.warn { "$file: unknown face '$face' (north, northeast, east, southeast, south, southwest, west, northwest)." }
                }
                val npc = spawn.path("npc").asText().asRSCM(RSCMType.NPC)
                val (level, mx, mz, lx, lz) = spawn.path("coords").asText().split('_').map(String::toInt)
                val coords = CoordGrid(mx * 64 + lx, mz * 64 + lz, level)
                settings[Key(npc, coords)] = Setting(direction, wander)
            }
        }
        logger.info { "Dodian: loaded face/wander settings for ${settings.size} npc spawns." }
    }

    private data class Key(val npc: Int, val coords: CoordGrid)

    private data class Setting(val face: Direction?, val wander: Int?)

    private companion object {
        val spawnDir: Path = Path.of(".data/dodian/map/npcs")
    }
}
