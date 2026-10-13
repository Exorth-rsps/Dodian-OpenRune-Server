package org.rsmod.content.dodian

import jakarta.inject.Inject
import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.script.onCommand
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.server.config.ServerConfig
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class DodianHomeScript
@Inject
constructor(private val config: ServerConfig, private val protectedAccess: ProtectedAccessLauncher) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1("loc.group_storage_yanille") {
            mes("Welcome to Dodian! This chest is handled by content/dodian.")
            telejump(HOME, TeleportType.Exempt)
        }

        onCommand("dodian") {
            desc = "Show the active gamemode, or ::dodian home to teleport home"
            cheat {
                player.mes("Gamemode: ${config.gamemode.name.lowercase()}")
                if (args.firstOrNull() == "home") {
                    protectedAccess.launch(player) { telejump(HOME, TeleportType.Exempt) }
                }
            }
        }
    }

    private companion object {
        val HOME = CoordGrid(2612, 3092, 0)
    }
}
