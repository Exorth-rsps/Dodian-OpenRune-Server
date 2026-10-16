package org.rsmod.content.quest.manager

import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.api.table.QuestRow
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private const val MINIQUEST_TYPE = 1

/**
 * Sends the quest tab filter ([QuestClientSync]) on login and lowers the quest point / completion
 * totals to match, so the quest tab only counts quests that can actually be played.
 */
class QuestVisibilityScript : PluginScript() {
    private var Player.questPointsMax by intVarBit("varbit.qp_max")
    private var Player.questsTotalCount by intVarBit("varbit.quests_total_count")

    private val totalQuestPoints: Int by lazy { Quest.playable().sumOf { it.questPoints } }

    private val totalQuests: Int by lazy {
        Quest.playable().count { QuestRow.getRow(it.rowID).type != MINIQUEST_TYPE }
    }

    override fun ScriptContext.startup() {
        onPlayerLogin {
            QuestClientSync.sync(player)
            player.questPointsMax = totalQuestPoints
            player.questsTotalCount = totalQuests
        }
    }
}
