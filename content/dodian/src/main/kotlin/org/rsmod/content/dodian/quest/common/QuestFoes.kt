package org.rsmod.content.dodian.quest.common

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import org.rsmod.api.death.NpcAttackValidateHook
import org.rsmod.api.death.NpcAttackValidateResult
import org.rsmod.api.death.NpcDeathKillContext
import org.rsmod.api.death.NpcDeathKillHook
import org.rsmod.api.death.PlayerDeathCleanupHook
import org.rsmod.api.npc.interact.AiPlayerInteractions
import org.rsmod.api.npc.isInCombat
import org.rsmod.api.npc.opPlayer2
import org.rsmod.api.npc.owner.assignSpawnOwner
import org.rsmod.api.npc.owner.isSpawnOwnedByOther
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.game.MapClock
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.entity.player.PlayerUid
import org.rsmod.map.CoordGrid

/**
 * Quest monsters summoned for one player (Nezikchened, Delrith): only their owner may fight them,
 * and they leave when the owner dies, logs out, walks away from the ritual spot or stops fighting.
 * Each quest calls [tick] from an ai timer on the foe's npc type.
 */
@Singleton
class QuestFoes
@Inject
constructor(
    private val npcRepo: NpcRepository,
    private val playerList: PlayerList,
    private val mapClock: MapClock,
    private val interactions: AiPlayerInteractions,
) {
    private class Foe(val owner: PlayerUid, val anchor: CoordGrid, var lastFight: Int, val spawned: Int)

    private val foes = ConcurrentHashMap<Npc, Foe>()
    private val killListeners = CopyOnWriteArrayList<(Npc, Player) -> Unit>()

    fun spawn(owner: Player, type: String, coords: CoordGrid, anchor: CoordGrid, attack: Boolean): Npc {
        val npc = Npc(type, coords)
        npc.respawns = false
        npcRepo.add(npc, MAX_LIFESPAN_CYCLES)
        npc.assignSpawnOwner(owner, mapClock.cycle)
        foes[npc] = Foe(owner.uid, anchor, mapClock.cycle, mapClock.cycle)
        npc.aiTimer(TICK_CYCLES)
        if (attack) {
            npc.opPlayer2(owner, interactions)
        }
        return npc
    }

    fun active(owner: Player, vararg types: String): Npc? =
        foes.entries
            .firstOrNull { (npc, foe) ->
                foe.owner == owner.uid && npc.isSlotAssigned && types.any { npc.isType(it) }
            }
            ?.key

    fun isFoe(npc: Npc): Boolean = foes.containsKey(npc)

    fun owner(npc: Npc): Player? = foes[npc]?.owner?.resolve(playerList)

    fun ageCycles(npc: Npc): Int = foes[npc]?.let { mapClock.cycle - it.spawned } ?: 0

    fun despawn(npc: Npc) {
        if (foes.remove(npc) != null && npc.isSlotAssigned) {
            npcRepo.del(npc, 0)
        }
    }

    fun onKill(listener: (npc: Npc, killer: Player) -> Unit) {
        killListeners += listener
    }

    /** Despawns [npc] when its owner is gone, too far away or no longer fighting it. */
    fun tick(npc: Npc): Boolean {
        val foe = foes[npc] ?: return false
        if (!npc.isSlotAssigned) {
            foes.remove(npc)
            return false
        }
        val owner = foe.owner.resolve(playerList)
        val away =
            owner == null ||
                owner.coords.level != foe.anchor.level ||
                owner.coords.chebyshevDistance(foe.anchor) > LEASH_DISTANCE
        if (npc.isInCombat()) {
            foe.lastFight = mapClock.cycle
        }
        if (away || mapClock.cycle - foe.lastFight > IDLE_CYCLES) {
            despawn(npc)
            return false
        }
        npc.aiTimer(TICK_CYCLES)
        return true
    }

    internal fun killed(npc: Npc, killer: Player) {
        if (foes.remove(npc) == null) {
            return
        }
        killListeners.forEach { it(npc, killer) }
    }

    internal fun ownerDied(player: Player) {
        foes.entries.filter { it.value.owner == player.uid }.forEach { despawn(it.key) }
    }

    companion object {
        const val TICK_CYCLES = 5
        private const val LEASH_DISTANCE = 15
        private const val IDLE_CYCLES = 500
        private const val MAX_LIFESPAN_CYCLES = 3000
    }
}

class QuestFoeAttackHook @Inject constructor(private val foes: QuestFoes) : NpcAttackValidateHook {
    override fun validate(player: Player, npc: Npc): NpcAttackValidateResult {
        if (!foes.isFoe(npc)) {
            return NpcAttackValidateResult.Pass
        }
        if (npc.isSpawnOwnedByOther(player)) {
            return NpcAttackValidateResult.Deny("It's not after you.")
        }
        return NpcAttackValidateResult.BypassSingleWayPvnRestriction
    }
}

class QuestFoeKillHook @Inject constructor(private val foes: QuestFoes) : NpcDeathKillHook {
    override fun onKill(context: NpcDeathKillContext) {
        foes.killed(context.npc, context.hero)
    }
}

class QuestFoeDeathCleanup @Inject constructor(private val foes: QuestFoes) : PlayerDeathCleanupHook {
    override fun cleanup(player: Player) {
        foes.ownerDied(player)
    }
}
