package org.rsmod.content.dodian.npcs

import jakarta.inject.Inject
import java.util.concurrent.CopyOnWriteArrayList
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.shops.Shops
import org.rsmod.content.skills.runecrafting.RunecraftingGates
import org.rsmod.content.skills.runecrafting.essence.teleportToRuneEssenceMine
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** A quest conversation that takes over Aubury's talk-to; returns true when it handled it. */
fun interface AuburyTalk {
    suspend fun Dialogue.talk(): Boolean
}

object AuburyTalks {
    private val talks = CopyOnWriteArrayList<AuburyTalk>()

    fun register(talk: AuburyTalk) {
        talks += talk
    }

    suspend fun Dialogue.handled(): Boolean = talks.any { talk -> with(talk) { talk() } }
}

class Aubury @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        for (aubury in AUBURY) {
            onOpNpc1(aubury) { startDialogue(it.npc) { talk(it.npc) } }
            onOpNpc3(aubury) { player.openShop(it.npc) }
            onOpNpc4(aubury) { teleport(it.npc) }
        }
    }

    private suspend fun Dialogue.talk(npc: Npc) {
        if (with(AuburyTalks) { handled() }) {
            return
        }
        chatNpc(quiz, "Do you want to buy some runes?")
        val canTeleport = RunecraftingGates.refusal(player) == null
        val choice =
            if (canTeleport) {
                choice3(
                    "Yes please!",
                    1,
                    "No thank you.",
                    2,
                    "Can you teleport me to the Rune Essence?",
                    3,
                )
            } else {
                choice2("Yes please!", 1, "Oh, it's a rune shop. No thank you, then.", 2)
            }
        when (choice) {
            1 -> {
                chatPlayer(happy, "Yes please!")
                player.openShop(npc)
            }
            2 -> {
                chatPlayer(
                    bored,
                    if (canTeleport) "No thank you." else "Oh, it's a rune shop. No thank you, then.",
                )
                chatNpc(sad, "Well, if you find someone who does want runes, please send them my way.")
            }
            3 -> {
                chatPlayer(quiz, "Can you teleport me to the Rune Essence?")
                chatNpc(
                    happy,
                    "Of course. By the way, if you end up making any runes from the essence you " +
                        "mine, I'll happily buy them from you.",
                )
                teleportToRuneEssenceMine()
            }
        }
    }

    private suspend fun ProtectedAccess.teleport(npc: Npc) {
        if (RunecraftingGates.refusal(player) == null) {
            teleportToRuneEssenceMine(npc)
            return
        }
        startDialogue(npc) {
            chatNpc(
                neutral,
                "Teleport you to the Rune Essence? Whatever for? You wouldn't know what to do with " +
                    "it. Wizard Sinina upstairs has been digging through the guild vault, perhaps " +
                    "she can teach you something.",
            )
        }
    }

    private fun Player.openShop(npc: Npc) {
        shops.open(this, npc, "Aubury's Rune Shop.", RUNE_SHOP)
    }

    companion object {
        val AUBURY = listOf("npc.aubury", "npc.aubury_2op", "npc.aubury_3op")

        private const val RUNE_SHOP = "inv.dodian_aubury_runeshop"
    }
}
