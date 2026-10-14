package org.rsmod.content.dodian

import org.rsmod.content.skills.runecrafting.RunecraftingGates
import org.rsmod.content.skills.runecrafting.essence.EssencePortals
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Points the altar and essence mine exit portals at the ruins and Aubury on the Dodian maps. */
class DodianRunecrafting : PluginScript() {
    override fun ScriptContext.startup() {
        RunecraftingGates.registerEssenceMineExit { EssencePortals.randomizeLocation(AUBURY) }
        RunecraftingGates.registerAltarExit { altar -> RUINS[altar] }
    }

    private companion object {
        val AUBURY = CoordGrid(2653, 3214, 0)

        /** Altar loc to the tile in front of its ruins; altars without ruins keep the OSRS exit. */
        val RUINS =
            mapOf(
                "loc.air_altar" to CoordGrid(2603, 3353, 0),
                "loc.mind_altar" to CoordGrid(2669, 3339, 0),
                "loc.water_altar" to CoordGrid(2727, 3206, 0),
                "loc.earth_altar" to CoordGrid(2674, 3425, 0),
                "loc.fire_altar" to CoordGrid(2457, 3397, 0),
                "loc.body_altar" to CoordGrid(2741, 3262, 0),
                "loc.cosmic_altar" to CoordGrid(2681, 3320, 0),
                "loc.chaos_altar" to CoordGrid(2483, 3264, 0),
                "loc.nature_altar" to CoordGrid(2473, 3213, 0),
            )
    }
}
