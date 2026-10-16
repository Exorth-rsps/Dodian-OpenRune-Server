package org.rsmod.content.dodian.quest.lostcircle

import dev.openrune.rscm.RSCM.asRSCM
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.dodian.quest.common.DodianQuestLocs
import org.rsmod.content.dodian.quest.common.carries
import org.rsmod.content.quest.manager.ItemRewardDisplay
import org.rsmod.content.quest.manager.QuestScript
import org.rsmod.content.quest.manager.rewards
import org.rsmod.content.skills.herblore.HerbloreGates
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.ScriptContext

class TheLostCircle :
    QuestScript(
        QUEST_KEY,
        "varp.dodian_lost_circle",
        rewards {
            xp("stat.herblore", HERBLORE_XP)
            extra("Access to Herblore")
        },
        ItemRewardDisplay(GUAM_LEAF, zoom = 400),
        questVarbit = "varbit.dodian_lost_circle_progress",
    ) {

    override fun ScriptContext.init() {
        check(quest.maxSteps == STAGE_COMPLETE) {
            "The Lost Circle end state is ${quest.maxSteps} in .data/dodian/quests.toml, but the " +
                "quest completes at stage $STAGE_COMPLETE."
        }
        HerbloreGates.register { player ->
            if (quest.isQuestCompleted(player)) {
                null
            } else {
                "You need to complete The Lost Circle to use the Herblore skill."
            }
        }
        onOpNpc1(KAQEMEEX) { startDialogue(it.npc) { kaqemeex() } }
        onOpNpc1(SANFEW) { startDialogue(it.npc) { sanfew() } }
        for (cauldron in DodianQuestLocs.locs("lost_circle_cauldron")) {
            onOpLocU(cauldron) { dipInCauldron(it.objType.internalName) }
        }
    }

    override fun subTitle(): String =
        "speaking to <col=800000>Kaqemeex</col> in <col=800000>Taverley</col>."

    override fun questLog(player: ProtectedAccess): String =
        questJournal(player) {
            val stage = quest.getQuestStage(player.player)
            for (step in STAGE_STARTED until stage) {
                strike(JOURNAL[step].replace(HIGHLIGHT_TAGS, ""))
            }
            line(JOURNAL[stage])
        }

    override fun completedLog(player: ProtectedAccess): String =
        completionJournal(player) {
            for (step in STAGE_STARTED until STAGE_COMPLETE) {
                line(JOURNAL[step].replace(HIGHLIGHT_TAGS, ""))
            }
            line("Kaqemeex has taught me the art of Herblore.")
        }

    private fun stage(player: Player): Int = quest.getQuestStage(player)

    private suspend fun ProtectedAccess.dipInCauldron(item: String) {
        if (stage(player) != STAGE_GATHERING) {
            mes("The cauldron bubbles quietly. You don't think you should put anything in it.")
            return
        }
        val meat = MEATS.firstOrNull { it.raw == item }
        if (meat == null) {
            mes("Nothing interesting happens.")
            return
        }
        val swapped =
            player.invTransaction(inv) {
                val inventory = select(inv)
                delete {
                    from = inventory
                    obj = meat.raw.asRSCM()
                    strictCount = 1
                }
                insert {
                    into = inventory
                    obj = meat.enchanted.asRSCM()
                    strictCount = 1
                }
            }.success
        if (swapped) {
            mes("You dip the ${meat.name} into the cauldron. It crackles with a faint green light.")
        }
    }

    private suspend fun Dialogue.kaqemeex() {
        when (stage(player)) {
            STAGE_NOT_STARTED -> kaqemeexOffer()
            STAGE_STARTED,
            STAGE_GATHERING ->
                chatNpc(
                    neutral,
                    "Have you spoken to Sanfew? He lives here in Taverley, on the upper floor of " +
                        "his house. Only he knows the old ritual.",
                )
            STAGE_RITUAL_DONE -> kaqemeexComplete()
            else ->
                chatNpc(
                    happy,
                    "Welcome back, herbalist. The herbs of Taverley are stronger than they have " +
                        "been in years.",
                )
        }
    }

    private suspend fun Dialogue.kaqemeexOffer() {
        chatNpc(neutral, "Greetings, traveller. You stand where the great circle once stood.")
        chatPlayer(confused, "Circle? I don't see anything here.")
        chatNpc(
            sad,
            "No. And that is the tragedy. For centuries, the stones of Taverley stood here. We " +
                "druids gathered among them to feel the power of Guthix flowing through the earth.",
        )
        chatNpc(
            sad,
            "Then, one by one, the stones cracked. They crumbled to dust, and the wind carried " +
                "them away. Nobody knows why.",
        )
        chatNpc(
            sad,
            "Without the circle, the balance is broken. Our herbs have lost their strength. Our " +
                "potions are little more than coloured water.",
        )
        chatPlayer(quiz, "Is there anything that can be done?")
        chatNpc(
            neutral,
            "Perhaps. My friend Sanfew has been searching through the oldest scrolls of our " +
                "order. He speaks of a ritual from the time before the stones.",
        )
        chatNpc(
            quiz,
            "Will you help him? I am too old to run errands across the land, and the young druids " +
                "have lost hope.",
        )
        val accept = choice2("I'll help the druids.", true, "Not right now.", false)
        if (!accept) {
            chatPlayer(neutral, "Not right now.")
            chatNpc(
                neutral,
                "Balance cannot be rushed. But do not wait too long. Every day without the circle, " +
                    "the herbs grow weaker.",
            )
            return
        }
        chatPlayer(happy, "I'll help the druids.")
        chatNpc(
            happy,
            "Guthix smiles on you. Sanfew lives here in Taverley, on the upper floor of his house. " +
                "Tell him I sent you.",
        )
        quest.setQuestStage(access, STAGE_STARTED)
    }

    private suspend fun Dialogue.kaqemeexComplete() {
        chatNpc(shocked, "I felt it. Just now, beneath my feet... the earth is warm again.")
        chatPlayer(happy, "Sanfew performed the ritual. It worked.")
        chatNpc(
            neutral,
            "It did more than work. We always believed the power lived in the stones. But the " +
                "stones were only a reminder. The power was in the earth all along.",
        )
        chatNpc(
            happy,
            "You have restored the balance, and that deserves a reward that cannot be bought. I " +
                "will teach you the art of Herblore.",
        )
        chatNpc(
            neutral,
            "Herbs that grow in the wild are covered in dirt and must be cleaned before use. Place " +
                "a clean herb in a vial of water to make an unfinished potion.",
        )
        chatNpc(
            neutral,
            "Then add a secondary ingredient. An eye of newt, for example, with a guam leaf makes " +
                "an attack potion.",
        )
        chatNpc(
            happy,
            "The more you learn, the more powerful the potions you'll be able to brew. Go now, " +
                "herbalist. The earth is listening again.",
        )
        quest.completeQuest(access)
    }

    private suspend fun Dialogue.sanfew() {
        when (stage(player)) {
            STAGE_NOT_STARTED ->
                chatNpc(
                    neutral,
                    "Hmm? Oh, I'm sorry, I'm terribly busy with these scrolls. Perhaps another time.",
                )
            STAGE_STARTED -> sanfewExplains()
            STAGE_GATHERING -> sanfewOffering()
            STAGE_RITUAL_DONE ->
                chatNpc(
                    neutral,
                    "The ritual is complete. Go and see Kaqemeex, he's waiting for you.",
                )
            else ->
                chatNpc(
                    happy,
                    "The young druids have started sitting where the circle once stood again. No " +
                        "stones, just people. Perhaps that is how it was always meant to be.",
                )
        }
    }

    private suspend fun Dialogue.sanfewExplains() {
        chatPlayer(
            neutral,
            "Kaqemeex sent me. He says you know a ritual that could help the druids.",
        )
        chatNpc(neutral, "He sent you? Then he trusts you, and that is good enough for me.")
        chatNpc(
            neutral,
            "Look at these scrolls. They are older than the stone circle itself. Before there " +
                "were stones, our ancestors made offerings directly to the earth.",
        )
        chatNpc(
            neutral,
            "The ritual needs four kinds of meat, offered as one: beef, rat, bear and chicken. The " +
                "strong, the small, the wild and the tame. Together, they stand for balance.",
        )
        chatPlayer(quiz, "And then I just bring you some raw meat?")
        chatNpc(
            shocked,
            "Raw meat? Goodness, no. Each piece must first be dipped in the Cauldron of Thunder. " +
                "Only then is the offering pure.",
        )
        chatNpc(
            neutral,
            "Bring me enchanted beef, enchanted rat, enchanted bear and enchanted chicken, and I " +
                "will perform the ritual.",
        )
        quest.setQuestStage(access, STAGE_GATHERING)
    }

    private suspend fun Dialogue.sanfewOffering() {
        if (MEATS.all { access.carries(it.enchanted) }) {
            chatPlayer(happy, "I have all four enchanted meats.")
            chatNpc(
                happy,
                "Let me see... beef, rat, bear and chicken. Yes, I can feel the cauldron's power in " +
                    "them.",
            )
            chatNpc(
                neutral,
                "Stand back. It has been a very long time since anyone performed this ritual.",
            )
            if (!takeOffering()) {
                return
            }
            quest.setQuestStage(access, STAGE_RITUAL_DONE)
            mesbox(
                "Sanfew raises the offering and speaks words in a language you've never heard. The " +
                    "floor trembles briefly."
            )
            chatNpc(shocked, "...It is done. Did you feel that? The earth answered.")
            chatNpc(happy, "Go to Kaqemeex. He'll want to thank you himself.")
            return
        }
        if (MEATS.any { access.carries(it.raw) }) {
            chatNpc(
                neutral,
                "That meat hasn't been dipped in the Cauldron of Thunder yet. Raw meat is no " +
                    "offering, it's just dinner.",
            )
            return
        }
        chatNpc(
            neutral,
            "I need enchanted beef, enchanted rat, enchanted bear and enchanted chicken.",
        )
        chatNpc(
            neutral,
            "Dip the raw meat in the Cauldron of Thunder, and bring all four to me at once.",
        )
    }

    private fun Dialogue.takeOffering(): Boolean =
        player.invTransaction(access.inv) {
            val inventory = select(access.inv)
            for (meat in MEATS) {
                delete {
                    from = inventory
                    obj = meat.enchanted.asRSCM()
                    strictCount = 1
                }
            }
        }.success

    private data class Meat(val raw: String, val enchanted: String, val name: String)

    companion object {
        const val QUEST_KEY = "quest_lost_circle"

        const val STAGE_NOT_STARTED = 0
        const val STAGE_STARTED = 1
        const val STAGE_GATHERING = 2
        const val STAGE_RITUAL_DONE = 3
        const val STAGE_COMPLETE = 4

        const val HERBLORE_XP = 250.0

        private const val KAQEMEEX = "npc.kaqemeex"
        private const val SANFEW = "npc.sanfew"
        private const val GUAM_LEAF = "obj.guam_leaf"

        private val MEATS =
            listOf(
                Meat("obj.raw_beef", "obj.enchanted_beef", "raw beef"),
                Meat("obj.raw_rat_meat", "obj.enchanted_rat_meat", "raw rat meat"),
                Meat("obj.raw_bear_meat", "obj.enchanted_bear_meat", "raw bear meat"),
                Meat("obj.raw_chicken", "obj.enchanted_chicken", "raw chicken"),
            )

        private val HIGHLIGHT_TAGS = Regex("</?red>")

        private val JOURNAL =
            listOf(
                "I can start this quest by speaking to <red>Kaqemeex</red> in <red>Taverley</red>.",
                "The stone circle of Taverley has crumbled to dust and the druids have lost their " +
                    "power. I should speak to <red>Sanfew</red> on the upper floor of his house in " +
                    "<red>Taverley</red>.",
                "Sanfew needs <red>enchanted beef</red>, <red>enchanted rat</red>, <red>enchanted " +
                    "bear</red> and <red>enchanted chicken</red>. I can make them by dipping raw " +
                    "meat in the <red>Cauldron of Thunder</red>.",
                "Sanfew performed the ritual and the earth answered. I should return to " +
                    "<red>Kaqemeex</red>.",
            )
    }
}
