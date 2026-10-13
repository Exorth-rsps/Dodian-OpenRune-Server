package org.rsmod.api.player.music

import jakarta.inject.Inject
import org.junit.jupiter.api.Test
import org.rsmod.api.music.MusicRepository
import org.rsmod.api.player.vars.enumVarp
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.testing.GameTestState
import org.rsmod.game.entity.Player

class DodianMusicTmpTest {
    class Deps @Inject constructor(val music: MusicPlayer, val repo: MusicRepository)

    private var Player.playMode by enumVarp<MusicPlayMode>("varp.musicplay")
    private val Player.currId by intVarBit("varbit.music_curr_id")
    private val Player.currArea by intVarBit("varbit.music_curr_area")
    private val Player.duration by intVarBit("varbit.music_curr_duration")

    @Test
    fun GameTestState.`next track after a track ends`() =
        runInjectedGameTest(Deps::class) { deps ->
            deps.repo.load()
            println("modern area tracks: ${deps.repo.getModernArea("area.dodian_home")?.map { it.displayName }}")
            player.playMode = MusicPlayMode.Area
            deps.music.enterArea(player, "area.dodian_home")
            println("after enter: id=${player.currId} area=${player.currArea} duration=${player.duration}")
            repeat(5) {
                deps.music.stop(player)
                try {
                    deps.music.playNext(player)
                } catch (t: Throwable) {
                    println("playNext threw: $t")
                    t.printStackTrace()
                }
                println("after next #$it: id=${player.currId} name=${deps.repo.forId(player.currId)?.displayName} area=${player.currArea}")
            }
        }
}
