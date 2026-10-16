package org.rsmod.content.skills.herblore

import java.util.concurrent.CopyOnWriteArrayList
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.game.entity.Player

/** Lets other content (e.g. a quest that unlocks Herblore) decide who may use the skill. */
fun interface HerbloreGate {
    /** The message shown instead of cleaning or mixing, or null when allowed. */
    fun refusal(player: Player): String?
}

object HerbloreGates {
    private val gates = CopyOnWriteArrayList<HerbloreGate>()

    fun register(gate: HerbloreGate) {
        gates += gate
    }

    fun refusal(player: Player): String? = gates.firstNotNullOfOrNull { it.refusal(player) }
}

internal fun ProtectedAccess.herbloreAllowed(): Boolean {
    val refusal = HerbloreGates.refusal(player) ?: return true
    mes(refusal)
    return false
}
