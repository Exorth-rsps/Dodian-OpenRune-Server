package org.rsmod.content.dodian

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.github.michaelbull.logging.InlineLogger
import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.forcedWalk
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.generic.locs.doors.DoorTranslations
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Doors without an open variant (quest, lock and guild doors) that players may pass on the Dodian
 * maps: the door swings open for a moment and the player steps to the other side. The doors are
 * listed in `.data/dodian/swing-doors.toml`.
 */
class DodianSwingDoors @Inject constructor(private val locRepo: LocRepository) : PluginScript() {
    private val logger = InlineLogger()

    override fun ScriptContext.startup() {
        val doors = loadDoors()
        for (door in doors) {
            onOpLoc1(door) {
                val door = it.vis
                val otherSide = DoorTranslations.translateOpen(door.coords, door.shape, door.angle)
                val dest = if (door.isOnDoorTileSide(coords)) otherSide else door.coords
                swingThrough(locRepo, door, dest)
            }
        }
        logger.info { "Dodian: ${doors.size} swing doors." }
    }

    private fun loadDoors(): List<String> {
        if (!doorsFile.exists()) {
            return emptyList()
        }
        return TomlMapper().readTree(doorsFile.toFile()).path("doors").map { it.asText() }
    }

    private companion object {
        val doorsFile: Path = Path.of(".data/dodian/swing-doors.toml")
    }
}

/** Swings [door] open where an opened door would stand and walks the player through to [dest]. */
internal suspend fun ProtectedAccess.swingThrough(
    locRepo: LocRepository,
    door: BoundLocInfo,
    dest: CoordGrid,
) {
    val openCoords = DoorTranslations.translateOpen(door.coords, door.shape, door.angle)
    val internal = RSCM.getReverseMapping(RSCMType.LOC, door.id)
    locRepo.del(door, SWING_CYCLES)
    locRepo.add(openCoords, internal, SWING_CYCLES, door.turnAngle(rotations = 1), door.shape)
    forcedWalk(dest, crossTiles = 1)
}

/** True when [coords] is on the same side of the wall as the door's own tile. */
internal fun BoundLocInfo.isOnDoorTileSide(coords: CoordGrid): Boolean {
    val across = DoorTranslations.translateOpen(this.coords, shape, angle)
    val dx = across.x - this.coords.x
    val dz = across.z - this.coords.z
    return (coords.x - this.coords.x) * dx + (coords.z - this.coords.z) * dz <= 0
}

private const val SWING_CYCLES = 3
