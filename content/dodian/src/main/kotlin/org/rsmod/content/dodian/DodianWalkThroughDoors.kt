package org.rsmod.content.dodian

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.generic.locs.doors.DoorTranslations
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Doors without an open variant (guild doors and the like) that players simply walk through:
 * the door disappears for a moment and the player steps to the other side of the wall. The doors
 * are listed in `.data/dodian/walkthrough-doors.toml`.
 */
class DodianWalkThroughDoors @Inject constructor(private val locRepo: LocRepository) :
    PluginScript() {
    private val logger = InlineLogger()

    override fun ScriptContext.startup() {
        val doors = loadDoors()
        for (door in doors) {
            onOpLoc1(door) { walkThrough(it.vis) }
        }
        logger.info { "Dodian: ${doors.size} walk-through doors." }
    }

    private suspend fun ProtectedAccess.walkThrough(door: BoundLocInfo) {
        val otherSide = DoorTranslations.translateOpen(door.coords, door.shape, door.angle)
        val dest = if (coords == door.coords) otherSide else door.coords
        locRepo.del(door, OPEN_CYCLES)
        teleport(dest)
        delay(1)
    }

    private fun loadDoors(): List<String> {
        if (!doorsFile.exists()) {
            return emptyList()
        }
        return TomlMapper().readTree(doorsFile.toFile()).path("doors").map { it.asText() }
    }

    private companion object {
        const val OPEN_CYCLES = 2
        val doorsFile: Path = Path.of(".data/dodian/walkthrough-doors.toml")
    }
}
