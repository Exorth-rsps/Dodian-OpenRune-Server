package org.rsmod.content.dodian

import org.rsmod.api.death.PlayerRespawnHook
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.module.PluginModule

/** The default respawn point (Lumbridge) is not on the Dodian maps; players respawn at home. */
class DodianRespawnHook : PlayerRespawnHook {
    override fun respawnCoords(player: Player): CoordGrid = DodianHomeScript.START
}

class DodianRespawnModule : PluginModule() {
    override fun bind() {
        addSetBinding<PlayerRespawnHook>(DodianRespawnHook::class.java)
    }
}
