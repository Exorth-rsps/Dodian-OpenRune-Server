package org.rsmod.content.dodian.quest.pieceofcake

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.dodian.quest.common.carries
import org.rsmod.content.quest.manager.ItemRewardDisplay
import org.rsmod.content.quest.manager.QuestJournalBuilder
import org.rsmod.content.quest.manager.QuestScript
import org.rsmod.content.quest.manager.rewards
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.ScriptContext

class APieceOfCake @Inject constructor(private val objRepo: ObjRepository) :
    QuestScript(
        QUEST_KEY,
        "varp.dodian_piece_of_cake",
        rewards {
            xp("stat.cooking", COOKING_XP)
            extra("$CAKE_REWARD Cakes")
        },
        ItemRewardDisplay(CAKE, zoom = 400),
        questVarbit = "varbit.dodian_piece_of_cake_progress",
    ) {

    private var Player.eggGiven by boolVarBit("varbit.dodian_piece_of_cake_egg_given")
    private var Player.milkGiven by boolVarBit("varbit.dodian_piece_of_cake_milk_given")
    private var Player.flourGiven by boolVarBit("varbit.dodian_piece_of_cake_flour_given")

    override fun ScriptContext.init() {
        check(quest.maxSteps == STAGE_COMPLETE) {
            "A Piece of Cake end state is ${quest.maxSteps} in .data/dodian/quests.toml, but the " +
                "quest completes at stage $STAGE_COMPLETE."
        }
        onOpNpc1(BAKER) { startDialogue(it.npc) { baker() } }
    }

    override fun subTitle(): String =
        "speaking to the <col=800000>Baker</col> at his stall on the square in " +
            "<col=800000>Yanille</col>."

    override fun questLog(player: ProtectedAccess): String =
        questJournal(player) {
            val p = player.player
            line(
                "Rats ate all of the Baker's ingredients, and the <red>Wizards' Guild</red> has " +
                    "ordered a cake. I need to bring him:"
            )
            ingredient(p.eggGiven, "an <red>egg</red>")
            ingredient(p.milkGiven, "a <red>bucket of milk</red>")
            ingredient(p.flourGiven, "a <red>pot of flour</red>")
            line("Everything can be found at the <red>farm</red> outside town.")
        }

    override fun completedLog(player: ProtectedAccess): String =
        completionJournal(player) { line("The cake was finished just in time.") }

    private fun QuestJournalBuilder.ingredient(given: Boolean, text: String) {
        if (given) strike(text.replace(HIGHLIGHT_TAGS, "")) else line(text)
    }

    private suspend fun Dialogue.baker() {
        when (quest.getQuestStage(player)) {
            STAGE_NOT_STARTED -> bakerOffer()
            STAGE_STARTED -> bakerDelivery()
            else ->
                chatNpc(
                    happy,
                    "Ah, my saviour! The wizards loved the cake. One of them even smiled. I didn't " +
                        "know they could do that.",
                )
        }
    }

    private suspend fun Dialogue.bakerOffer() {
        chatNpc(worried, "Oh no, oh no, oh no... what am I going to do?")
        chatPlayer(quiz, "What's wrong?")
        chatNpc(
            angry,
            "Rats! A whole family of them got into my storeroom last night. They ate everything. " +
                "My eggs, my milk, every last bit of flour!",
        )
        chatNpc(
            worried,
            "And today of all days, the Wizards' Guild has ordered a big cake for their afternoon " +
                "tea.",
        )
        chatNpc(
            worried,
            "Have you ever seen an impatient wizard? I have. The last baker who was late with an " +
                "order spent a week as a frog.",
        )
        chatPlayer(neutral, "That sounds serious.")
        chatNpc(
            worried,
            "It is! I can't leave my stall, or the whole market will help themselves to my bread. " +
                "Could you fetch me some ingredients?",
        )
        val accept =
            choice2("Of course, what do you need?", true, "Sorry, I'm in a hurry.", false)
        if (!accept) {
            chatPlayer(neutral, "Sorry, I'm in a hurry.")
            chatNpc(
                sad,
                "So am I! So am I... If you change your mind, I'll be here. Hopefully not as a frog.",
            )
            return
        }
        chatPlayer(happy, "Of course, what do you need?")
        chatNpc(happy, "Oh, thank you! I need an egg, a bucket of milk and a pot of flour.")
        chatNpc(
            neutral,
            "You'll find everything at the farm outside town. There are chickens for the eggs and " +
                "a dairy cow for the milk.",
        )
        chatNpc(
            neutral,
            "For the flour, pick some wheat in the field, put the grain in the hopper at the mill " +
                "and work the controls. Then collect the flour with a pot.",
        )
        chatNpc(
            neutral,
            "You'll find a bucket and a pot lying around the farm. Farmers never tidy anything up.",
        )
        chatNpc(worried, "Please hurry! I can already feel my nose turning green.")
        quest.setQuestStage(access, STAGE_STARTED)
    }

    private suspend fun Dialogue.bakerDelivery() {
        val handed = mutableListOf<String>()
        if (!player.eggGiven && access.carries(EGG)) {
            if (access.invDel(access.inv, EGG).success) {
                player.eggGiven = true
                handed += "An egg! Lovely and fresh, too."
            }
        }
        if (!player.milkGiven && access.carries(MILK)) {
            if (access.invDel(access.inv, MILK).success) {
                access.invAddOrDrop(objRepo, BUCKET)
                player.milkGiven = true
                handed += "Milk! Here's your bucket back."
            }
        }
        if (!player.flourGiven && access.carries(FLOUR)) {
            if (access.invDel(access.inv, FLOUR).success) {
                access.invAddOrDrop(objRepo, POT)
                player.flourGiven = true
                handed += "Flour, wonderful! Here's your pot back."
            }
        }
        if (handed.isNotEmpty()) {
            chatNpc(happy, "You're back! Did you bring anything?")
            for (line in handed) {
                chatNpc(happy, line)
            }
        }
        if (player.eggGiven && player.milkGiven && player.flourGiven) {
            bakeCake()
        } else {
            stillNeeded()
        }
    }

    private suspend fun Dialogue.stillNeeded() {
        chatNpc(neutral, "I still need:")
        if (!player.eggGiven) {
            chatNpc(neutral, "An egg. The chickens at the farm should have laid a few by now.")
        }
        if (!player.milkGiven) {
            chatNpc(neutral, "A bucket of milk. Try the dairy cow at the farm.")
        }
        if (!player.flourGiven) {
            chatNpc(neutral, "A pot of flour. Grain in the hopper at the mill, then collect it with a pot.")
        }
        chatNpc(worried, "Please hurry, the wizards will be here soon!")
    }

    private suspend fun Dialogue.bakeCake() {
        chatNpc(
            happy,
            "That's everything! Egg, milk, flour... Now stand back and watch a master at work.",
        )
        mesbox("The baker mixes the ingredients at lightning speed and slides the tin into his oven.")
        chatNpc(neutral, "...")
        chatNpc(neutral, "...")
        chatNpc(
            happy,
            "There! A perfect cake, and with time to spare. The wizards will never know how close " +
                "it was.",
        )
        chatNpc(
            happy,
            "You've saved my stall, and very likely my nose too. Here, take these. Freshly baked, " +
                "of course.",
        )
        chatNpc(happy, "And if you ever want to learn to bake one yourself, you know who to ask.")
        access.invAddOrDrop(objRepo, CAKE, CAKE_REWARD)
        quest.completeQuest(access)
    }

    companion object {
        const val QUEST_KEY = "quest_piece_of_cake"

        const val STAGE_NOT_STARTED = 0
        const val STAGE_STARTED = 1
        const val STAGE_COMPLETE = 2

        const val COOKING_XP = 300.0
        const val CAKE_REWARD = 2

        private const val BAKER = "npc.baker_merchant"

        private const val EGG = "obj.egg"
        private const val MILK = "obj.bucket_milk"
        private const val FLOUR = "obj.pot_flour"
        private const val BUCKET = "obj.bucket_empty"
        private const val POT = "obj.pot_empty"
        private const val CAKE = "obj.cake"

        private val HIGHLIGHT_TAGS = Regex("</?red>")
    }
}
