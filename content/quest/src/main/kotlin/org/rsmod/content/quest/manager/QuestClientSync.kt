package org.rsmod.content.quest.manager

import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import org.rsmod.api.player.output.runClientScript
import org.rsmod.game.entity.Player

/**
 * Sends the quest tab filter to the client: `,<id>,` for every quest with a server-side
 * implementation, plus `,s<id>,` / `,c<id>,` for the ones the player has started or completed.
 *
 * The overridden `[proc,questlist_hide_quest]` hides rows whose id is missing, and the overridden
 * `[proc,quest_progress_get]` reads the `s`/`c` markers for quests without a client-side varp (the
 * native quests return their own varp before reaching them). An empty string means "no filter" to
 * the client, so with no quests a lone separator is sent to hide every row.
 */
object QuestClientSync {
    private const val NO_QUESTS = ","

    fun sync(player: Player) {
        val script = "clientscript.quest_available_set".asRSCM(RSCMType.CLIENTSCRIPT)
        player.runClientScript(script, filter(player))
        redrawQuestList(player)
    }

    private fun filter(player: Player): String {
        val quests = Quest.all().sortedBy { it.id }
        if (quests.isEmpty()) {
            return NO_QUESTS
        }
        val entries = mutableListOf<String>()
        for (quest in quests) {
            entries += quest.id.toString()
            when (quest.questState(player)) {
                QuestProgressState.IN_PROGRESS -> entries += "s${quest.id}"
                QuestProgressState.FINISHED -> entries += "c${quest.id}"
                QuestProgressState.NOT_STARTED -> {}
            }
        }
        return entries.joinToString(",", ",", ",")
    }

    private fun redrawQuestList(player: Player) {
        if (!player.ui.containsOverlay("interface.questlist")) {
            return
        }
        player.runClientScript(
            "clientscript.[clientscript,questlist_draw]".asRSCM(RSCMType.CLIENTSCRIPT),
            RSCM.getRSCM("component.questlist:container"),
            RSCM.getRSCM("component.questlist:list_container"),
            RSCM.getRSCM("component.questlist:text_container"),
            RSCM.getRSCM("component.questlist:scrollbar"),
            RSCM.getRSCM("component.questlist:list"),
            0,
        )
    }
}
