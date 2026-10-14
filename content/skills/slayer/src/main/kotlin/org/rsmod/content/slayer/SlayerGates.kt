package org.rsmod.content.slayer

import java.util.concurrent.CopyOnWriteArrayList
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.game.entity.Player

/** Lets other content (e.g. a quest that unlocks Slayer) decide who may use the slayer masters. */
interface SlayerGate {
    /** The line a master says instead of assigning a task or trading, or null when allowed. */
    fun refusal(player: Player): String?

    /** Runs before a master's own talk-to dialogue; returns true when it handled the conversation. */
    suspend fun Dialogue.beforeTalk(npcId: Int): Boolean = false
}

object SlayerGates {
    private val gates = CopyOnWriteArrayList<SlayerGate>()

    fun register(gate: SlayerGate) {
        gates += gate
    }

    fun refusal(player: Player): String? = gates.firstNotNullOfOrNull { it.refusal(player) }

    suspend fun Dialogue.beforeTalk(npcId: Int): Boolean =
        gates.any { gate -> with(gate) { beforeTalk(npcId) } }
}
