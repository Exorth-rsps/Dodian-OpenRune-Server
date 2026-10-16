package org.rsmod.content.dodian.quest.spinningayarn

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.shops.Shops
import org.rsmod.content.dodian.quest.common.carries
import org.rsmod.content.dodian.quest.common.takeUpTo
import org.rsmod.content.quest.manager.ItemRewardDisplay
import org.rsmod.content.quest.manager.QuestScript
import org.rsmod.content.quest.manager.rewards
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.ScriptContext

class SpinningAYarn
@Inject
constructor(
    private val objRepo: ObjRepository,
    private val shops: Shops,
    private val random: GameRandom,
) :
    QuestScript(
        QUEST_KEY,
        "varp.dodian_spinning_a_yarn",
        rewards {
            xp("stat.crafting", CRAFTING_XP)
            extra("$COIN_REWARD coins")
            extra("Balls of wool for sale at the Silk merchant")
        },
        ItemRewardDisplay(BALL_OF_WOOL, zoom = 400),
        questVarbit = "varbit.dodian_spinning_a_yarn_progress",
    ) {

    private var Player.woolGiven by intVarBit("varbit.dodian_spinning_a_yarn_wool_given")

    override fun ScriptContext.init() {
        check(quest.maxSteps == STAGE_COMPLETE) {
            "Spinning a Yarn end state is ${quest.maxSteps} in .data/dodian/quests.toml, but the " +
                "quest completes at stage $STAGE_COMPLETE."
        }
        onOpNpc1(SILK_MERCHANT) { startDialogue(it.npc) { merchant(it.npc) } }
    }

    override fun subTitle(): String = "speaking to the <col=800000>Silk merchant</col> at his stall."

    override fun questLog(player: ProtectedAccess): String =
        questJournal(player) {
            line(
                "The city guard has ordered <red>20 balls of wool</red> from the Silk merchant, but " +
                    "his supplier has hurt his back."
            )
            line(
                "I can shear the <red>sheep</red> at the <red>farm</red> outside town and spin the " +
                    "wool on a <red>spinning wheel</red>."
            )
            line("I have delivered <red>${player.player.woolGiven}/$WOOL_NEEDED</red> balls of wool.")
        }

    override fun completedLog(player: ProtectedAccess): String =
        completionJournal(player) {
            line("The night watch of Yanille will have warm socks all winter.")
        }

    private suspend fun Dialogue.merchant(npc: Npc) {
        when (quest.getQuestStage(player)) {
            STAGE_NOT_STARTED -> merchantOffer()
            STAGE_STARTED -> merchantDelivery()
            else -> {
                chatNpc(
                    happy,
                    "Ah, my favourite shepherd! The guards say their socks are the warmest in the " +
                        "land. I told them they'd be warmer in silk. They didn't listen.",
                )
                offerShop(npc)
            }
        }
    }

    private suspend fun Dialogue.offerShop(npc: Npc) {
        val trade = choice2("Let's see what you're selling.", true, "Goodbye.", false)
        if (!trade) {
            return
        }
        openShop(npc)
    }

    private fun Dialogue.openShop(npc: Npc) {
        if (quest.isQuestCompleted(player)) {
            shops.open(player, npc, SHOP_TITLE, WOOL_SHOP)
        } else {
            shops.open(player, npc, SHOP_TITLE, SILK_SHOP)
            access.mes("The Silk merchant has no balls of wool in stock.")
        }
    }

    private suspend fun Dialogue.merchantOffer() {
        chatNpc(
            happy,
            "Silk! The finest silk in all the land! Smooth as a summer breeze, light as a feather!",
        )
        chatPlayer(quiz, "I see you also sell balls of wool.")
        chatNpc(
            worried,
            "Shh! Not so loud. Wool is... a side business. Silk is my passion. Wool merely pays " +
                "the bills.",
        )
        chatNpc(sad, "And right now, it isn't even doing that. I'm completely sold out.")
        chatPlayer(quiz, "What happened?")
        chatNpc(
            neutral,
            "The city guard has ordered twenty balls of wool. The nights on the walls are getting " +
                "cold, and they want to knit warm socks for the night watch.",
        )
        chatNpc(
            sad,
            "Twenty! My biggest order of the year. And my supplier, the farmer, has done his back " +
                "in. He can't even bend down to tie his boots, let alone shear a sheep.",
        )
        chatPlayer(quiz, "Can't you shear them yourself?")
        chatNpc(
            shocked,
            "Me? Touch a sheep? Do you know what they smell like? I once sailed through a storm " +
                "off Karamja with nothing but a silk sail and a ship's biscuit, but a sheep... no. " +
                "I have my limits.",
        )
        chatNpc(
            quiz,
            "But you look like someone who isn't afraid of a little lanolin. Would you bring me " +
                "twenty balls of wool?",
        )
        val accept =
            choice2("Alright, I'll get your wool.", true, "I'd rather not smell like a sheep.", false)
        if (!accept) {
            chatPlayer(neutral, "I'd rather not smell like a sheep.")
            chatNpc(
                neutral,
                "I understand completely. Truly, I do. But the guard won't. If you change your " +
                    "mind, I'll be here.",
            )
            return
        }
        chatPlayer(happy, "Alright, I'll get your wool.")
        chatNpc(
            happy,
            "Splendid! You'll find the sheep at the farm outside town. There should be a pair of " +
                "shears lying around there somewhere.",
        )
        chatNpc(
            neutral,
            "Shear the sheep, then spin the wool into balls on the spinning wheel. Raw wool is no " +
                "use to me. The guard wants to knit, not stuff pillows.",
        )
        chatNpc(
            happy,
            "You can bring them in a few at a time, I'll keep count. I'm a merchant after all. " +
                "Counting is the one thing I never get wrong.",
        )
        player.woolGiven = 0
        quest.setQuestStage(access, STAGE_STARTED)
    }

    private suspend fun Dialogue.merchantDelivery() {
        if (access.carries(BALL_OF_WOOL)) {
            chatNpc(
                happy,
                "Is that wool I see? Hand it over, quickly, before anyone notices I'm dealing in " +
                    "the stuff.",
            )
            val taken = access.takeUpTo(BALL_OF_WOOL, WOOL_NEEDED - player.woolGiven)
            player.woolGiven += taken
            if (player.woolGiven >= WOOL_NEEDED) {
                allWool()
                return
            }
            val remaining = WOOL_NEEDED - player.woolGiven
            chatNpc(neutral, "That's ${player.woolGiven} so far. I still need $remaining more.")
            chatNpc(laugh, TALL_TALES[random.of(TALL_TALES.size)])
            return
        }
        val remaining = WOOL_NEEDED - player.woolGiven
        if (access.carries(WOOL)) {
            chatNpc(
                angry,
                "That's raw wool! Did you think the guard would knit socks out of fluff? Spin it " +
                    "into balls on the spinning wheel first.",
            )
            return
        }
        chatNpc(
            neutral,
            "I still need $remaining balls of wool. The sheep are at the farm outside town, and " +
                "don't forget to spin the wool first.",
        )
    }

    private suspend fun Dialogue.allWool() {
        chatNpc(
            happy,
            "Twenty! Twenty balls of wool! Exactly twenty, I counted them myself, three times.",
        )
        chatNpc(
            happy,
            "The guard will have warm feet all winter, and I'll have a very full purse. Well, " +
                "slightly fuller.",
        )
        chatNpc(neutral, "Here's your share. And between you and me...")
        chatNpc(
            laugh,
            "...wool isn't all that bad. Don't tell anyone I said that. I have a reputation to " +
                "protect.",
        )
        chatNpc(
            happy,
            "From now on, if you ever need balls of wool, you know where to come. And if you ever " +
                "want real quality, there's always silk.",
        )
        access.invAddOrDrop(objRepo, COINS, COIN_REWARD)
        quest.completeQuest(access)
    }

    companion object {
        const val QUEST_KEY = "quest_spinning_a_yarn"

        const val STAGE_NOT_STARTED = 0
        const val STAGE_STARTED = 1
        const val STAGE_COMPLETE = 2

        const val WOOL_NEEDED = 20
        const val CRAFTING_XP = 150.0
        const val COIN_REWARD = 500

        private const val SILK_MERCHANT = "npc.silk_merchant_ardougne"
        private const val SHOP_TITLE = "Silk Merchant"
        private const val SILK_SHOP = "inv.dodian_silk_shop"
        private const val WOOL_SHOP = "inv.dodian_silk_shop_wool"

        private const val BALL_OF_WOOL = "obj.ball_of_wool"
        private const val WOOL = "obj.wool"
        private const val COINS = "obj.coins"

        private val TALL_TALES =
            listOf(
                "Did I ever tell you about the time I traded silk with a dragon? It wanted a " +
                    "scarf. Very fashionable creature. Terrible haggler, though.",
                "When I was younger, I crossed the desert on a camel made entirely of silk. Well, " +
                    "its saddle was silk. The camel was mostly camel.",
                "I once sold a silk robe to a wizard who turned me into a teapot when I wouldn't " +
                    "lower my price. I still got my asking price. Teapots are very persuasive.",
                "The silk I sell comes from spiders the size of houses. Or so I tell the " +
                    "customers. Don't repeat that.",
            )
    }
}
