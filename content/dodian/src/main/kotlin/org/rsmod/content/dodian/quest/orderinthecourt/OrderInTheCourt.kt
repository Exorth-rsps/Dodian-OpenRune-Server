package org.rsmod.content.dodian.quest.orderinthecourt

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.dodian.quest.common.DodianQuestLocs
import org.rsmod.content.dodian.quest.common.carries
import org.rsmod.content.dodian.quest.common.ownsAnywhere
import org.rsmod.content.quest.manager.ItemRewardDisplay
import org.rsmod.content.quest.manager.QuestScript
import org.rsmod.content.quest.manager.chooseLampSkill
import org.rsmod.content.quest.manager.rewards
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.ScriptContext

class OrderInTheCourt @Inject constructor(private val objRepo: ObjRepository) :
    QuestScript(
        QUEST_KEY,
        "varp.dodian_order_in_the_court",
        rewards {
            extra("%,d coins".format(COIN_REWARD))
            extra("%,d XP lamp".format(LAMP_XP.toInt()))
        },
        ItemRewardDisplay(FLAX, zoom = 400),
        questVarbit = "varbit.dodian_order_in_the_court_progress",
    ) {

    private var Player.talkedBartender by boolVarBit("varbit.dodian_order_in_the_court_talked_bartender")
    private var Player.foundFlax by boolVarBit("varbit.dodian_order_in_the_court_found_flax")
    private var Player.talkedSalesman by boolVarBit("varbit.dodian_order_in_the_court_talked_salesman")
    private var Player.talkedFlaxKeeper by
        boolVarBit("varbit.dodian_order_in_the_court_talked_flaxkeeper")
    private var Player.talkedSeer by boolVarBit("varbit.dodian_order_in_the_court_talked_seer")
    private var Player.foundPoison by boolVarBit("varbit.dodian_order_in_the_court_found_poison")

    override fun ScriptContext.init() {
        check(quest.maxSteps == STAGE_COMPLETE) {
            "Order in the Court end state is ${quest.maxSteps} in .data/dodian/quests.toml, but " +
                "the quest completes at stage $STAGE_COMPLETE."
        }
        onOpNpc1(KING_ARTHUR) { startDialogue(it.npc) { arthur() } }
        onOpNpc1(BARTENDER) { startDialogue(it.npc) { bartender() } }
        onOpNpc1(POISON_SALESMAN) { startDialogue(it.npc) { salesman() } }
        onOpNpc1(FLAX_KEEPER) { startDialogue(it.npc) { flaxKeeper() } }
        onOpNpc1(SEER) { startDialogue(it.npc) { seer() } }
        for (bales in DodianQuestLocs.locs("order_in_the_court_flax_bales")) {
            onOpLoc1(bales) { searchBales() }
        }
        onOpHeld1(LAMP) { rubLamp() }
    }

    override fun subTitle(): String =
        "speaking to <col=800000>King Arthur</col> in the <col=800000>courthouse</col> in " +
            "<col=800000>Seers' Village</col>."

    override fun questLog(player: ProtectedAccess): String =
        questJournal(player) {
            val p = player.player
            line(
                "Someone poisoned the ale at the <red>Forester's Arms</red>. Everyone blames the " +
                    "<red>Poison Salesman</red>, but King Arthur has his doubts."
            )
            if (p.foundFlax) {
                line("The Bartender found <red>flax</red> by the poisoned barrel.")
            }
            if (p.talkedSalesman) {
                line(
                    "The Poison Salesman sold one vial of weapon poison yesterday, to a man who " +
                        "<red>smelled of flax</red>."
                )
            }
            if (p.talkedFlaxKeeper) {
                line("The Flax keeper says he was home all evening. He seemed nervous.")
            }
            if (p.foundPoison) {
                line(
                    "I found a half-empty vial of <red>weapon poison</red> in the <red>bales of " +
                        "flax</red>."
                )
            }
            line("When I'm ready, I can present my case to <red>King Arthur</red>.")
        }

    override fun completedLog(player: ProtectedAccess): String =
        completionJournal(player) {
            line("The Flax keeper confessed, and the Poison Salesman is free.")
        }

    private fun stage(player: Player): Int = quest.getQuestStage(player)

    private suspend fun Dialogue.arthur() {
        when (stage(player)) {
            STAGE_NOT_STARTED -> arthurOffer()
            STAGE_INVESTIGATING -> {
                chatNpc(
                    quiz,
                    "How goes the investigation? Are you ready to present your case to the court?",
                )
                val ready = choice2("I'm ready to present my case.", true, "Not yet.", false)
                if (ready) {
                    trial()
                } else {
                    chatPlayer(neutral, "Not yet.")
                    chatNpc(neutral, "Take your time. Better a late verdict than a wrong one.")
                }
            }
            else ->
                chatNpc(
                    happy,
                    "Order in the court! ...Ah, it's you. The court remains in your debt, adventurer.",
                )
        }
    }

    private suspend fun Dialogue.arthurOffer() {
        chatNpc(
            neutral,
            "Ah, an adventurer. You've arrived at a troubled time. Order in the court, if you please.",
        )
        chatPlayer(quiz, "What's happened?")
        chatNpc(
            sad,
            "Last night, someone poisoned the ale at the Forester's Arms. Half the village spent " +
                "the night groaning in bed. Myself included, I'm ashamed to say.",
        )
        chatNpc(
            neutral,
            "The village has already decided who did it. The Poison Salesman. Who else sells " +
                "poison in Seers?",
        )
        chatNpc(
            worried,
            "But a king cannot judge on rumours alone. The man swears he's innocent, and " +
                "something about this case doesn't sit right with me.",
        )
        chatNpc(
            quiz,
            "Will you investigate before the trial begins? Speak to the Bartender, the accused, " +
                "anyone who might know something. Then come back and present your findings to " +
                "this court.",
        )
        val accept =
            choice2(
                "I'll find out the truth.",
                true,
                "Sounds like the Poison Salesman did it.",
                false,
            )
        if (!accept) {
            chatPlayer(neutral, "Sounds like the Poison Salesman did it.")
            chatNpc(
                worried,
                "That's what everyone says. That's exactly what worries me. Come back if you'd " +
                    "like to help.",
            )
            return
        }
        chatPlayer(happy, "I'll find out the truth.")
        chatNpc(
            happy,
            "Excellent. Start at the Forester's Arms. The Bartender found something by the barrel " +
                "this morning.",
        )
        quest.setQuestStage(access, STAGE_INVESTIGATING)
    }

    private suspend fun Dialogue.trial() {
        chatNpc(neutral, "Order in the court! The court will now hear the case of the poisoned ale.")
        chatNpc(quiz, "Adventurer, who do you accuse?")
        val suspect =
            choice3(
                "The Poison Salesman.",
                Suspect.Salesman,
                "The Bartender.",
                Suspect.Bartender,
                "The Flax keeper.",
                Suspect.FlaxKeeper,
            )
        if (suspect != Suspect.FlaxKeeper) {
            wrongVerdict()
            return
        }
        chatPlayer(neutral, "I accuse the Flax keeper.")
        mesbox("A murmur runs through the courtroom.")
        chatNpc(shocked, "The Flax keeper? A serious accusation. What evidence do you bring?")

        val firstEvidence =
            choice3(
                "The flax found by the poisoned barrel.",
                Evidence.Flax,
                "The Seer's vision.",
                Evidence.Vision,
                "I have no evidence, it's just a feeling.",
                Evidence.Feeling,
            )
        when (firstEvidence) {
            Evidence.Vision -> {
                chatPlayer(neutral, "The Seer foresaw the culprit in a vision.")
                chatNpc(
                    angry,
                    "This court does not accept prophecies. If it did, every case would be " +
                        "decided before the crime was committed.",
                )
                wrongVerdict()
                return
            }
            Evidence.Feeling -> {
                wrongVerdict()
                return
            }
            else -> {}
        }
        if (!player.foundFlax || !access.carries(FLAX)) {
            noEvidence()
            return
        }
        chatPlayer(neutral, "The Bartender found this flax right next to the poisoned barrel.")
        chatNpc(
            quiz,
            "Flax... in a tavern cellar. Curious. But anyone could have walked through a flax " +
                "field. Do you have more?",
        )

        val decisive =
            choice2(
                "This half-empty vial of weapon poison.",
                true,
                "The Flax keeper looked nervous.",
                false,
            )
        if (!decisive) {
            chatNpc(
                laugh,
                "Everyone looks nervous in a courtroom. I look nervous in a courtroom, and I'm the " +
                    "judge.",
            )
            wrongVerdict()
            return
        }
        if (!player.foundPoison || !access.carries(WEAPON_POISON)) {
            noEvidence()
            return
        }
        rightVerdict()
    }

    private suspend fun Dialogue.rightVerdict() {
        chatPlayer(
            neutral,
            "I found this vial of weapon poison hidden in the bales of flax beside the Flax " +
                "keeper's house.",
        )
        chatPlayer(
            neutral,
            "And the Poison Salesman told me he sold exactly one vial yesterday. To a customer who " +
                "smelled of flax.",
        )
        chatNpc(neutral, "...Flax keeper. Step forward. What do you have to say?")
        chatNpcSpecific(
            "Flax keeper",
            FLAX_KEEPER,
            angry,
            "...Fine! It was me! Every night those drunks stumble out of that tavern and straight " +
                "through my field. Trampling, singing, falling over. My whole harvest is ruined!",
        )
        chatNpcSpecific(
            "Flax keeper",
            FLAX_KEEPER,
            sad,
            "I only wanted the place closed. Nobody was supposed to get properly ill. Just... a " +
                "little ill.",
        )
        chatNpc(
            angry,
            "You poisoned half the village and let an innocent man take the blame. The court " +
                "finds you guilty.",
        )
        chatNpc(
            neutral,
            "Your sentence: you will work in the Forester's Arms, without pay, until the " +
                "Bartender is satisfied. And the Bartender will put up a fence around your field.",
        )
        chatNpc(neutral, "Poison Salesman, you are free to go. With the court's apologies.")
        chatNpc(
            happy,
            "As for you, adventurer... the court thanks you. Seers' Village sleeps safely tonight " +
                "because of you.",
        )
        access.invDel(access.inv, WEAPON_POISON)
        access.invAddOrDrop(objRepo, COINS, COIN_REWARD)
        access.invAddOrDrop(objRepo, LAMP)
        quest.completeQuest(access)
    }

    private suspend fun Dialogue.noEvidence() {
        chatNpc(
            angry,
            "You speak of evidence you don't have with you. The court cannot judge on words alone.",
        )
        chatNpc(neutral, "Bring the evidence and try again.")
    }

    private suspend fun Dialogue.wrongVerdict() {
        chatNpc(angry, "That isn't nearly enough to convict anyone. The court is adjourned.")
        chatNpc(neutral, "Investigate further, adventurer. And next time, bring me facts.")
    }

    private suspend fun Dialogue.bartender() {
        when (stage(player)) {
            STAGE_INVESTIGATING ->
                when {
                    !player.talkedBartender -> {
                        chatNpc(worried, "Don't order the ale. Seriously. Don't.")
                        chatPlayer(neutral, "I'm investigating the poisoning for King Arthur.")
                        chatNpc(
                            neutral,
                            "Finally, someone with sense. Everyone says it was the Poison " +
                                "Salesman, but he never even comes in here. Says the ale's too weak.",
                        )
                        chatNpc(
                            quiz,
                            "This morning I found something odd by the barrel in the cellar. " +
                                "Strands of flax. Lots of them. Nobody brings flax into a tavern.",
                        )
                        chatNpc(neutral, "Here, take them. Maybe they'll mean something to you.")
                        access.invAddOrDrop(objRepo, FLAX)
                        player.talkedBartender = true
                        player.foundFlax = true
                    }
                    !access.ownsAnywhere(FLAX) -> {
                        chatNpc(
                            neutral,
                            "Lost it? There's more where that came from. The cellar's still full " +
                                "of the stuff. Here.",
                        )
                        access.invAddOrDrop(objRepo, FLAX)
                    }
                    else ->
                        chatNpc(
                            quiz,
                            "Found out who did it yet? I'd like to know who I'm banning for life.",
                        )
                }
            STAGE_COMPLETE ->
                chatNpc(
                    laugh,
                    "The Flax keeper is mopping my floors as we speak. Best employee I've ever " +
                        "had. Doesn't get paid, never complains.",
                )
            else -> chatNpc(neutral, "What can I get you?")
        }
    }

    private suspend fun Dialogue.salesman() {
        when (stage(player)) {
            STAGE_INVESTIGATING ->
                if (!player.talkedSalesman) {
                    chatNpc(
                        angry,
                        "I didn't do it! I sell poison to adventurers, for their weapons! I don't " +
                            "go around pouring it into people's ale!",
                    )
                    chatPlayer(neutral, "I believe you. Did anyone buy poison from you recently?")
                    chatNpc(
                        sad,
                        "Business has been terrible. I sold just one vial of weapon poison all " +
                            "week. Yesterday afternoon.",
                    )
                    chatPlayer(quiz, "Who bought it?")
                    chatNpc(
                        neutral,
                        "I didn't see his face, he kept his hood up. Said it was for rats. But I " +
                            "remember the smell. He smelled like a field. Like... flax.",
                    )
                    player.talkedSalesman = true
                } else {
                    chatNpc(
                        worried,
                        "One vial, yesterday afternoon, to a man who smelled of flax. That's all I " +
                            "know. Please, get me out of this.",
                    )
                }
            STAGE_COMPLETE ->
                chatNpc(
                    happy,
                    "You saved my business! And my neck. If you ever need poison... for your " +
                        "weapons, of course... come to me.",
                )
            else -> chatNpc(neutral, "Poison for your weapons, adventurer? Best in the land.")
        }
    }

    private suspend fun Dialogue.flaxKeeper() {
        when (stage(player)) {
            STAGE_INVESTIGATING ->
                if (!player.talkedFlaxKeeper) {
                    chatNpc(
                        worried,
                        "Hmm? The poisoning? Terrible business. Just terrible. I was home all " +
                            "evening. Alone. With my flax.",
                    )
                    chatPlayer(quiz, "Did you go near the Forester's Arms last night?")
                    chatNpc(
                        worried,
                        "The tavern? Never. I hate that place. Well, I don't hate it. I just... " +
                            "never go there. Why would I? Ha. Ha.",
                    )
                    chatNpc(neutral, "Anyway, I have a lot of flax to keep. Goodbye.")
                    player.talkedFlaxKeeper = true
                } else {
                    chatNpc(
                        worried,
                        "I told you, I was home all evening. Please stop looking at my flax bales " +
                            "like that.",
                    )
                }
            STAGE_COMPLETE -> chatNpc(angry, "Don't talk to me. I have to go mop a tavern.")
            else -> chatNpc(neutral, "Hello. Lovely flax this year.")
        }
    }

    private suspend fun Dialogue.seer() {
        when (stage(player)) {
            STAGE_INVESTIGATING -> {
                chatNpc(neutral, "I saw it coming, you know. In a vision.")
                chatPlayer(quiz, "You saw who poisoned the ale?")
                chatNpc(
                    neutral,
                    "I saw a man. Or possibly a large bird. Surrounded by... something yellow. Or " +
                        "brown. Long and stringy.",
                )
                chatNpc(
                    sad,
                    "I also saw myself drinking the ale anyway. Visions aren't always useful.",
                )
                player.talkedSeer = true
            }
            STAGE_COMPLETE ->
                chatNpc(happy, "I knew you'd solve it. I saw it in a vision. After you solved it.")
            else -> chatNpc(neutral, "I foresee... that you will walk away soon.")
        }
    }

    private suspend fun ProtectedAccess.searchBales() {
        val stage = quest.getQuestStage(player)
        when {
            stage == STAGE_INVESTIGATING && player.talkedSalesman && !player.foundPoison -> {
                mesbox("You search through the bales of flax...")
                mesbox("Hidden deep inside, you find a half-empty vial of weapon poison!")
                invAddOrDrop(objRepo, WEAPON_POISON)
                player.foundPoison = true
            }
            stage == STAGE_INVESTIGATING && player.foundPoison && !ownsAnywhere(WEAPON_POISON) -> {
                mesbox(
                    "You search the bales again and find another vial of weapon poison. The Flax " +
                        "keeper must have bought more than one."
                )
                invAddOrDrop(objRepo, WEAPON_POISON)
            }
            else -> mesbox("Just bales of flax. Lots and lots of flax.")
        }
    }

    private suspend fun ProtectedAccess.rubLamp() {
        val stat = chooseLampSkill("Choose the stat you wish to be advanced!") ?: return
        if (invDel(inv, LAMP).failure) {
            return
        }
        statAdvance(stat, LAMP_XP)
        mes("Your wish has been granted!")
    }

    private enum class Suspect {
        Salesman,
        Bartender,
        FlaxKeeper,
    }

    private enum class Evidence {
        Flax,
        Vision,
        Feeling,
    }

    companion object {
        const val QUEST_KEY = "quest_order_in_the_court"

        const val STAGE_NOT_STARTED = 0
        const val STAGE_INVESTIGATING = 1
        const val STAGE_COMPLETE = 2

        const val COIN_REWARD = 5000
        const val LAMP_XP = 2500.0

        private const val KING_ARTHUR = "npc.king_arthur"
        private const val BARTENDER = "npc.foresters_bartender"
        private const val POISON_SALESMAN = "npc.poison_salesman"
        private const val FLAX_KEEPER = "npc.diary_flax_keeper"
        private const val SEER = "npc.seer"

        private const val FLAX = "obj.flax"
        private const val WEAPON_POISON = "obj.weapon_poison"
        private const val COINS = "obj.coins"
        const val LAMP = "obj.thosf_reward_lamp"
    }
}
