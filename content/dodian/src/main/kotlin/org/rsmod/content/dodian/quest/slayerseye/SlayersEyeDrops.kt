package org.rsmod.content.dodian.quest.slayerseye

import jakarta.inject.Inject
import org.rsmod.api.config.constants
import org.rsmod.api.death.NpcDeathKillContext
import org.rsmod.api.death.NpcDeathKillHook
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.content.dodian.quest.slayerseye.TheSlayersEye.Companion.BONE_SHARD
import org.rsmod.content.dodian.quest.slayerseye.TheSlayersEye.Companion.EYE_OF_NEWT
import org.rsmod.content.dodian.quest.slayerseye.TheSlayersEye.Companion.STAGE_GEM_MADE
import org.rsmod.content.dodian.quest.slayerseye.TheSlayersEye.Companion.STAGE_TUREAL_SPOKEN
import org.rsmod.content.quest.manager.Quest
import org.rsmod.game.entity.Player

/** Bone shards from skeletons and eyes of newt from moss giants, only for the killer. */
class SlayersEyeDrops
@Inject
constructor(private val objRepo: ObjRepository, private val random: GameRandom) :
    NpcDeathKillHook {

    override fun onKill(context: NpcDeathKillContext) {
        val item = DROPS[context.npc.name.lowercase()] ?: return
        val quest = Quest.get(TheSlayersEye.QUEST_KEY) ?: return
        val player = context.hero
        if (quest.getQuestStage(player) !in STAGE_TUREAL_SPOKEN..STAGE_GEM_MADE) {
            return
        }
        if (player.owns(item) || !random.randomBoolean(DROP_CHANCE)) {
            return
        }
        val duration = player.lootDropDuration ?: constants.lootdrop_duration
        objRepo.add(item, context.dropCoords, duration, receiver = player)
    }

    private fun Player.owns(item: String): Boolean =
        item in inv || item in invMap.getOrPut("inv.bank")

    private companion object {
        /** One in this many kills drops the item. */
        const val DROP_CHANCE = 5

        val DROPS = mapOf("skeleton" to BONE_SHARD, "moss giant" to EYE_OF_NEWT)
    }
}
