package org.rsmod.content.dodian

import jakarta.inject.Inject
import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.script.onCommand
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onPlayerInit
import org.rsmod.api.server.config.ServerConfig
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import org.rsmod.routefinder.collision.CollisionFlagMap

class DodianHomeScript
@Inject
constructor(
    private val config: ServerConfig,
    private val protectedAccess: ProtectedAccessLauncher,
    private val collision: CollisionFlagMap,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onPlayerInit {
            if (!collision.isZoneAllocated(player.coords.x, player.coords.z, 0)) {
                player.coords = START
            }
        }

        onOpLoc1("loc.group_storage_yanille") {
            mes("Welcome to Dodian! This chest is handled by content/dodian.")
            telejump(START, TeleportType.Exempt)
        }

        onCommand("dodian") {
            desc = "Show the active gamemode, or ::dodian home to teleport home"
            cheat {
                player.mes("Gamemode: ${config.gamemode.name.lowercase()}")
                if (args.firstOrNull() == "home") {
                    protectedAccess.launch(player) { telejump(START, TeleportType.Exempt) }
                }
            }
        }
    }

    private companion object {
        val START = CoordGrid(2670, 3231, 0)
    }
}
