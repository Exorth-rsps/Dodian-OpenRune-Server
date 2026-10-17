package org.rsmod.content.dodian

import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCMType
import dev.openrune.types.ObjectServerType
import jakarta.inject.Inject
import org.rsmod.api.config.refs.params
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** The Yanille manhole and the ladder back up from the sewers (with Scurrius' lair) below it. */
class DodianSewers @Inject constructor(private val locRepo: LocRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(MANHOLE_CLOSED) { openManhole(it.vis) }
        onOpLoc2(MANHOLE_OPEN) { closeManhole(it.vis) }
        onOpLoc1(MANHOLE_OPEN) { climb(it.type, SEWER_ARRIVAL) }
        onOpLoc1(SEWER_LADDER) { climb(it.type, MANHOLE_ARRIVAL) }
    }

    private fun ProtectedAccess.openManhole(manhole: BoundLocInfo) {
        locRepo.change(manhole, MANHOLE_OPEN, MANHOLE_OPEN_CYCLES)
        mes("You pull back the cover of the manhole.")
    }

    private fun ProtectedAccess.closeManhole(manhole: BoundLocInfo) {
        locRepo.change(manhole, MANHOLE_CLOSED, MANHOLE_OPEN_CYCLES)
        mes("You place the cover back over the manhole.")
    }

    private suspend fun ProtectedAccess.climb(type: ObjectServerType, dest: CoordGrid) {
        arriveDelay()
        anim(RSCM.getReverseMapping(RSCMType.SEQ, type.param(params.climb_anim).id))
        delay(1)
        telejump(dest)
    }

    private companion object {
        const val MANHOLE_CLOSED = "loc.manholeclosed"
        const val MANHOLE_OPEN = "loc.manholeopen"
        const val SEWER_LADDER = "loc.fai_varrock_manhole_ladder"
        const val MANHOLE_OPEN_CYCLES = 500

        val SEWER_ARRIVAL = CoordGrid(2469, 9539, 0)
        val MANHOLE_ARRIVAL = CoordGrid(2652, 3236, 0)
    }
}
