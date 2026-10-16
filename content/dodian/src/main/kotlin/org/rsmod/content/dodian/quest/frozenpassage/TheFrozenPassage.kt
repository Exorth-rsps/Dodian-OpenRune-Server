package org.rsmod.content.dodian.quest.frozenpassage

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.dodian.isOnDoorTileSide
import org.rsmod.content.dodian.quest.common.DodianQuestLocs
import org.rsmod.content.dodian.swingThrough
import org.rsmod.content.generic.locs.doors.DoorTranslations
import org.rsmod.content.quest.manager.ItemRewardDisplay
import org.rsmod.content.quest.manager.QuestJournalBuilder
import org.rsmod.content.quest.manager.QuestScript
import org.rsmod.content.quest.manager.rewards
import org.rsmod.game.entity.Player
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.plugin.scripts.ScriptContext

class TheFrozenPassage @Inject constructor(private val locRepo: LocRepository) :
    QuestScript(
        QUEST_KEY,
        "varp.dodian_frozen_passage",
        rewards {
            xp("stat.mining", MINING_XP)
            xp("stat.smithing", SMITHING_XP)
            extra("Access to the Ice Mountain tunnel")
        },
        ItemRewardDisplay(BRONZE_BAR, zoom = 400),
        questVarbit = "varbit.dodian_frozen_passage_progress",
    ) {

    private var Player.clayGiven by boolVarBit("varbit.dodian_frozen_passage_clay_given")
    private var Player.barsGiven by boolVarBit("varbit.dodian_frozen_passage_bars_given")
    private var Player.nailsGiven by boolVarBit("varbit.dodian_frozen_passage_nails_given")

    override fun ScriptContext.init() {
        check(quest.maxSteps == STAGE_COMPLETE) {
            "The Frozen Passage end state is ${quest.maxSteps} in .data/dodian/quests.toml, but " +
                "the quest completes at stage $STAGE_COMPLETE."
        }
        onOpNpc1(DORIC) { startDialogue(it.npc) { doric() } }
        for (dwarf in TUNNEL_DWARVES) {
            onOpNpc1(dwarf) { startDialogue(it.npc) { tunnelDwarf() } }
        }
        for (entrance in DodianQuestLocs.locs("frozen_passage_tunnel_entrance")) {
            onOpLoc1(entrance) { enterTunnel(it.vis) }
        }
        for (support in DodianQuestLocs.locs("frozen_passage_deepest_support")) {
            onOpLoc1(support) { inspectSupport() }
        }
    }

    override fun subTitle(): String =
        "speaking to <col=800000>Doric</col> at the tunnel entrance under the " +
            "<col=800000>Ice Mountain</col>."

    override fun questLog(player: ProtectedAccess): String =
        questJournal(player) {
            val p = player.player
            when (quest.getQuestStage(p)) {
                STAGE_STARTED -> {
                    line("The frost has cracked the supports in the tunnel. Doric needs:")
                    material(p.clayGiven, "<red>4 clay</red>")
                    material(p.barsGiven, "<red>2 bronze bars</red>")
                    material(p.nailsGiven, "<red>15 bronze nails</red>")
                }
                STAGE_FIXED ->
                    line(
                        "Doric fixed the front supports. I should <red>inspect the deepest " +
                            "support</red> at the far end of the tunnel."
                    )
                else -> line("The deepest support is holding. I should tell <red>Doric</red>.")
            }
        }

    override fun completedLog(player: ProtectedAccess): String =
        completionJournal(player) {
            line(
                "The tunnel under the Ice Mountain is open, and Doric is a little less afraid of " +
                    "the dark."
            )
        }

    private fun QuestJournalBuilder.material(given: Boolean, text: String) {
        if (given) strike(text.replace(HIGHLIGHT_TAGS, "")) else line(text)
    }

    private suspend fun ProtectedAccess.enterTunnel(entrance: BoundLocInfo) {
        if (quest.getQuestStage(player) < STAGE_FIXED) {
            startDialogue {
                chatNpcSpecific(
                    "Doric",
                    DORIC,
                    angry,
                    "Oi! Nobody goes in there until those supports are fixed. Talk to me first.",
                )
            }
            return
        }
        val destination = DodianQuestLocs.coord("frozen_passage_tunnel_destination")
        if (destination != null) {
            telejump(destination, TeleportType.Exempt)
            return
        }
        if (entrance.shapeId !in WALL_SHAPES) {
            mes("The tunnel beyond is still being cleared out. Doric will let you know when it's ready.")
            return
        }
        val otherSide = DoorTranslations.translateOpen(entrance.coords, entrance.shape, entrance.angle)
        swingThrough(locRepo, entrance, if (entrance.isOnDoorTileSide(coords)) otherSide else entrance.coords)
    }

    private suspend fun ProtectedAccess.inspectSupport() {
        if (quest.getQuestStage(player) != STAGE_FIXED) {
            mesbox("A sturdy wooden support. It looks like it'll hold for another hundred years.")
            return
        }
        mesbox(
            "You inspect the support. The wood is old but sturdy, and the ice around it has been " +
                "sealed with fresh clay."
        )
        mesbox("It's holding. You should tell Doric.")
        quest.setQuestStage(this, STAGE_INSPECTED)
    }

    private suspend fun Dialogue.tunnelDwarf() {
        when (quest.getQuestStage(player)) {
            STAGE_NOT_STARTED ->
                chatNpc(
                    angry,
                    "Tunnel's closed. The frost cracked the supports, and Doric won't let anyone " +
                        "through until they're fixed. Go and talk to him if you want to help.",
                )
            STAGE_STARTED ->
                chatNpc(
                    neutral,
                    "Still waiting on those materials for Doric? Clay, bronze bars and nails. The " +
                        "sooner he gets them, the sooner we can all go home.",
                )
            STAGE_FIXED,
            STAGE_INSPECTED ->
                chatNpc(
                    neutral,
                    "Doric says you're checking the deepest support. Mind your head in there, the " +
                        "ceiling's lower than it looks.",
                )
            else ->
                chatNpc(
                    happy,
                    "Welcome, friend! Thanks to you the tunnel's safe again. Even Doric goes in " +
                        "now. Ten steps at a time.",
                )
        }
    }

    private suspend fun Dialogue.doric() {
        when (quest.getQuestStage(player)) {
            STAGE_NOT_STARTED -> doricOffer()
            STAGE_STARTED -> doricMaterials()
            STAGE_FIXED ->
                chatNpc(
                    neutral,
                    "Have you checked the deepest support yet? It's right at the far end of the " +
                        "tunnel. My knee and I will wait here.",
                )
            STAGE_INSPECTED -> doricComplete()
            else -> chatNpc(happy, "I went in yesterday. Ten whole steps! Tomorrow I'm going for twenty.")
        }
    }

    private suspend fun Dialogue.doricOffer() {
        chatNpc(angry, "Hold it right there! Where do you think you're going?")
        chatPlayer(confused, "Into the tunnel?")
        chatNpc(neutral, "Not today you're not. Not any day, until those supports are fixed.")
        chatNpc(
            worried,
            "This winter the frost crept so deep into the mountain that the old wooden supports " +
                "cracked. The ice is pushing the rocks apart. One wrong step and the whole " +
                "ceiling comes down.",
        )
        chatPlayer(quiz, "Can it be repaired?")
        chatNpc(
            neutral,
            "Of course it can. I'm a dwarf. But I'm a dwarf with a bad knee and no materials.",
        )
        chatNpc(neutral, "Bring me what I need, and I'll fix it myself.")
        val accept = choice2("What do you need?", true, "I'll find another way around.", false)
        if (!accept) {
            chatPlayer(neutral, "I'll find another way around.")
            chatNpc(laugh, "Good luck with that. It's a mountain. There isn't another way around.")
            return
        }
        chatPlayer(quiz, "What do you need?")
        chatNpc(
            neutral,
            "Four lumps of clay for the mortar. That'll seal the cracks so the ice can't get back in.",
        )
        chatNpc(
            neutral,
            "Two bronze bars for new brackets, and fifteen bronze nails to fix them to the beams.",
        )
        chatNpc(
            neutral,
            "You'll find copper, tin and clay rocks around here. Smelt the ore in a furnace, then " +
                "hammer one bar into nails on an anvil. Don't forget a hammer.",
        )
        chatNpc(
            neutral,
            "Bring it all in at once or bit by bit, I don't mind. Just bring the full amount of each.",
        )
        quest.setQuestStage(access, STAGE_STARTED)
    }

    private suspend fun Dialogue.doricMaterials() {
        val handed = mutableListOf<String>()
        var tooFew = false
        if (!player.clayGiven) {
            when (hand(CLAY, CLAY_NEEDED)) {
                Hand.Given -> {
                    player.clayGiven = true
                    handed += "Four lumps of clay. Nice and sticky."
                }
                Hand.TooFew -> tooFew = true
                Hand.None -> {}
            }
        }
        if (!player.barsGiven) {
            when (hand(BRONZE_BAR, BARS_NEEDED)) {
                Hand.Given -> {
                    player.barsGiven = true
                    handed += "Two bronze bars. Good, solid work."
                }
                Hand.TooFew -> tooFew = true
                Hand.None -> {}
            }
        }
        if (!player.nailsGiven) {
            when (hand(BRONZE_NAILS, NAILS_NEEDED)) {
                Hand.Given -> {
                    player.nailsGiven = true
                    handed += "Fifteen nails, and not a bent one among them. You've got a smith's hands."
                }
                Hand.TooFew -> tooFew = true
                Hand.None -> {}
            }
        }
        if (handed.isNotEmpty()) {
            chatNpc(happy, "Ah, materials! Let's see what you've got.")
            for (line in handed) {
                chatNpc(happy, line)
            }
        }
        if (player.clayGiven && player.barsGiven && player.nailsGiven) {
            doricRepairs()
            return
        }
        if (tooFew) {
            chatNpc(neutral, "That's not enough. I need the full amount, or the repair won't hold.")
        }
        chatNpc(neutral, "I still need:")
        if (!player.clayGiven) {
            chatNpc(neutral, "Four lumps of clay.")
        }
        if (!player.barsGiven) {
            chatNpc(neutral, "Two bronze bars.")
        }
        if (!player.nailsGiven) {
            chatNpc(neutral, "Fifteen bronze nails. Hammer a bronze bar on an anvil.")
        }
    }

    private fun Dialogue.hand(item: String, needed: Int): Hand {
        val carried = access.invTotal(access.inv, item)
        return when {
            carried <= 0 -> Hand.None
            carried < needed -> Hand.TooFew
            access.invDel(access.inv, item, needed).success -> Hand.Given
            else -> Hand.None
        }
    }

    private suspend fun Dialogue.doricRepairs() {
        chatNpc(happy, "That's everything! Right, give me a moment...")
        mesbox(
            "Doric disappears just inside the tunnel entrance. You hear hammering, grumbling and a " +
                "lot of dwarvish swearing."
        )
        chatNpc(happy, "There. The front supports are as good as new.")
        chatNpc(
            neutral,
            "Now, there's one more support, right at the deepest part of the tunnel. Somebody " +
                "needs to check it.",
        )
        chatPlayer(quiz, "Why don't you check it?")
        chatNpc(
            worried,
            "Me? Er... my knee. Yes. My knee is bad. Very bad. You go. Just inspect the deepest " +
                "support and tell me if it's holding.",
        )
        quest.setQuestStage(access, STAGE_FIXED)
    }

    private suspend fun Dialogue.doricComplete() {
        chatPlayer(happy, "The deepest support is holding. The whole tunnel is safe.")
        chatNpc(shocked, "It is? ...Really safe? No cracks, no ice?")
        chatPlayer(quiz, "Not a single crack. Is something wrong?")
        chatNpc(sad, "...Fine. I'll tell you the truth. There's nothing wrong with my knee.")
        chatNpc(
            sad,
            "When I was a young dwarf, I got lost in that tunnel. A whole night, alone in the " +
                "dark, listening to the ice creak. I haven't been past the entrance since.",
        )
        chatNpc(
            neutral,
            "But if you went all the way to the end and came back in one piece... then maybe it's " +
                "time I stopped being afraid of it.",
        )
        chatNpc(
            happy,
            "Thank you, friend. The tunnel is open to you. And maybe, one day, to me too.",
        )
        quest.completeQuest(access)
    }

    private enum class Hand {
        None,
        TooFew,
        Given,
    }

    companion object {
        const val QUEST_KEY = "quest_frozen_passage"

        const val STAGE_NOT_STARTED = 0
        const val STAGE_STARTED = 1
        const val STAGE_FIXED = 2
        const val STAGE_INSPECTED = 3
        const val STAGE_COMPLETE = 4

        const val MINING_XP = 1000.0
        const val SMITHING_XP = 1000.0

        private const val CLAY_NEEDED = 4
        private val WALL_SHAPES = 0..3
        private const val BARS_NEEDED = 2
        private const val NAILS_NEEDED = 15

        private const val DORIC = "npc.doric"
        private val TUNNEL_DWARVES = listOf("npc.mguild_guard1", "npc.mguild_guard2")

        private const val CLAY = "obj.clay"
        private const val BRONZE_BAR = "obj.bronze_bar"
        private const val BRONZE_NAILS = "obj.nails_bronze"

        private val HIGHLIGHT_TAGS = Regex("</?red>")
    }
}
