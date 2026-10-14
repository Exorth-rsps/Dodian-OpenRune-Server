package org.rsmod.content.dodian.quest.slayerseye

import org.rsmod.api.death.NpcDeathKillHook
import org.rsmod.plugin.module.PluginModule

class SlayersEyeModule : PluginModule() {
    override fun bind() {
        addSetBinding<NpcDeathKillHook>(SlayersEyeDrops::class.java)
    }
}
