package org.rsmod.content.dodian

import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Dodian quests are not split into free and members' quests, so the quest tab drops the headers. */
class DodianQuestTabScript : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerLogin { VarPlayerIntMapSetter.set(player, "varbit.questlist_disable_headers", 1) }
    }
}
