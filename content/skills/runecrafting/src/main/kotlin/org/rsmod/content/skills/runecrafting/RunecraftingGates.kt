package org.rsmod.content.skills.runecrafting

import java.util.concurrent.CopyOnWriteArrayList
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid

/** Lets other content (e.g. a quest that unlocks Runecrafting) decide who may enter the altars. */
fun interface RunecraftingGate {
    /** The message shown instead of entering the ruins, or null when allowed. */
    fun refusal(player: Player): String?
}

object RunecraftingGates {
    private val gates = CopyOnWriteArrayList<RunecraftingGate>()
    private val essenceMineExits = CopyOnWriteArrayList<(Player) -> CoordGrid?>()
    private val altarExits = CopyOnWriteArrayList<(String) -> CoordGrid?>()

    fun register(gate: RunecraftingGate) {
        gates += gate
    }

    fun refusal(player: Player): String? = gates.firstNotNullOfOrNull { it.refusal(player) }

    /** Overrides where the rune essence mine exit portal sends [Player]s; null keeps the default. */
    fun registerEssenceMineExit(exit: (Player) -> CoordGrid?) {
        essenceMineExits += exit
    }

    fun essenceMineExit(player: Player): CoordGrid? =
        essenceMineExits.firstNotNullOfOrNull { it(player) }

    /** Overrides where an altar's exit portal (and talisman locate) leads, by altar loc name. */
    fun registerAltarExit(exit: (altar: String) -> CoordGrid?) {
        altarExits += exit
    }

    fun altarExit(altar: String): CoordGrid? = altarExits.firstNotNullOfOrNull { it(altar) }
}
