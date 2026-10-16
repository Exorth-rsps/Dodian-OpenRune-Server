package org.rsmod.content.dodian.quest.forgottenaltar

import dev.openrune.rscm.RSCM.asRSCM
import jakarta.inject.Inject
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.dodian.quest.common.SharedTalk.onSharedTalk
import org.rsmod.content.quest.manager.ItemRewardDisplay
import org.rsmod.content.quest.manager.QuestScript
import org.rsmod.content.quest.manager.rewards
import org.rsmod.content.skills.runecrafting.RunecraftingGates
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Dodian's replacement for Rune Mysteries. The stages live in `varp.runemysteries` so anything that
 * checks for a completed Rune Mysteries keeps working. The spec numbers the stages 0..5; they map
 * onto the varp unchanged, except that completion is stored as 6, the Rune Mysteries end state.
 */
class TheForgottenAltar @Inject constructor(private val objRepo: ObjRepository) :
    QuestScript(
        QUEST_KEY,
        RUNE_MYSTERIES_VARP,
        rewards {
            extra("Air talisman")
            extra("Access to Runecrafting")
        },
        ItemRewardDisplay(AIR_TALISMAN, zoom = 400),
        questVarbit = RUNE_MYSTERIES_VARP,
    ) {

    override fun ScriptContext.init() {
        check(quest.maxSteps == STAGE_COMPLETE) {
            "The Forgotten Altar end state is ${quest.maxSteps} in .data/dodian/quests.toml, " +
                "but the quest completes at stage $STAGE_COMPLETE."
        }
        RunecraftingGates.register { player ->
            if (quest.isQuestCompleted(player)) {
                null
            } else {
                "You feel a strange power, but you don't understand how to use it."
            }
        }
        onOpNpc1(SININA) { startDialogue(it.npc) { sinina() } }
        onOpNpc1(PERSTEN) { startDialogue(it.npc) { persten() } }
        onSharedTalk(TRAIBORN, priority = 0) {
            traiborn()
            true
        }
    }

    override fun subTitle(): String =
        "speaking to <col=800000>Wizard Sinina</col> at the top of the " +
            "<col=800000>Wizards' Guild</col> in <col=800000>Yanille</col>."

    override fun questLog(player: ProtectedAccess): String =
        questJournal(player) {
            val stage = quest.getQuestStage(player.player)
            for (step in STAGE_TALISMAN until stage) {
                strike(JOURNAL[step].replace(HIGHLIGHT_TAGS, ""))
            }
            line(JOURNAL[stage])
        }

    override fun completedLog(player: ProtectedAccess): String =
        completionJournal(player) {
            for (step in STAGE_TALISMAN..STAGE_RETURNED) {
                line(JOURNAL[step].replace(HIGHLIGHT_TAGS, ""))
            }
            line("I have rediscovered the art of Runecrafting.")
        }

    private fun stage(access: ProtectedAccess): Int = quest.getQuestStage(access.player)

    private suspend fun Dialogue.sinina() {
        when (stage(access)) {
            STAGE_NOT_STARTED -> sininaOffer()
            STAGE_TALISMAN ->
                if (hasAnywhere(AIR_TALISMAN)) {
                    chatNpc(
                        quiz,
                        "Have you brought the talisman to Wizard Persten yet? Her tower is near " +
                            "the Crafting Guild, at the very top.",
                    )
                } else {
                    chatNpc(
                        worried,
                        "You lost it? Oh dear... Luckily the vault had more than one of those " +
                            "strange stones. Here, take this one, and please be more careful.",
                    )
                    access.invAddOrDrop(objRepo, AIR_TALISMAN)
                }
            STAGE_PACKAGE,
            STAGE_NOTES ->
                chatNpc(
                    laugh,
                    "Persten and Traiborn working together? I'd pay good money to see that. Let " +
                        "me know what they find.",
                )
            STAGE_RETURNED -> sininaComplete()
            else ->
                chatNpc(
                    happy,
                    "Hello again, runecrafter! I've started cataloguing the rest of the vault. " +
                        "Who knows what else is hiding in there.",
                )
        }
    }

    private suspend fun Dialogue.sininaOffer() {
        chatNpc(
            shocked,
            "Oh! Careful where you step, adventurer. I've been clearing out the old guild vault " +
                "and there's dust everywhere.",
        )
        chatPlayer(quiz, "Find anything interesting?")
        chatNpc(
            neutral,
            "Mostly broken wands and mouldy spellbooks. But this... this is different. Here, listen.",
        )
        chatNpc(
            neutral,
            "Hold it close. Can you hear it? It hums. Softly, like the wind through a keyhole.",
        )
        chatPlayer(confused, "It's... vibrating. What is it?")
        chatNpc(
            neutral,
            "That's just it, I don't know. None of the books in the vault mention it. But I know " +
                "someone who might.",
        )
        chatNpc(
            quiz,
            "Wizard Persten. She spends her days studying magic older than this guild itself. " +
                "Would you take it to her?",
        )
        val accept =
            choice2("Of course, I'll take it to her.", true, "Not right now.", false)
        if (!accept) {
            chatPlayer(neutral, "Not right now, sorry.")
            chatNpc(
                neutral,
                "Very well. The talisman isn't going anywhere. It's been in that vault for " +
                    "centuries, after all.",
            )
            return
        }
        chatPlayer(happy, "Of course, I'll take it to her. Where can I find her?")
        chatNpc(
            neutral,
            "She lives in the tower near the Crafting Guild. You'll find her at the very top. She " +
                "likes to be close to the sky.",
        )
        chatNpc(
            worried,
            "Be careful with it. Something tells me it's more important than it looks.",
        )
        if (access.inv.isFull()) {
            access.mes("You need a free inventory space.")
            return
        }
        access.invAdd(access.inv, AIR_TALISMAN)
        quest.setQuestStage(access, STAGE_TALISMAN)
    }

    private suspend fun Dialogue.sininaComplete() {
        chatPlayer(happy, "Sinina! We found out what the talisman is.")
        chatNpc(shocked, "Tell me everything!")
        chatPlayer(
            neutral,
            "It's a key. Ancient altars are hidden inside ruins all over the land. With a " +
                "talisman, you can enter them.",
        )
        chatPlayer(neutral, "And at those altars you can turn rune essence into runes.")
        chatNpc(
            shocked,
            "Make our own runes? That's... that's a craft we lost so long ago, nobody even " +
                "remembered it existed.",
        )
        chatNpc(
            sad,
            "And it was lying in our own vault, collecting dust. I feel rather foolish.",
        )
        chatNpc(
            happy,
            "You've brought something back that the whole world forgot. The craft is yours now: " +
                "Runecrafting.",
        )
        chatNpc(
            neutral,
            "Keep that talisman. It will lead you to the air altar near Traiborn's house. Find " +
                "more talismans and you'll find more altars.",
        )
        removeQuestItems()
        quest.completeQuest(access)
    }

    private suspend fun Dialogue.persten() {
        when (stage(access)) {
            STAGE_NOT_STARTED ->
                chatNpc(
                    angry,
                    "Shh! Can't you see I'm concentrating? Come back when you have something " +
                        "worth my time.",
                )
            STAGE_TALISMAN ->
                if (player.inv.contains(AIR_TALISMAN)) {
                    perstenTakesTalisman()
                } else {
                    chatNpc(
                        quiz,
                        "Sinina sent you? Then where is this mysterious object of hers? Go and " +
                            "get it back.",
                    )
                }
            STAGE_PACKAGE ->
                if (hasAnywhere(RESEARCH_PACKAGE)) {
                    chatNpc(
                        quiz,
                        "Have you delivered my research to Traiborn yet? His house is next to " +
                            "the air ruins.",
                    )
                    chatNpc(
                        neutral,
                        "And make sure he doesn't put it somewhere and forget about it.",
                    )
                } else {
                    chatNpc(
                        angry,
                        "You lost my research? Years of work! ...Fortunately I always keep a " +
                            "copy. Here. Don't lose this one.",
                    )
                    access.invAddOrDrop(objRepo, RESEARCH_PACKAGE)
                }
            STAGE_NOTES ->
                if (player.inv.contains(RESEARCH_NOTES)) {
                    perstenReadsNotes()
                } else {
                    chatNpc(
                        quiz,
                        "Did Traiborn give you anything? I need his notes to confirm my theory.",
                    )
                }
            STAGE_RETURNED ->
                chatNpc(
                    neutral,
                    "What are you still doing here? Go and tell Sinina what we found!",
                )
            else ->
                chatNpc(
                    happy,
                    "Ah, the runecrafter. I'm writing a book about our discovery. Don't worry, " +
                        "you'll be in the acknowledgements.",
                )
        }
    }

    private suspend fun Dialogue.perstenTakesTalisman() {
        chatPlayer(
            neutral,
            "Wizard Persten? Wizard Sinina sent me. She found this in the vault of the Wizards' " +
                "Guild.",
        )
        chatNpc(
            quiz,
            "Sinina? What has she dug up this time... Wait. Let me see that.",
        )
        chatNpc(
            shocked,
            "By Saradomin's beard... it's humming. Do you have any idea what this is?",
        )
        chatPlayer(neutral, "I was hoping you would.")
        chatNpc(
            neutral,
            "I've read about objects like this. Fragments of very old texts. They speak of altars " +
                "that can't be seen, only entered.",
        )
        chatNpc(
            neutral,
            "If I'm right, this talisman is a key. But I need proof. Theories are nothing without " +
                "proof.",
        )
        chatNpc(
            neutral,
            "I'll keep the talisman here to study. Meanwhile, take this package with my research " +
                "to Wizard Traiborn.",
        )
        chatPlayer(quiz, "Who is he?")
        chatNpc(
            neutral,
            "An old colleague. Scatterbrained, terribly forgetful, but he's lived next to the air " +
                "ruins for years and has measured every gust of magic that comes out of them.",
        )
        chatNpc(
            neutral,
            "If anyone can confirm my theory, it's him. You'll find his house right next to the " +
                "ruins.",
        )
        if (swap(AIR_TALISMAN, RESEARCH_PACKAGE)) {
            quest.setQuestStage(access, STAGE_PACKAGE)
        }
    }

    private suspend fun Dialogue.perstenReadsNotes() {
        chatPlayer(neutral, "Traiborn gave me his notes.")
        chatNpc(
            happy,
            "Finally! Let me see... Oh, his handwriting is dreadful. And is that a soup stain?",
        )
        chatNpc(
            shocked,
            "But this... yes... yes! Look here. He measured the energy around the ruins every day " +
                "for years. And whenever someone stood near the ruins with a talisman, the " +
                "energy spiked.",
        )
        chatNpc(
            happy,
            "I was right. The talisman is a key. The ruins are doorways to hidden altars, each " +
                "bound to its own element.",
        )
        chatNpc(
            neutral,
            "And at those altars, rune essence can be bound into runes. Air, water, earth, fire... " +
                "perhaps even more.",
        )
        chatPlayer(quiz, "So anyone with a talisman and some essence can make runes?")
        chatNpc(
            neutral,
            "Anyone who knows how. And now you know how. Here's your talisman back. You've " +
                "earned it.",
        )
        chatNpc(
            happy,
            "Go and tell Sinina. She'll want to hear that her dusty vault hid the greatest " +
                "discovery of the age.",
        )
        if (swap(RESEARCH_NOTES, AIR_TALISMAN)) {
            quest.setQuestStage(access, STAGE_RETURNED)
        }
    }

    private suspend fun Dialogue.traiborn() {
        when (stage(access)) {
            STAGE_NOT_STARTED,
            STAGE_TALISMAN ->
                chatNpc(
                    confused,
                    "Hmm? Oh, hello. Did I invite you? I don't remember inviting anyone. I don't " +
                        "remember much, to be honest.",
                )
            STAGE_PACKAGE ->
                if (player.inv.contains(RESEARCH_PACKAGE)) {
                    traibornReadsPackage()
                } else {
                    chatNpc(
                        confused,
                        "Persten sent you? With a package? I don't see any package. Unless I've " +
                            "forgotten it already. That happens.",
                    )
                }
            STAGE_NOTES ->
                if (hasAnywhere(RESEARCH_NOTES)) {
                    chatNpc(
                        confused,
                        "Shouldn't you be bringing those notes to Persten? Or was I supposed to do " +
                            "that? No, I'm fairly sure it was you.",
                    )
                } else {
                    chatNpc(
                        happy,
                        "Lost my notes? Oh, don't worry. I wrote everything down twice. Or was it " +
                            "three times? Here, take this copy.",
                    )
                    access.invAddOrDrop(objRepo, RESEARCH_NOTES)
                }
            else ->
                chatNpc(
                    laugh,
                    "So the ruins are a door, and the talisman is the key! And to think I've lived " +
                        "next to it all these years and only ever used it to dry my socks.",
                )
        }
    }

    private suspend fun Dialogue.traibornReadsPackage() {
        chatPlayer(neutral, "Wizard Traiborn? Wizard Persten sent me with this package.")
        chatNpc(
            confused,
            "Persten? Persten... ah yes, the one with the tower! Very serious woman. Never laughs " +
                "at my jokes.",
        )
        chatNpc(happy, "A package for me? How lovely. Is it my birthday?")
        chatPlayer(
            neutral,
            "It's her research. About a talisman that hums. She thinks it's the key to an altar " +
                "in the ruins.",
        )
        chatNpc(
            neutral,
            "The ruins? Oh, those ruins! I've been studying those for... well, a long time. " +
                "Years, I think. Possibly decades.",
        )
        chatNpc(shocked, "Let me read... mmh... mhm... OH! But that explains everything!")
        chatNpc(
            neutral,
            "Every now and then the energy from the ruins jumps right off my scales. I always " +
                "thought it was the weather. Or my cat.",
        )
        chatNpc(
            confused,
            "I've written it all down, somewhere. Now where did I put those notes...",
        )
        chatNpc(
            happy,
            "Not under the bed... not in the teapot... aha! In my hat. Of course. Where else " +
                "would I keep them?",
        )
        chatNpc(
            neutral,
            "Here, take them to Persten. And tell her the soup stain was there when I found it.",
        )
        if (swap(RESEARCH_PACKAGE, RESEARCH_NOTES)) {
            quest.setQuestStage(access, STAGE_NOTES)
        }
    }

    private fun Dialogue.hasAnywhere(item: String): Boolean =
        access.inv.contains(item) || access.bank.contains(item)

    private fun Dialogue.removeQuestItems() {
        for (item in listOf(RESEARCH_PACKAGE, RESEARCH_NOTES)) {
            for (inv in listOf(access.inv, access.bank)) {
                val count = access.invTotal(inv, item)
                if (count > 0) {
                    access.invDel(inv, item, count)
                }
            }
        }
    }

    /** Swaps one [taken] for one [given] in a single transaction, so nothing is lost. */
    private fun Dialogue.swap(taken: String, given: String): Boolean =
        player.invTransaction(access.inv) {
            val inventory = select(access.inv)
            delete {
                from = inventory
                obj = taken.asRSCM()
                strictCount = 1
            }
            insert {
                into = inventory
                obj = given.asRSCM()
                strictCount = 1
            }
        }.success

    companion object {
        const val QUEST_KEY = "quest_forgotten_altar"

        const val STAGE_NOT_STARTED = 0
        const val STAGE_TALISMAN = 1
        const val STAGE_PACKAGE = 2
        const val STAGE_NOTES = 3
        const val STAGE_RETURNED = 4
        const val STAGE_COMPLETE = 6

        private const val RUNE_MYSTERIES_VARP = "varp.runemysteries"

        private const val SININA = "npc.magic_store_owner2"
        private const val PERSTEN = "npc.tote_persten_alkharid_child"
        private const val TRAIBORN = "npc.traiborn"

        private const val AIR_TALISMAN = "obj.air_talisman"
        private const val RESEARCH_PACKAGE = "obj.research_package"
        private const val RESEARCH_NOTES = "obj.research_notes"

        private val HIGHLIGHT_TAGS = Regex("</?red>")

        private val JOURNAL =
            listOf(
                "I can start this quest by speaking to <red>Wizard Sinina</red> at the top of the " +
                    "<red>Wizards' Guild</red> in <red>Yanille</red>.",
                "Sinina found a strange humming <red>talisman</red> in the guild vault. I should " +
                    "take it to <red>Wizard Persten</red> at the top of the tower near the " +
                    "<red>Crafting Guild</red>.",
                "Persten thinks the talisman is a key. She gave me her <red>research package</red> " +
                    "to deliver to <red>Wizard Traiborn</red>, who lives next to the " +
                    "<red>air ruins</red>.",
                "Traiborn gave me his <red>notes</red> about the ruins. I should bring them to " +
                    "<red>Wizard Persten</red>.",
                "The talisman is a key to hidden altars where runes can be crafted! I should tell " +
                    "<red>Wizard Sinina</red>.",
            )
    }
}
