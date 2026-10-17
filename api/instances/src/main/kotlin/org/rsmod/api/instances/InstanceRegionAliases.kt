package org.rsmod.api.instances

import java.util.concurrent.ConcurrentHashMap
import org.rsmod.map.CoordGrid

/**
 * Lets a server copy an instance from a different map square than the one its script names, e.g.
 * a boss arena moved to another spot on a custom map. Copied regions, the entry and exit coords and
 * npc spawns inside an aliased square all move to the same local position in the alias.
 */
public object InstanceRegionAliases {
    private val aliases = ConcurrentHashMap<Int, Int>()

    public fun register(originalRegion: Int, aliasRegion: Int) {
        aliases[originalRegion] = aliasRegion
    }

    internal fun region(regionId: Int): Int = aliases[regionId] ?: regionId

    internal fun local(local: RegionLocal): RegionLocal {
        val alias = aliases[squareId(local.regionZoneX, local.regionZoneZ)] ?: return local
        return local.copy(regionZoneX = alias shr 8, regionZoneZ = alias and 0xFF)
    }

    internal fun coord(coord: CoordGrid): CoordGrid {
        if (coord == CoordGrid.ZERO || coord == CoordGrid.NULL) {
            return coord
        }
        val alias = aliases[squareId(coord.mx, coord.mz)] ?: return coord
        return CoordGrid(coord.level, alias shr 8, alias and 0xFF, coord.lx, coord.lz)
    }

    internal fun apply(area: InstanceArea.CopyRegions): InstanceArea.CopyRegions {
        if (aliases.isEmpty()) {
            return area
        }
        return area.copy(
            regionIds = area.regionIds.map(::region),
            enterCoord = local(area.enterCoord),
            exitCoord = coord(area.exitCoord),
            npcSpawns = area.npcSpawns.map { it.copy(coord = local(it.coord)) },
        )
    }

    private fun squareId(mx: Int, mz: Int): Int = (mx shl 8) or mz
}
