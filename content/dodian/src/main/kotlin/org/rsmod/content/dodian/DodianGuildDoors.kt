package org.rsmod.content.dodian

import jakarta.inject.Inject
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.craftingLvl
import org.rsmod.api.player.stat.fishingLvl
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.generic.locs.doors.DoorTranslations
import org.rsmod.game.entity.Player
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Guild doors on the Dodian maps that only ask for a skill level; leaving is always allowed. */
class DodianGuildDoors @Inject constructor(private val locRepo: LocRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        for (guild in Guild.entries) {
            onOpLoc1(guild.door) { enter(it.vis, guild) }
        }
    }

    private suspend fun ProtectedAccess.enter(door: BoundLocInfo, guild: Guild) {
        val otherSide = DoorTranslations.translateOpen(door.coords, door.shape, door.angle)
        val inside = if (guild.insideOnDoorTile) door.coords else otherSide
        val outside = if (guild.insideOnDoorTile) otherSide else door.coords
        val entering = door.isOnDoorTileSide(coords) != guild.insideOnDoorTile
        if (entering && guild.level(player) < guild.required) {
            startDialogue {
                chatNpcSpecific(guild.masterTitle, guild.master, neutral, guild.refusal)
            }
            return
        }
        swingThrough(locRepo, door, if (entering) inside else outside)
        val welcome = guild.welcome ?: return
        if (entering) {
            startDialogue { chatNpcSpecific(guild.masterTitle, guild.master, happy, welcome) }
        }
    }

    private enum class Guild(
        val door: String,
        val insideOnDoorTile: Boolean,
        val required: Int,
        val level: (Player) -> Int,
        val master: String,
        val masterTitle: String,
        val refusal: String,
        val welcome: String?,
    ) {
        Crafting(
            door = "loc.craftingguilddoor",
            insideOnDoorTile = false,
            required = 40,
            level = { it.craftingLvl },
            master = "npc.master_crafter",
            masterTitle = "Master Crafter",
            refusal =
                "Sorry, only experienced crafters are allowed in here. You must be level 40 or " +
                    "above to enter.",
            welcome = "Welcome to the Guild of Master Craftsmen.",
        ),
        Fishing(
            door = "loc.fishguilddoor",
            insideOnDoorTile = true,
            required = 68,
            level = { it.fishingLvl },
            master = "npc.master_fisher",
            masterTitle = "Master fisher",
            refusal =
                "Hello, I'm afraid only the top fishers are allowed to use our premier fishing " +
                    "facilities. You need a Fishing level of 68 to enter.",
            welcome = null,
        ),
    }
}
