package org.rsmod.content.dodian.quest.common

import org.rsmod.api.death.NpcAttackValidateHook
import org.rsmod.api.death.NpcDeathKillHook
import org.rsmod.api.death.PlayerDeathCleanupHook
import org.rsmod.plugin.module.PluginModule

class QuestFoesModule : PluginModule() {
    override fun bind() {
        addSetBinding<NpcAttackValidateHook>(QuestFoeAttackHook::class.java)
        addSetBinding<NpcDeathKillHook>(QuestFoeKillHook::class.java)
        addSetBinding<PlayerDeathCleanupHook>(QuestFoeDeathCleanup::class.java)
    }
}
