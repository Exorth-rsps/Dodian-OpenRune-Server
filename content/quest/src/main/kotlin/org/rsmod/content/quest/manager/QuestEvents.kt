package org.rsmod.content.quest.manager

import dev.openrune.definition.type.widget.IfEvent
import org.rsmod.api.player.ui.ifSetEvents
import org.rsmod.api.script.onIfOpen
import org.rsmod.api.table.QuestRow
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class QuestEvents : PluginScript() {

    private var questCount: Int = 0

    override fun ScriptContext.startup() {
        questCount = QuestRow.all().size

        onIfOpen("interface.questjournal_overview") {
            player.ifSetEvents(
                "component.questjournal_overview:content_inner",
                0..23,
                IfEvent.Op1,
                IfEvent.Op2,
                IfEvent.Op3,
                IfEvent.Op4,
            )
        }

        onIfOpen("interface.questlist") {
            player.ifSetEvents(
                "component.questlist:list",
                0..questCount,
                IfEvent.Op1,
                IfEvent.Op2,
                IfEvent.Op3,
                IfEvent.Op4,
            )
        }
    }
}
