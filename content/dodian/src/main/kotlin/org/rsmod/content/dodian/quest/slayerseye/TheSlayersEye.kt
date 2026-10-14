package org.rsmod.content.dodian.quest.slayerseye

import dev.openrune.rscm.RSCM.asRSCM
import jakarta.inject.Inject
import org.rsmod.api.invtx.invAddOrDrop
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.quest.manager.ItemRewardDisplay
import org.rsmod.content.quest.manager.QuestScript
import org.rsmod.content.quest.manager.rewards
import org.rsmod.content.slayer.SlayerGate
import org.rsmod.content.slayer.SlayerGates
import org.rsmod.content.slayer.dialogue.SlayerMasters
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.ScriptContext

class TheSlayersEye @Inject constructor(private val objRepo: ObjRepository) :
    QuestScript(
        QUEST_KEY,
        "varp.dodian_slayers_eye",
        rewards {
            xp("stat.slayer", SLAYER_XP)
            extra("20,000 coins")
            extra("Access to Slayer masters")
        },
        ItemRewardDisplay(ENCHANTED_GEM, zoom = 400),
        questVarbit = "varbit.dodian_slayers_eye_progress",
    ) {

    override fun ScriptContext.init() {
        check(quest.maxSteps == STAGE_COMPLETE) {
            "The Slayer's Eye end state is ${quest.maxSteps} in .data/dodian/quests.toml, " +
                "but the quest completes at stage $STAGE_COMPLETE."
        }
        SlayerGates.register(MasterGate())
        onOpNpc1(GEM_TRADER) { startDialogue(it.npc) { gemTrader() } }
        onOpNpc1(AUBURY) { startDialogue(it.npc) { aubury() } }
    }

    override fun subTitle(): String =
        "speaking to <col=800000>Aya</col> in the <col=800000>Heroes' Guild</col>."

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
            line("I have become a slayer and can now get tasks from any slayer master.")
        }

    private inner class MasterGate : SlayerGate {
        override fun refusal(player: Player): String? =
            if (quest.isQuestCompleted(player)) {
                null
            } else {
                "You don't even have an Enchanted Gem. Speak to Aya in the Heroes' Guild if you " +
                    "want to become a slayer."
            }

        override suspend fun Dialogue.beforeTalk(npcId: Int): Boolean =
            when (npcId) {
                SlayerMasters.Npc.aya -> aya()
                SlayerMasters.Npc.turael -> tureal()
                else -> false
            }
    }

    private fun stage(player: Player): Int = quest.getQuestStage(player)

    private suspend fun Dialogue.aya(): Boolean {
        when (stage(player)) {
            STAGE_NOT_STARTED -> ayaOffer()
            in STAGE_STARTED..STAGE_COLLECTING -> {
                chatNpc(neutral, "Have you spoken to my father yet? He's a guard at the prison in Yanille.")
                chatNpc(neutral, "If anyone knows how to make a new Eye, it's him.")
            }
            STAGE_GEM_MADE ->
                if (player.inv.contains(ENCHANTED_GEM)) {
                    ayaComplete()
                } else {
                    chatNpc(
                        happy,
                        "Aubury made the Eye for you? Wonderful! But where is it? Bring it here so I " +
                            "can see it with my own eyes.",
                    )
                }
            else -> {
                chatNpc(
                    happy,
                    "Hello again, slayer. Have you been to a slayer master yet? They'll be glad to " +
                        "have someone like you.",
                )
                return false
            }
        }
        return true
    }

    private suspend fun Dialogue.ayaOffer() {
        chatNpc(
            neutral,
            "Ah, an adventurer. You have the look of someone who's faced a few monsters in their time.",
        )
        chatPlayer(quiz, "I've had my share. Is something wrong?")
        chatNpc(
            sad,
            "Something's been lost. A stone... we call it the Enchanted Gem. It's like an eye. " +
                "Through it, a slayer master can see the tasks they've given, and the slayer can " +
                "see how many creatures remain.",
        )
        chatNpc(
            sad,
            "Without it, no slayer master can hand out tasks. The whole craft is grinding to a halt.",
        )
        chatPlayer(quiz, "Can't you just make a new one?")
        chatNpc(
            neutral,
            "If only it were that simple. I never learned how they were made. But my father... " +
                "he knows far more about the Eye than I ever did.",
        )
        chatNpc(quiz, "Perhaps he even knows how to make one. Will you help me?")
        val accept = choice2("Yes, I'll help you.", true, "Sorry, I'm busy.", false)
        if (!accept) {
            chatPlayer(neutral, "Sorry, I'm busy right now.")
            chatNpc(neutral, "I understand. If you change your mind, you know where to find me.")
            return
        }
        chatPlayer(happy, "Yes, I'll help you. Where can I find your father?")
        chatNpc(
            neutral,
            "His name is Tureal. He works as a guard at the prison in Yanille. Tell him I sent " +
                "you. He can be a bit gruff, but he means well.",
        )
        chatNpc(happy, "Thank you, adventurer. The slayer masters will owe you a great deal.")
        quest.setQuestStage(access, STAGE_STARTED)
    }

    private suspend fun Dialogue.ayaComplete() {
        chatPlayer(happy, "Aya, I have it. A new Enchanted Gem!")
        chatNpc(shocked, "Let me see... By the gods, it's real. You've actually done it!")
        chatNpc(happy, "My father, the gem trader, Aubury... and you, bringing it all together.")
        chatNpc(
            happy,
            "You've saved the work of the slayer masters. Without you, the craft would have died " +
                "out completely.",
        )
        chatNpc(
            neutral,
            "And there's something else. Anyone who carries an Eye is a slayer. From today, that " +
                "includes you.",
        )
        chatNpc(
            neutral,
            "Visit any slayer master and they'll give you a task. Use the gem to check how many " +
                "creatures you still have to slay.",
        )
        chatNpc(happy, "Welcome to the hunt, slayer.")
        player.invAddOrDrop(objRepo, COINS, COIN_REWARD)
        quest.completeQuest(access)
    }

    private suspend fun Dialogue.tureal(): Boolean {
        when (stage(player)) {
            STAGE_NOT_STARTED -> return false
            STAGE_STARTED -> turealExplains()
            in STAGE_TUREAL_SPOKEN..STAGE_GEM_MADE -> {
                chatNpc(
                    neutral,
                    "Still here? You'll need an uncut sapphire, a bone shard and an eye of newt.",
                )
                chatNpc(neutral, "Talk to the gem trader in Yanille. He made the Eyes back in the day.")
            }
            else -> {
                chatNpc(
                    happy,
                    "So you're the one who made the new Eye. Aya told me all about it. Good work, slayer.",
                )
                return false
            }
        }
        return true
    }

    private suspend fun Dialogue.turealExplains() {
        chatPlayer(quiz, "Are you Tureal? Your daughter Aya sent me.")
        chatNpc(laugh, "Aya? Hah, she still remembers her old man. What does she want?")
        chatPlayer(
            neutral,
            "She says the Enchanted Gem has been lost. The slayer masters can't give out tasks " +
                "without it.",
        )
        chatNpc(neutral, "The Eye... I haven't heard anyone speak of it in years.")
        chatNpc(
            neutral,
            "Yes, I know what it is. Every slayer used to carry one. Slayer master hands out a " +
                "task, and the Eye keeps track of it for you. Clever little things.",
        )
        chatPlayer(quiz, "Do you know how to make one?")
        chatNpc(
            neutral,
            "Not myself, no. But I know what goes into it. You'll need an uncut sapphire, a bone " +
                "shard and an eye of newt.",
        )
        chatNpc(
            neutral,
            "The sapphire forms the body of the Eye. The bone shard gives it the memory of the " +
                "creatures it hunts. And the eye of newt... well, it's an eye, isn't it? It lets " +
                "the stone see.",
        )
        chatPlayer(quiz, "And who puts it all together?")
        chatNpc(
            neutral,
            "I had a friend who could make them. He runs the gem stall here in Yanille. Go and " +
                "talk to him, tell him Tureal sent you.",
        )
        quest.setQuestStage(access, STAGE_TUREAL_SPOKEN)
    }

    private suspend fun Dialogue.gemTrader() {
        when (stage(player)) {
            STAGE_TUREAL_SPOKEN -> {
                chatPlayer(
                    neutral,
                    "Tureal sent me. He says you used to make the Enchanted Gems.",
                )
                chatNpc(
                    laugh,
                    "Tureal? Ha! Haven't seen that old dog in years. The Enchanted Gems, you say...",
                )
                chatNpc(
                    neutral,
                    "I'm afraid he's given me too much credit. I never did anything special. I " +
                        "only cut the uncut sapphires into proper sapphires.",
                )
                chatNpc(
                    neutral,
                    "Any decent crafter can do that. You could do it yourself, or I can do it for you.",
                )
                chatPlayer(quiz, "Then who made them into Enchanted Gems?")
                chatNpc(
                    neutral,
                    "That was the work of a wizard. Aubury, he lives at the Wizards' Guild here " +
                        "in Yanille. With the right ingredients, he can enchant the sapphire.",
                )
                quest.setQuestStage(access, STAGE_GEM_TRADER_SPOKEN)
                cutOptions()
            }
            in STAGE_GEM_TRADER_SPOKEN..STAGE_GEM_MADE -> {
                chatNpc(
                    neutral,
                    "Back again? Remember, Aubury at the Wizards' Guild can enchant the sapphire.",
                )
                cutOptions()
            }
            else -> chatNpc(neutral, "Fine gems, cut and uncut! Have a look around.")
        }
    }

    private suspend fun Dialogue.cutOptions() {
        when (
            choice3(
                "Can you cut my uncut sapphire for me?",
                1,
                "I'll cut it myself.",
                2,
                "Thanks, I'll go see Aubury.",
                3,
            )
        ) {
            1 -> {
                if (!player.inv.contains(UNCUT_SAPPHIRE)) {
                    chatNpc(
                        neutral,
                        "You don't have an uncut sapphire with you. Bring me one and I'll cut it for you.",
                    )
                    return
                }
                chatPlayer(quiz, "Can you cut my uncut sapphire for me?")
                chatNpc(happy, "For a friend of Tureal? Of course. Hand it over...")
                if (!swap(listOf(UNCUT_SAPPHIRE), SAPPHIRE)) {
                    return
                }
                chatNpc(
                    happy,
                    "There you go. Clean cut, if I may say so myself. Take it to Aubury at the " +
                        "Wizards' Guild.",
                )
            }
            2 -> {
                chatPlayer(neutral, "I'll cut it myself.")
                chatNpc(
                    neutral,
                    "Good for you. Just bring the cut sapphire to Aubury at the Wizards' Guild.",
                )
            }
            3 -> {
                chatPlayer(neutral, "Thanks, I'll go see Aubury.")
                chatNpc(
                    neutral,
                    "Don't forget, he'll need the sapphire cut. Come back if you want me to do it.",
                )
            }
        }
    }

    private suspend fun Dialogue.aubury() {
        when (stage(player)) {
            STAGE_GEM_TRADER_SPOKEN -> auburyExplains()
            STAGE_COLLECTING -> auburyCheckItems(advance = true)
            STAGE_GEM_MADE ->
                if (hasGemAnywhere(access)) {
                    chatNpc(neutral, "Go on, take the gem to whoever sent you.")
                } else {
                    chatNpc(
                        neutral,
                        "Lost it already? Bring me another sapphire, bone shard and eye of newt " +
                            "and I'll make you a new one.",
                    )
                    auburyCheckItems(advance = false)
                }
            else -> chatNpc(neutral, "Hello there. I'm rather busy with my studies at the moment.")
        }
    }

    private suspend fun Dialogue.auburyExplains() {
        chatPlayer(neutral, "The gem trader sent me. He says you can make an Enchanted Gem.")
        chatNpc(
            happy,
            "An Enchanted Gem? My, my... that takes me back. Yes, I used to enchant those for the " +
                "slayer masters.",
        )
        chatNpc(neutral, "I'll need three things: a cut sapphire, a bone shard and an eye of newt.")
        chatPlayer(quiz, "Where can I find a bone shard and an eye of newt?")
        chatNpc(
            neutral,
            "Skeletons sometimes carry bone shards. Old, brittle things, they practically fall " +
                "apart when you hit them.",
        )
        chatNpc(
            neutral,
            "As for the eye of newt, try the moss giants. Don't ask me why they have them. Giants " +
                "eat all sorts of things.",
        )
        chatNpc(neutral, "Bring me all three and I'll enchant the gem for you.")
        quest.setQuestStage(access, STAGE_COLLECTING)
        if (hasAllIngredients(player)) {
            enchantGem(advance = true)
        }
    }

    private suspend fun Dialogue.auburyCheckItems(advance: Boolean) {
        when {
            hasAllIngredients(player) -> enchantGem(advance)
            player.inv.contains(UNCUT_SAPPHIRE) && !player.inv.contains(SAPPHIRE) ->
                chatNpc(
                    neutral,
                    "That sapphire is still uncut. I can't enchant it like that. Cut it yourself, " +
                        "or have the gem trader do it for you.",
                )
            else -> {
                chatNpc(
                    quiz,
                    "Do you have everything? I need a cut sapphire, a bone shard and an eye of newt.",
                )
                chatNpc(
                    neutral,
                    "Remember, skeletons drop the bone shards and moss giants carry the eyes of newt.",
                )
            }
        }
    }

    private suspend fun Dialogue.enchantGem(advance: Boolean) {
        chatPlayer(happy, "I have everything you need.")
        chatNpc(
            happy,
            "Excellent! Let me see... a sapphire, a bone shard and an eye of newt. Perfect.",
        )
        chatNpc(neutral, "Stand back a little. This needs concentration.")
        if (!swap(INGREDIENTS, ENCHANTED_GEM)) {
            return
        }
        if (advance) {
            quest.setQuestStage(access, STAGE_GEM_MADE)
        }
        chatNpc(happy, "There we are. Look closely, it's watching you.")
        chatNpc(neutral, "Take it back to whoever sent you. I'm sure they'll be very pleased.")
    }

    /** Swaps [taken] for one [given] in a single transaction, so nothing is lost on a full pack. */
    private fun Dialogue.swap(taken: List<String>, given: String): Boolean =
        player.invTransaction(access.inv) {
            val inventory = select(access.inv)
            for (item in taken) {
                delete {
                    from = inventory
                    obj = item.asRSCM()
                    strictCount = 1
                }
            }
            insert {
                into = inventory
                obj = given.asRSCM()
                strictCount = 1
            }
        }.success

    private fun hasAllIngredients(player: Player): Boolean =
        INGREDIENTS.all { player.inv.contains(it) }

    private fun hasGemAnywhere(access: ProtectedAccess): Boolean =
        access.inv.contains(ENCHANTED_GEM) || access.bank.contains(ENCHANTED_GEM)

    companion object {
        const val QUEST_KEY = "quest_slayers_eye"

        const val STAGE_NOT_STARTED = 0
        const val STAGE_STARTED = 1
        const val STAGE_TUREAL_SPOKEN = 2
        const val STAGE_GEM_TRADER_SPOKEN = 3
        const val STAGE_COLLECTING = 4
        const val STAGE_GEM_MADE = 5
        const val STAGE_COMPLETE = 6

        const val BONE_SHARD = "obj.zqboneshard"
        const val EYE_OF_NEWT = "obj.eye_of_newt"

        private const val GEM_TRADER = "npc.gem_merchant_ardougne"
        private const val AUBURY = "npc.bim_aubury"

        private const val UNCUT_SAPPHIRE = "obj.uncut_sapphire"
        private const val SAPPHIRE = "obj.sapphire"
        private const val ENCHANTED_GEM = "obj.slayer_gem"
        private const val COINS = "obj.coins"
        private val INGREDIENTS = listOf(SAPPHIRE, BONE_SHARD, EYE_OF_NEWT)

        private const val SLAYER_XP = 10_000.0
        private const val COIN_REWARD = 20_000

        private val HIGHLIGHT_TAGS = Regex("</?red>")

        private val JOURNAL =
            listOf(
                "I can start this quest by speaking to <red>Aya</red> in the <red>Heroes' Guild</red>.",
                "Aya told me the Enchanted Gem has been lost. Her father, <red>Tureal</red>, might " +
                    "know more. He's a guard at the <red>Yanille prison</red>.",
                "Tureal said I need an <red>uncut sapphire</red>, a <red>bone shard</red> and an " +
                    "<red>eye of newt</red>. He sent me to the <red>gem trader</red> in <red>Yanille</red>.",
                "The gem trader only cut the sapphires. <red>Aubury</red> at the " +
                    "<red>Wizards' Guild</red> in <red>Yanille</red> can enchant the gem.",
                "Aubury needs a <red>cut sapphire</red>, a <red>bone shard</red> (dropped by " +
                    "skeletons) and an <red>eye of newt</red> (dropped by moss giants).",
                "Aubury made the <red>Enchanted gem</red>. I should show it to <red>Aya</red> in " +
                    "the <red>Heroes' Guild</red>.",
            )
    }
}
