package org.rsmod.content.dodian.quest.silverblade

import dev.openrune.ServerCacheManager
import dev.openrune.rscm.RSCM.asRSCM
import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.righthand
import org.rsmod.api.player.vars.boolVarBit
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.player.worn.EquipGates
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onAiTimer
import org.rsmod.api.script.onModifyNpcHit
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.content.dodian.quest.common.QuestFoes
import org.rsmod.content.dodian.quest.common.SharedTalk.onSharedTalk
import org.rsmod.content.dodian.quest.common.carries
import org.rsmod.content.dodian.quest.common.carriesOrWears
import org.rsmod.content.dodian.quest.common.ownsAnywhere
import org.rsmod.content.dodian.quest.common.removeEverywhere
import org.rsmod.content.dodian.quest.common.takeUpTo
import org.rsmod.content.dodian.quest.darkenedshield.TheDarkenedShield
import org.rsmod.content.dodian.quest.forgottenaltar.TheForgottenAltar
import org.rsmod.content.quest.manager.ItemRewardDisplay
import org.rsmod.content.quest.manager.Quest
import org.rsmod.content.quest.manager.QuestScript
import org.rsmod.content.quest.manager.rewards
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList
import org.rsmod.game.entity.player.PlayerUid
import org.rsmod.game.hit.HitType
import org.rsmod.plugin.scripts.ScriptContext

