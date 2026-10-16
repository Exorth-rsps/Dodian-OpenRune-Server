package org.rsmod.api.player.worn

import dev.openrune.types.ItemServerType
import java.util.concurrent.CopyOnWriteArrayList
import org.rsmod.game.entity.Player

/** Lets content (e.g. a quest that unlocks an armour piece) refuse equipping an item. */
public fun interface EquipGate {
    /** The message shown instead of equipping [type], or null when allowed. */
    public fun refusal(player: Player, type: ItemServerType): String?
}

public object EquipGates {
    private val gates = CopyOnWriteArrayList<EquipGate>()

    public fun register(gate: EquipGate) {
        gates += gate
    }

    public fun refusal(player: Player, type: ItemServerType): String? =
        gates.firstNotNullOfOrNull { it.refusal(player, type) }
}
