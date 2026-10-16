package org.rsmod.content.dodian

import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.annotations.InternalApi
import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.script.onCommand
import org.rsmod.api.script.onEvent
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.server.config.ServerConfig
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.player.SessionStateEvent
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import org.rsmod.routefinder.collision.CollisionFlagMap

private var Player.playMode by intVarp("varp.musicplay")
private val Player.areaMode by intVarBit("varbit.music_area_mode")
private val Player.masterVolume by intVarBit("varbit.option_master_volume_desktop")
private val Player.musicVolume by intVarBit("varbit.option_music_desktop")
private val Player.currMusicId by intVarBit("varbit.music_curr_id")
private val Player.currMusicArea by intVarBit("varbit.music_curr_area")
private val Player.musicClocks by intVarBit("varbit.music_curr_clocks")
private val Player.musicDuration by intVarBit("varbit.music_curr_duration")

class DodianHomeScript
@Inject
constructor(
    private val config: ServerConfig,
    private val protectedAccess: ProtectedAccessLauncher,
    private val collision: CollisionFlagMap,
) : PluginScript() {
    override fun ScriptContext.startup() {
        // Before the login rebuild is queued, or the client builds the scene at the old coords.
        onEvent<SessionStateEvent.PrepareLogin> {
            if (!collision.isZoneAllocated(player.coords.x, player.coords.z, 0)) {
                player.coords = START
            }
        }

        onOpLoc1("loc.group_storage_yanille") {
            mes("Welcome to Dodian! This chest is handled by content/dodian.")
            telejump(START, TeleportType.Exempt)
        }

        onCommand("dodian") {
            desc = "Show the active gamemode"
            cheat { player.mes("Gamemode: ${config.gamemode.name.lowercase()}") }
        }

        onCommand("dodianhome") {
            desc = "Teleport to the Dodian start location"
            cheat { protectedAccess.launch(player) { telejump(START, TeleportType.Exempt) } }
        }

        onCommand("dodianmusic") {
            desc = "Show the server-side music state"
            cheat { player.showMusicState() }
        }
    }

    @OptIn(InternalApi::class)
    private fun Player.showMusicState() {
        val timer = "timer.music_sync".asRSCM(RSCMType.TIMER).toShort()
        val area = currMusicArea.takeIf { it > 0 }?.let { RSCM.getReverseMapping(RSCMType.AREA, it - 1) }
        mes("Volume: master=$masterVolume music=$musicVolume (0 = muted, music timer then idles)")
        mes("Play mode=$playMode area mode=$areaMode sync timer=${softTimerMap[timer] != null}")
        mes("Track id=$currMusicId clocks=$musicClocks/$musicDuration area=$area")
    }

    internal companion object {
        val START = CoordGrid(2670, 3231, 0)
    }
}
