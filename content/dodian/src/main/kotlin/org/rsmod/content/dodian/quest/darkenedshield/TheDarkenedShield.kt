package org.rsmod.content.dodian.quest.darkenedshield

import dev.openrune.rscm.RSCM.asRSCM
import jakarta.inject.Inject
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onAiTimer
import org.rsmod.api.script.onOpHeld1
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLocU
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.dodian.isOnDoorTileSide
import org.rsmod.content.dodian.quest.common.DodianQuestLocs
import org.rsmod.content.dodian.quest.common.QuestFoes
import org.rsmod.content.dodian.quest.common.SharedTalk.onSharedTalk
import org.rsmod.content.dodian.quest.common.carries
import org.rsmod.content.dodian.quest.common.ownsAnywhere
import org.rsmod.content.dodian.swingThrough
import org.rsmod.content.generic.locs.doors.DoorTranslations
import org.rsmod.content.quest.manager.ItemRewardDisplay
import org.rsmod.content.quest.manager.QuestScript
import org.rsmod.content.quest.manager.chooseLampSkill
import org.rsmod.content.quest.manager.rewards
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.plugin.scripts.ScriptContext

class TheDarkenedShield
@Inject
constructor(
    private val objRepo: ObjRepository,
    private val locRepo: LocRepository,
    private val foes: QuestFoes,
    private val launcher: ProtectedAccessLauncher,
) :
    QuestScript(
        QUEST_KEY,
        "varp.dodian_darkened_shield",
        rewards {
            extra("Access to the Legends' Guild")
            extra("$LAMP_REWARD x %,d XP lamps".format(LAMP_XP.toInt()))
        },
        ItemRewardDisplay(CYAN_CRYSTAL, zoom = 400),
        questVarbit = "varbit.dodian_darkened_shield_progress",
    ) {

    private val Player.questPoints by intVarp("varp.qp")

    override fun ScriptContext.init() {
        check(quest.maxSteps == STAGE_COMPLETE) {
            "The Darkened Shield end state is ${quest.maxSteps} in .data/dodian/quests.toml, but " +
                "the quest completes at stage $STAGE_COMPLETE."
        }
        onOpNpc1(RADIMUS) { startDialogue(it.npc) { radimus() } }
        onOpNpc1(WATCHTOWER_WIZARD) { startDialogue(it.npc) { wizard() } }
        onSharedTalk(VIYELDI, priority = 0) {
            viyeldi(npc)
            true
        }
        onAiTimer(NEZIKCHENED) { foes.tick(npc) }
        foes.onKill { npc, killer -> if (npc.isType(NEZIKCHENED)) nezikchenedSlain(killer) }
        for (altar in DodianQuestLocs.locs("darkened_shield_altars")) {
            onOpLocU(altar, GREY_CRYSTAL) { cleanseCrystal() }
        }
        for (gate in DodianQuestLocs.locs("darkened_shield_legends_gate")) {
            onOpLoc1(gate) { legendsGate(it.vis) }
        }
        onOpHeld1(LAMP) { rubLamp() }
    }

    override fun subTitle(): String =
        "speaking to <col=800000>Radimus Erkle</col> outside the <col=800000>Legends' " +
            "Guild</col>. Requirements: 10 Quest Points, 50 Attack, 50 Strength, 50 Defence, 42 " +
            "Prayer."

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
            line("I have become a Legend and may enter the Legends' Guild.")
        }

    private fun stage(player: Player): Int = quest.getQuestStage(player)

    private fun ProtectedAccess.missingRequirements(): List<String> = buildList {
        if (player.questPoints < REQUIRED_QP) add("$REQUIRED_QP Quest Points")
        for ((stat, name, level) in REQUIRED_STATS) {
            if (statBase(stat) < level) add("$level $name")
        }
    }

    private suspend fun Dialogue.radimus() {
        when (stage(player)) {
            STAGE_NOT_STARTED -> radimusOffer()
            in STAGE_STARTED..STAGE_CLEANSED ->
                chatNpc(
                    neutral,
                    "Have you helped the Watchtower yet? The tower stands north of Yanille. The " +
                        "longer you wait, the weaker that shield becomes.",
                )
            STAGE_RESTORED -> {
                chatNpc(
                    happy,
                    "Word travels fast. A demon slain, a dark wizard driven off, and the shield " +
                        "above Yanille shines as bright as ever.",
                )
                chatNpc(
                    neutral,
                    "You didn't come here asking to be called a legend. You went out and became one.",
                )
                chatNpc(
                    happy,
                    "The gate of the Legends' Guild is open to you. Welcome, Legend.",
                )
                access.invAddOrDrop(objRepo, LAMP, LAMP_REWARD)
                quest.completeQuest(access)
            }
            else -> chatNpc(happy, "Welcome back, Legend. The guild is yours to enter.")
        }
    }

    private suspend fun Dialogue.radimusOffer() {
        chatNpc(
            neutral,
            "Halt. Beyond this gate lies the Legends' Guild, and only true legends may enter.",
        )
        val missing = access.missingRequirements()
        if (missing.isNotEmpty()) {
            chatNpc(
                neutral,
                "You are not ready, adventurer. Come back when you have proven yourself.",
            )
            access.mes("You need ${missing.joinWithAnd()} to start this quest.")
            return
        }
        chatPlayer(quiz, "How do I become a legend?")
        chatNpc(
            neutral,
            "Not with words. Legends are made by deeds, the kind that people still tell stories " +
                "about long after you're gone.",
        )
        chatNpc(
            worried,
            "As it happens, I've received troubling news. The magical shield above Yanille has " +
                "been flickering. The Watchtower that sustains it is in trouble.",
        )
        chatNpc(
            worried,
            "If that shield falls, the ogres will overrun the city. Save Yanille, and the gate is " +
                "yours.",
        )
        val accept = choice2("I'll go to the Watchtower.", true, "Maybe another time.", false)
        if (!accept) {
            chatPlayer(neutral, "Maybe another time.")
            chatNpc(neutral, "Then the gate stays closed. I'll be here.")
            return
        }
        chatPlayer(neutral, "I'll go to the Watchtower.")
        chatNpc(
            neutral,
            "Good. The tower stands just north of Yanille. Speak to the wizard there.",
        )
        chatNpc(
            neutral,
            "And adventurer... come back alive. Legends are no use to anyone dead.",
        )
        quest.setQuestStage(access, STAGE_STARTED)
    }

    private suspend fun Dialogue.wizard() {
        when (stage(player)) {
            STAGE_NOT_STARTED ->
                chatNpc(neutral, "Hello there. The shield above Yanille needs my full attention.")
            STAGE_STARTED -> wizardExplains()
            STAGE_FIND_THIEF,
            STAGE_FIGHTING ->
                chatNpc(
                    quiz,
                    "Have you found my crystal? Look on the hills east of the Fishing Guild. " +
                        "That's where the dark glow comes from.",
                )
            STAGE_CURSED_CRYSTAL ->
                if (access.ownsAnywhere(GREY_CRYSTAL)) {
                    wizardSeesCurse()
                } else {
                    chatNpc(
                        neutral,
                        "You lost the crystal? Don't worry. The crystals are bound to this tower. " +
                            "Let me call it back...",
                    )
                    chatNpc(
                        sad,
                        "There. Still grey and lifeless, I'm afraid. Take it to an altar and pray " +
                            "over it.",
                    )
                    access.invAddOrDrop(objRepo, GREY_CRYSTAL)
                }
            STAGE_CLEANSED ->
                if (access.carries(CYAN_CRYSTAL)) {
                    wizardRestores()
                } else if (!access.ownsAnywhere(CYAN_CRYSTAL)) {
                    chatNpc(
                        neutral,
                        "You cleansed it, and then lost it? Hm. Luckily it's still bound to this " +
                            "tower. Here it is.",
                    )
                    access.invAddOrDrop(objRepo, CYAN_CRYSTAL)
                } else {
                    chatNpc(quiz, "Bring me the cleansed crystal, so I can put it back.")
                }
            STAGE_RESTORED ->
                chatNpc(
                    happy,
                    "The shield is holding perfectly. Go and see Radimus Erkle, you've earned it.",
                )
            else ->
                chatNpc(
                    happy,
                    "Ah, our hero! The ogres haven't dared come near Yanille since the shield was " +
                        "restored.",
                )
        }
    }

    private suspend fun Dialogue.wizardExplains() {
        chatPlayer(
            neutral,
            "Radimus Erkle sent me. He says the shield above Yanille is in trouble.",
        )
        chatNpc(
            shocked,
            "Trouble? It's a disaster! One of the four powering crystals is gone. Stolen, right " +
                "from under my nose!",
        )
        chatNpc(
            worried,
            "Without all four crystals, the shield fades. A few more days, and it will collapse " +
                "completely. And then the ogres...",
        )
        chatPlayer(quiz, "Do you know who took it?")
        chatNpc(
            neutral,
            "For the last few nights, I've seen a dark glow from the top of this tower. Far to the " +
                "northeast, on the hills east of the Fishing Guild.",
        )
        chatNpc(
            neutral,
            "Whoever is up there is using powerful magic. My crystal's magic, I'd bet my beard on " +
                "it.",
        )
        chatNpc(
            worried,
            "Please, find whoever it is and bring the crystal back. But be careful. Anyone who " +
                "steals from this tower is no ordinary thief.",
        )
        quest.setQuestStage(access, STAGE_FIND_THIEF)
    }

    private suspend fun Dialogue.wizardSeesCurse() {
        chatPlayer(neutral, "I found your crystal. The thief was a dark wizard named Viyeldi.")
        chatNpc(shocked, "Viyeldi! I thought that name had been forgotten. Show me the crystal...")
        chatNpc(
            sad,
            "Oh no. Oh no, no, no. It's grey and lifeless. His dark magic has drained every bit of " +
                "power from it.",
        )
        chatNpc(sad, "I can't put it back like this. It would poison the whole shield.")
        chatPlayer(quiz, "Can you clean it?")
        chatNpc(
            neutral,
            "Not with magic. Dark magic this deep can only be driven out by faith. Take it to an " +
                "altar and pray over it.",
        )
        chatNpc(
            neutral,
            "It will take a strong spirit. But you've already faced a demon, so I think you'll " +
                "manage.",
        )
    }

    private suspend fun Dialogue.wizardRestores() {
        chatPlayer(happy, "I've cleansed the crystal.")
        chatNpc(happy, "Let me see... It's shining again! Pure as the day I first placed it.")
        chatNpc(neutral, "Now, back where it belongs...")
        if (access.invDel(access.inv, CYAN_CRYSTAL).failure) {
            return
        }
        quest.setQuestStage(access, STAGE_RESTORED)
        chatNpc(
            happy,
            "Listen... can you hear the hum? The shield is whole again. Yanille is safe.",
        )
        chatNpc(
            happy,
            "I don't know how to thank you. But I know someone who will want to hear about this. " +
                "Go back to Radimus Erkle.",
        )
    }

    private suspend fun Dialogue.viyeldi(viyeldi: Npc?) {
        when (stage(player)) {
            STAGE_NOT_STARTED,
            STAGE_STARTED -> chatNpc(angry, "Leave. You have no business here.")
            STAGE_FIND_THIEF -> {
                chatPlayer(angry, "You're the one who stole the crystal from the Watchtower.")
                chatNpc(
                    laugh,
                    "Stole? I *borrowed* it. That old fool in his tower wastes its power holding " +
                        "back a few ogres.",
                )
                chatNpc(
                    angry,
                    "In my hands, it opens doors. Doors to places you couldn't imagine. And things " +
                        "come through those doors...",
                )
                chatPlayer(angry, "Give the crystal back. Yanille needs it.")
                chatNpc(laugh, "Yanille? Hah! Let me show you what I need it for.")
                chatNpc(angry, "NEZIKCHENED! I call upon you! Rise, and destroy this fool!")
                quest.setQuestStage(access, STAGE_FIGHTING)
                summon(viyeldi)
            }
            STAGE_FIGHTING ->
                if (foes.active(player, NEZIKCHENED) != null) {
                    chatNpc(laugh, "Ha! You'd better worry about my pet, not me.")
                } else {
                    chatNpc(laugh, "Back again? You didn't learn your lesson the first time.")
                    chatNpc(angry, "NEZIKCHENED! Finish what you started!")
                    summon(viyeldi)
                }
            else ->
                chatNpc(
                    angry,
                    "Enjoy your little victory. There will be other crystals... and other doors.",
                )
        }
    }

    private fun Dialogue.summon(viyeldi: Npc?) {
        if (foes.active(player, NEZIKCHENED) != null) {
            return
        }
        val anchor = viyeldi?.coords ?: player.coords
        access.ifClose()
        foes.spawn(player, NEZIKCHENED, anchor.translateX(1), anchor, attack = true)
    }

    private fun nezikchenedSlain(killer: Player) {
        if (stage(killer) != STAGE_FIGHTING) {
            return
        }
        launcher.launch(killer) {
            mes("Viyeldi: No! Impossible! This isn't over, adventurer!")
            mes("Viyeldi vanishes in a cloud of black smoke. He leaves the crystal behind.")
            invAddOrDrop(objRepo, GREY_CRYSTAL)
            quest.setQuestStage(this, STAGE_CURSED_CRYSTAL)
        }
    }

    private suspend fun ProtectedAccess.cleanseCrystal() {
        if (quest.getQuestStage(player) != STAGE_CURSED_CRYSTAL) {
            mes("Nothing interesting happens.")
            return
        }
        if (statBase(PRAYER) < REQUIRED_PRAYER) {
            mes(
                "You pray over the crystal, but the darkness is too strong. You need a Prayer " +
                    "level of $REQUIRED_PRAYER."
            )
            return
        }
        mes("You kneel at the altar and pray over the dark crystal...")
        anim("seq.human_pickupfloor")
        delay(3)
        val swapped =
            player.invTransaction(inv) {
                val inventory = select(inv)
                delete {
                    from = inventory
                    obj = GREY_CRYSTAL.asRSCM()
                    strictCount = 1
                }
                insert {
                    into = inventory
                    obj = CYAN_CRYSTAL.asRSCM()
                    strictCount = 1
                }
            }.success
        if (swapped) {
            mes("The darkness slowly fades. The crystal glows a bright, pure cyan.")
            quest.setQuestStage(this, STAGE_CLEANSED)
        }
    }

    private suspend fun ProtectedAccess.legendsGate(gate: BoundLocInfo) {
        if (!quest.isQuestCompleted(player)) {
            startDialogue {
                chatNpcSpecific(
                    "Radimus Erkle",
                    RADIMUS,
                    neutral,
                    "Only legends may pass through this gate.",
                )
            }
            return
        }
        val otherSide = DoorTranslations.translateOpen(gate.coords, gate.shape, gate.angle)
        swingThrough(locRepo, gate, if (gate.isOnDoorTileSide(coords)) otherSide else gate.coords)
    }

    private suspend fun ProtectedAccess.rubLamp() {
        val stat = chooseLampSkill("Choose the stat you wish to be advanced!") ?: return
        if (invDel(inv, LAMP).failure) {
            return
        }
        statAdvance(stat, LAMP_XP)
        mes("Your wish has been granted!")
    }

    private fun List<String>.joinWithAnd(): String =
        if (size <= 1) joinToString() else dropLast(1).joinToString(", ") + " and " + last()

    companion object {
        const val QUEST_KEY = "quest_darkened_shield"

        const val STAGE_NOT_STARTED = 0
        const val STAGE_STARTED = 1
        const val STAGE_FIND_THIEF = 2
        const val STAGE_FIGHTING = 3
        const val STAGE_CURSED_CRYSTAL = 4
        const val STAGE_CLEANSED = 5
        const val STAGE_RESTORED = 6
        const val STAGE_COMPLETE = 7

        const val LAMP_REWARD = 2
        const val LAMP_XP = 7500.0

        private const val REQUIRED_QP = 10
        private const val REQUIRED_PRAYER = 42
        private const val PRAYER = "stat.prayer"
        private val REQUIRED_STATS =
            listOf(
                Triple("stat.attack", "Attack", 50),
                Triple("stat.strength", "Strength", 50),
                Triple("stat.defence", "Defence", 50),
                Triple(PRAYER, "Prayer", REQUIRED_PRAYER),
            )

        private const val RADIMUS = "npc.radimus_erkle"
        private const val WATCHTOWER_WIZARD = "npc.watchtower_wizard"
        const val VIYELDI = "npc.viyeldi"
        private const val NEZIKCHENED = "npc.nezikchened"

        private const val GREY_CRYSTAL = "obj.powering_crystal4"
        private const val CYAN_CRYSTAL = "obj.powering_crystal3"
        private const val LAMP = "obj.legends_lamp"

        private val HIGHLIGHT_TAGS = Regex("</?red>")

        private val JOURNAL =
            listOf(
                "I can start this quest by speaking to <red>Radimus Erkle</red> outside the " +
                    "<red>Legends' Guild</red>. Requirements: 10 Quest Points, 50 Attack, 50 " +
                    "Strength, 50 Defence, 42 Prayer.",
                "Radimus told me the shield above Yanille is failing. I should speak to the " +
                    "<red>Watchtower Wizard</red> in the <red>Watchtower</red> north of " +
                    "<red>Yanille</red>.",
                "One of the <red>powering crystals</red> has been stolen. The wizard saw a dark " +
                    "glow on the <red>hills east of the Fishing Guild</red>.",
                "The dark wizard <red>Viyeldi</red> stole the crystal and summoned the demon " +
                    "<red>Nezikchened</red>. I must defeat it.",
                "I defeated Nezikchened and Viyeldi fled. The crystal has turned grey and " +
                    "lifeless. I must <red>pray over it at an altar</red>.",
                "The crystal is cleansed. I should bring it back to the <red>Watchtower " +
                    "Wizard</red>.",
                "The shield above Yanille is restored. I should return to <red>Radimus " +
                    "Erkle</red>.",
            )
    }
}