class TheSilverBlade
@Inject
constructor(
    private val objRepo: ObjRepository,
    private val shops: Shops,
    private val foes: QuestFoes,
    private val launcher: ProtectedAccessLauncher,
    private val playerList: PlayerList,
) :
    QuestScript(
        QUEST_KEY,
        "varp.dodian_silver_blade",
        rewards {
            xp("stat.strength", STRENGTH_XP)
            xp("stat.defence", DEFENCE_XP)
            extra("Ability to wear rune platebodies")
            extra("Ability to wear green d'hide bodies")
        },
        ItemRewardDisplay(SILVERLIGHT, zoom = 600),
        questVarbit = "varbit.dodian_silver_blade_progress",
    ) {

    private var Player.traibornBones by intVarBit("varbit.dodian_silver_blade_traiborn_bones")
    private var Player.traibornKeyGiven by boolVarBit("varbit.dodian_silver_blade_traiborn_key")
    private var Player.rovinKeyGiven by boolVarBit("varbit.dodian_silver_blade_rovin_key")

    private val darkenedShield: Quest? by lazy { Quest.get(TheDarkenedShield.QUEST_KEY) }
    private val forgottenAltar: Quest? by lazy { Quest.get(TheForgottenAltar.QUEST_KEY) }

    override fun ScriptContext.init() {
        check(quest.maxSteps == STAGE_COMPLETE) {
            "The Silver Blade end state is ${quest.maxSteps} in .data/dodian/quests.toml, but the " +
                "quest completes at stage $STAGE_COMPLETE."
        }
        EquipGates.register { player, type ->
            val gated = type.id == RUNE_PLATEBODY.asRSCM() ||
                (GREEN_DHIDE_REQUIRES_QUEST && type.id == GREEN_DHIDE_BODY.asRSCM())
            if (gated && !quest.isQuestCompleted(player)) {
                "You need to complete The Silver Blade to wear this."
            } else {
                null
            }
        }
        onOpNpc1(OZIACH) { startDialogue(it.npc) { oziach(it.npc) } }
        onOpNpc3(OZIACH) { oziachTrade(it.npc) }
        onOpNpc1(PRYSIN) { startDialogue(it.npc) { prysin() } }
        onOpNpc1(ROVIN) { startDialogue(it.npc) { rovin() } }
        onSharedTalk(TRAIBORN, priority = 10) { traiborn() }
        onSharedTalk(TheDarkenedShield.VIYELDI, priority = 10) { viyeldi(npc) }

        onAiTimer(DELRITH) { foes.tick(npc) }
        onAiTimer(DELRITH_WEAKENED) { weakenedTick(npc) }
        val delrithType = checkNotNull(ServerCacheManager.getNpc(DELRITH.asRSCM()))
        onModifyNpcHit(delrithType) {
            if (!hit.isFromPlayer) {
                return@onModifyNpcHit
            }
            val attacker = hit.sourceSlot?.let { playerList[it] } ?: return@onModifyNpcHit
            val silverlight =
                hit.type == HitType.Melee && attacker.righthand?.id == SILVERLIGHT.asRSCM()
            if (!silverlight) {
                hit.damage = 0
                attacker.mes("Your attack doesn't seem to hurt Delrith at all.")
            }
        }
        foes.onKill { npc, killer -> if (npc.isType(DELRITH)) delrithWeakened(npc, killer) }
    }

    override fun subTitle(): String =
        "speaking to <col=800000>Oziach</col>. I must have completed <col=800000>The Darkened " +
            "Shield</col>."

    override fun questLog(player: ProtectedAccess): String =
        questJournal(player) {
            val p = player.player
            when (quest.getQuestStage(p)) {
                STAGE_STARTED ->
                    line(
                        "Viyeldi has summoned the demon <red>Delrith</red>. Only " +
                            "<red>Silverlight</red> can harm it. I should speak to <red>Sir " +
                            "Prysin</red>."
                    )
                STAGE_KEYS -> {
                    line(
                        "Sir Prysin keeps Silverlight in a chest with three locks. I need the keys " +
                            "from:"
                    )
                    if (p.traibornKeyGiven) strike("Wizard Traiborn") else line("<red>Wizard Traiborn</red>")
                    if (p.rovinKeyGiven) strike("Captain Rovin") else line("<red>Captain Rovin</red>")
                    if (!p.traibornKeyGiven) {
                        line(
                            "Traiborn needs <red>$BONES_NEEDED bones</red> to summon his key. I have " +
                                "given him <red>${p.traibornBones}/$BONES_NEEDED</red>."
                        )
                    }
                    line("The incantation is: <red>$INCANTATION_TEXT</red>.")
                }
                STAGE_SILVERLIGHT ->
                    line(
                        "I have <red>Silverlight</red>. Delrith is on the <red>hills east of the " +
                            "Fishing Guild</red>. When it falls, I must speak the incantation: " +
                            "<red>$INCANTATION_TEXT</red>."
                    )
                else -> line("Delrith has been banished. I should return to <red>Oziach</red>.")
            }
        }

    override fun completedLog(player: ProtectedAccess): String =
        completionJournal(player) {
            line(
                "Oziach trusts me now. I can wear rune platebodies and green d'hide bodies."
            )
        }

    private fun stage(player: Player): Int = quest.getQuestStage(player)

    private suspend fun ProtectedAccess.oziachTrade(npc: Npc) {
        if (quest.isQuestCompleted(player)) {
            shops.open(player, npc, "Oziach's Armour", OZIACH_SHOP)
        } else {
            startDialogue(npc) { oziachRefuses() }
        }
    }

    private suspend fun Dialogue.oziachRefuses() {
        chatNpc(
            neutral,
            "Aye, I've got a rune platebody. Finest armour money can buy. And no, it's not for " +
                "sale. Not to the likes of you.",
        )
        chatNpc(neutral, "Come back when you've done something worth telling.")
    }

    private suspend fun Dialogue.oziach(npc: Npc) {
        when (stage(player)) {
            STAGE_NOT_STARTED ->
                if (darkenedShield?.isQuestCompleted(player) == true) {
                    oziachOffer()
                } else {
                    oziachRefuses()
                }
            in STAGE_STARTED..STAGE_SILVERLIGHT ->
                chatNpc(
                    angry,
                    "Is Delrith banished yet? No? Then stop wasting my time. Sir Prysin has the " +
                        "sword, and the demon is on the hills east of the Fishing Guild.",
                )
            STAGE_BANISHED -> oziachComplete()
            else -> {
                chatNpc(
                    happy,
                    "Back for more armour? Good. Heroes get through it faster than anyone.",
                )
                shops.open(player, npc, "Oziach's Armour", OZIACH_SHOP)
            }
        }
    }

    private suspend fun Dialogue.oziachOffer() {
        chatNpc(
            neutral,
            "You again? I've heard the stories. A demon slain at the Watchtower. Not bad.",
        )
        chatPlayer(quiz, "I was hoping to buy your rune platebody.")
        chatNpc(
            laugh,
            "Hah! One demon and you think you're a hero. I've slain more beasts than you've had " +
                "hot dinners.",
        )
        chatNpc(
            neutral,
            "Still... there is something you could do. That dark wizard you chased off, Viyeldi. " +
                "He's back. Same hills, east of the Fishing Guild.",
        )
        chatNpc(
            worried,
            "And this time he hasn't summoned some lesser demon. He's brought back Delrith. An " +
                "ancient thing. It took a legendary sword to banish it the last time.",
        )
        chatNpc(
            neutral,
            "Banish Delrith, and I'll sell you the armour. I might even let you wear it in my " +
                "presence.",
        )
        val accept =
            choice2(
                "I'll deal with Delrith.",
                true,
                "That sounds like a job for someone else.",
                false,
            )
        if (!accept) {
            chatPlayer(neutral, "That sounds like a job for someone else.")
            chatNpc(neutral, "That's what they all say. That's why I still have the armour.")
            return
        }
        chatPlayer(
            neutral,
            "I'll deal with Delrith. Where do I find that legendary sword?",
        )
        chatNpc(
            neutral,
            "Silverlight. Sir Prysin keeps it. His ancestor Wally used it to banish Delrith " +
                "centuries ago.",
        )
        chatNpc(
            angry,
            "Don't even think about going after that demon with an ordinary weapon. It'll laugh " +
                "at you. Then it'll eat you.",
        )
        quest.setQuestStage(access, STAGE_STARTED)
    }

    private suspend fun Dialogue.oziachComplete() {
        chatPlayer(neutral, "Delrith is banished. Viyeldi ran off again.")
        chatNpc(
            shocked,
            "...You're not lying, are you? I can see it in your face. You've looked into the eyes " +
                "of something old.",
        )
        chatNpc(
            neutral,
            "Fine. A deal's a deal. From now on, the rune platebody is yours to buy, and to wear.",
        )
        chatNpc(
            neutral,
            "And you can wear green dragonhide too, while you're at it. Anyone who can face " +
                "Delrith can handle a bit of leather.",
        )
        chatNpc(
            neutral,
            "Keep that sword close. Wizards like Viyeldi never stay gone for long.",
        )
        quest.completeQuest(access)
    }

    private suspend fun Dialogue.prysin() {
        when (stage(player)) {
            STAGE_NOT_STARTED -> chatNpc(neutral, "Good day. Forgive me, I'm rather busy.")
            STAGE_STARTED -> prysinExplains()
            STAGE_KEYS -> prysinKeys()
            STAGE_SILVERLIGHT ->
                if (access.ownsAnywhere(SILVERLIGHT)) {
                    chatNpc(
                        neutral,
                        "Go, before Delrith grows stronger. $INCANTATION_TEXT.",
                    )
                } else {
                    chatNpc(
                        shocked,
                        "You lost Silverlight?! ...Thankfully, the sword always finds its way back " +
                            "to my family. I found it in the chest this morning. Here.",
                    )
                    access.invAddOrDrop(objRepo, SILVERLIGHT)
                }
            else -> chatNpc(happy, "Wally would be proud. Keep Silverlight. It has chosen you now.")
        }
    }

    private suspend fun Dialogue.prysinExplains() {
        chatPlayer(neutral, "Oziach sent me. Delrith has returned, and I need Silverlight.")
        chatNpc(
            shocked,
            "Delrith? Back? By Saradomin... my ancestor Wally gave his life's work to banish that " +
                "creature.",
        )
        chatNpc(
            neutral,
            "Silverlight is the only weapon that can harm it. I keep it locked in a chest with " +
                "three locks, so that no single person can ever take it.",
        )
        chatPlayer(neutral, "Then open it, please.")
        chatNpc(
            sad,
            "If only it were that easy. I have one key. The other two I gave to two old friends, " +
                "for safekeeping.",
        )
        chatNpc(
            neutral,
            "Wizard Traiborn has the second. He lives next to the air ruins. Brilliant man, " +
                "though... a little forgetful.",
        )
        chatNpc(
            neutral,
            "Captain Rovin has the third. He's as stubborn as a mule, so mention my name and tell " +
                "him exactly why you need it.",
        )
        chatNpc(neutral, "Bring me both keys and I'll open the chest.")
        chatNpc(
            worried,
            "And remember Wally's words. When Delrith falls, you must speak the incantation, or it " +
                "will simply rise again: Carlem... Aber... Camerinthum... Purchai... Gabindo.",
        )
        chatPlayer(confused, "Could you say that one more time?")
        chatNpc(
            neutral,
            "Carlem, Aber, Camerinthum, Purchai, Gabindo. In that order. Get it wrong and you'll be " +
                "fighting it all night.",
        )
        player.traibornBones = 0
        quest.setQuestStage(access, STAGE_KEYS)
    }

    private suspend fun Dialogue.prysinKeys() {
        val hasTraibornKey = access.carries(TRAIBORN_KEY)
        val hasRovinKey = access.carries(ROVIN_KEY)
        if (hasTraibornKey && hasRovinKey) {
            chatPlayer(happy, "I have both keys.")
            chatNpc(happy, "Wonderful! Let me add mine...")
            mesbox("Sir Prysin turns all three keys at once. The chest creaks open.")
            access.removeEverywhere(TRAIBORN_KEY)
            access.removeEverywhere(ROVIN_KEY)
            access.invAddOrDrop(objRepo, SILVERLIGHT)
            quest.setQuestStage(access, STAGE_SILVERLIGHT)
            chatNpc(
                happy,
                "Silverlight. It hasn't seen daylight in years. It will glow when you face evil.",
            )
            chatNpc(neutral, "Go. And don't forget the words: $INCANTATION_TEXT.")
            return
        }
        if (!hasTraibornKey) {
            chatNpc(neutral, "I still need the key from Wizard Traiborn, next to the air ruins.")
        }
        if (!hasRovinKey) {
            chatNpc(neutral, "I still need the key from Captain Rovin. Mention my name.")
        }
        chatNpc(neutral, "The incantation, in case you've forgotten: $INCANTATION_TEXT.")
    }

    /** Traiborn's Silver Blade lines; his Forgotten Altar dialogue keeps priority while it runs. */
    private suspend fun Dialogue.traiborn(): Boolean {
        val stage = stage(player)
        val altarInProgress = forgottenAltar?.questState(player)?.isInProgress == true
        when {
            stage == STAGE_KEYS -> traibornKeys()
            stage >= STAGE_SILVERLIGHT && !altarInProgress ->
                chatNpc(
                    happy,
                    "Silverlight! I remember that sword. Or I think I do. Shiny thing, isn't it?",
                )
            else -> return false
        }
        return true
    }

    private suspend fun Dialogue.traibornKeys() {
        if (player.traibornKeyGiven) {
            if (access.ownsAnywhere(TRAIBORN_KEY)) {
                chatNpc(
                    happy,
                    "Take the key to Prysin. And say hello to him from me. Or did I already do that?",
                )
            } else {
                chatNpc(
                    happy,
                    "Lost it already? Don't worry, I made a spare. I keep it... in my hat.",
                )
                access.invAddOrDrop(objRepo, TRAIBORN_KEY)
            }
            return
        }
        if (player.traibornBones == 0) {
            chatPlayer(neutral, "Sir Prysin says you have one of the Silverlight keys.")
            chatNpc(
                confused,
                "A key? Did I? ...Oh yes! Prysin's key! I put it somewhere very safe.",
            )
            chatNpc(confused, "So safe that I can't remember where. Typical.")
            chatNpc(
                neutral,
                "No matter, I can summon it back. I'll just need some bones for the spell. " +
                    "Twenty-five should do it.",
            )
            chatPlayer(shocked, "Twenty-five bones?")
            chatNpc(
                happy,
                "It's a very old spell. Old spells are hungry. You can bring them in a few at a " +
                    "time, I'll keep count. I'll write it down this time. Probably.",
            )
            takeBones()
        } else {
            if (access.carries(BONES)) {
                chatNpc(happy, "Bones! Lovely. Hand them over.")
                takeBones()
            }
        }
        if (player.traibornBones >= BONES_NEEDED) {
            chatNpc(
                happy,
                "Twenty-five! Now stand back... Bones of old, bring back what's lost...",
            )
            mesbox("There is a puff of smoke. A small silver key falls out of Traiborn's hat.")
            chatNpc(happy, "My hat! Of course. It's always in my hat. Here you go.")
            access.invAddOrDrop(objRepo, TRAIBORN_KEY)
            player.traibornKeyGiven = true
            return
        }
        if (player.traibornBones > 0) {
            val remaining = BONES_NEEDED - player.traibornBones
            chatNpc(
                confused,
                "That's ${player.traibornBones} so far. Only $remaining to go. Or was it more? No, " +
                    "I wrote it down. On my hand.",
            )
        }
    }

    private fun Dialogue.takeBones() {
        player.traibornBones += access.takeUpTo(BONES, BONES_NEEDED - player.traibornBones)
    }

    private suspend fun Dialogue.rovin() {
        when (stage(player)) {
            STAGE_NOT_STARTED,
            STAGE_STARTED ->
                chatNpc(neutral, "Move along, citizen. I have important matters to attend to.")
            STAGE_KEYS ->
                when {
                    !player.rovinKeyGiven -> rovinQuestions()
                    !access.ownsAnywhere(ROVIN_KEY) -> {
                        chatNpc(
                            shocked,
                            "You lost it? Prysin's key? ...Thankfully, I had a copy made. Don't " +
                                "tell him. Here.",
                        )
                        access.invAddOrDrop(objRepo, ROVIN_KEY)
                    }
                    else -> chatNpc(bored, "You have the key. What are you still doing here?")
                }
            else ->
                chatNpc(
                    neutral,
                    "So you're the one who faced Delrith. Hmph. Not bad, citizen. Not bad at all.",
                )
        }
    }

    private suspend fun Dialogue.rovinQuestions() {
        chatPlayer(neutral, "I need the Silverlight key Sir Prysin gave you.")
        chatNpc(
            angry,
            "The key? Absolutely not. Do you know how many people have tried to talk me out of " +
                "that key? Dozens. Liars, every one of them.",
        )
        chatNpc(neutral, "Tell me why you need it. And it had better be good.")
        val reason =
            choice3(
                "Delrith has returned, and Sir Prysin sent me to banish it.",
                1,
                "I want to sell the sword.",
                2,
                "I'm a collector of shiny keys.",
                3,
            )
        when (reason) {
            1 -> {
                chatPlayer(neutral, "Delrith has returned, and Sir Prysin sent me to banish it.")
                chatNpc(
                    shocked,
                    "Delrith? ...Prysin only ever told one person what that key was for. Me. And " +
                        "now you know too, so he must have told you.",
                )
                chatNpc(
                    neutral,
                    "Very well. Take it. And if you get yourself killed, don't come crying to me.",
                )
                access.invAddOrDrop(objRepo, ROVIN_KEY)
                player.rovinKeyGiven = true
            }
            2 -> {
                chatPlayer(neutral, "I want to sell the sword.")
                chatNpc(angry, "At least you're honest. Honestly stupid. Get out.")
            }
            else -> {
                chatPlayer(neutral, "I'm a collector of shiny keys.")
                chatNpc(angry, "And I'm the Queen of Karamja. No.")
            }
        }
    }

    private suspend fun Dialogue.viyeldi(viyeldi: Npc?): Boolean {
        when (stage(player)) {
            STAGE_STARTED,
            STAGE_KEYS -> {
                chatNpc(
                    angry,
                    "You again! Come to steal another crystal? There's nothing for you here. Not " +
                        "yet.",
                )
                chatNpc(laugh, "Come back with a real weapon. If you can find one.")
            }
            STAGE_SILVERLIGHT ->
                when {
                    foes.active(player, DELRITH, DELRITH_WEAKENED) != null ->
                        chatNpc(laugh, "Ha! Delrith will finish you off.")
                    !access.carriesOrWears(SILVERLIGHT) ->
                        chatNpc(
                            laugh,
                            "No sword? No chance. Delrith would eat you before breakfast.",
                        )
                    player.uid in summonedBefore -> {
                        chatNpc(madlaugh, "Back for more? DELRITH! Again!")
                        summonDelrith(viyeldi)
                    }
                    else -> {
                        chatNpc(
                            shocked,
                            "Silverlight?! Where did you... no matter. Not even that old blade will " +
                                "save you.",
                        )
                        chatNpc(madlaugh, "DELRITH! Rise from the dark and tear this fool apart!")
                        mesbox("Viyeldi vanishes in a cloud of black smoke, laughing.")
                        summonDelrith(viyeldi)
                    }
                }
            STAGE_BANISHED ->
                chatNpc(
                    angry,
                    "You've won again... but every door you close, I'll open two more.",
                )
            else -> return false
        }
        return true
    }

    /** Players who already summoned Delrith since the server started, for Viyeldi's taunt. */
    private val summonedBefore = mutableSetOf<PlayerUid>()

    private fun Dialogue.summonDelrith(viyeldi: Npc?) {
        summonedBefore += player.uid
        val anchor = viyeldi?.coords ?: player.coords
        access.ifClose()
        foes.spawn(player, DELRITH, anchor.translateX(1), anchor, attack = true)
    }

    private fun delrithWeakened(npc: Npc, killer: Player) {
        if (stage(killer) != STAGE_SILVERLIGHT) {
            return
        }
        val weakened = foes.spawn(killer, DELRITH_WEAKENED, npc.coords, npc.coords, attack = false)
        launcher.launch(killer) {
            mes("Delrith is weakened. Now is the time to speak the incantation!")
            startDialogue(weakened) { incantation(weakened) }
        }
    }

    private suspend fun Dialogue.incantation(weakened: Npc) {
        val spoken = mutableListOf<String>()
        for (expected in INCANTATION) {
            val words = INCANTATION.shuffled()
            val word =
                choice5(
                    words[0], words[0],
                    words[1], words[1],
                    words[2], words[2],
                    words[3], words[3],
                    words[4], words[4],
                    title = "Speak the incantation",
                )
            spoken += word
            if (word != expected) {
                chatPlayer(angry, spoken.joinToString("... ") + "...")
                mesbox("Delrith roars and rises again!")
                reviveDelrith(weakened)
                return
            }
        }
        if (!foes.isFoe(weakened)) {
            return
        }
        chatPlayer(angry, "Carlem... Aber... Camerinthum... Purchai... Gabindo!")
        mesbox(
            "Delrith screams. A white light pours out of Silverlight, and the demon is pulled back " +
                "into the void."
        )
        foes.despawn(weakened)
        quest.setQuestStage(access, STAGE_BANISHED)
    }

    private fun weakenedTick(npc: Npc) {
        if (foes.ageCycles(npc) < WEAKENED_CYCLES) {
            foes.tick(npc)
            return
        }
        reviveDelrith(npc)
    }

    private fun reviveDelrith(weakened: Npc) {
        val owner = foes.owner(weakened)
        if (!foes.isFoe(weakened)) {
            return
        }
        foes.despawn(weakened)
        if (owner == null) {
            return
        }
        val delrith = foes.spawn(owner, DELRITH, weakened.coords, weakened.coords, attack = true)
        delrith.hitpoints = (delrith.baseHitpointsLvl * REVIVE_HITPOINTS_FRACTION).toInt().coerceAtLeast(1)
    }

    companion object {
        const val QUEST_KEY = "quest_silver_blade"

        const val STAGE_NOT_STARTED = 0
        const val STAGE_STARTED = 1
        const val STAGE_KEYS = 2
        const val STAGE_SILVERLIGHT = 3
        const val STAGE_BANISHED = 4
        const val STAGE_COMPLETE = 5

        const val STRENGTH_XP = 10000.0
        const val DEFENCE_XP = 10000.0
        const val GREEN_DHIDE_REQUIRES_QUEST = true

        private const val BONES_NEEDED = 25
        private const val WEAKENED_CYCLES = 50
        private const val REVIVE_HITPOINTS_FRACTION = 0.25

        private val INCANTATION = listOf("Carlem", "Aber", "Camerinthum", "Purchai", "Gabindo")
        private val INCANTATION_TEXT = INCANTATION.joinToString(", ")

        private const val OZIACH = "npc.oziach"
        private const val PRYSIN = "npc.sir_prysin"
        private const val ROVIN = "npc.captain_rovin"
        private const val TRAIBORN = "npc.traiborn"
        private const val DELRITH = "npc.delrith"
        private const val DELRITH_WEAKENED = "npc.delrith_weakened"
        private const val OZIACH_SHOP = "inv.runeplateshop"

        private const val SILVERLIGHT = "obj.silverlight"
        private const val TRAIBORN_KEY = "obj.silverlight_key_2"
        private const val ROVIN_KEY = "obj.silverlight_key_3"
        private const val BONES = "obj.bones"
        private const val RUNE_PLATEBODY = "obj.rune_platebody"
        private const val GREEN_DHIDE_BODY = "obj.dragonhide_body"
    }
}
