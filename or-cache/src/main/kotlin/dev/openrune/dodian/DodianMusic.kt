package dev.openrune.dodian

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import dev.openrune.definition.constants.ConstantProvider
import dev.openrune.definition.dbtables.DBRow
import dev.openrune.definition.dbtables.DBTable
import java.io.File

/**
 * In dodian mode the `music_modern` rows (area -> tracks) come from `.data/dodian/music.toml`
 * instead of the upstream rows. Column ids match `dev.openrune.impl.Music.musicModern`:
 * 0 = area name without `area.`, 1 = track dbrows, 2 = start/stop music on area enter/exit.
 */
public object DodianMusic {
    private val musicFile = File("../.data/dodian/music.toml")
    private const val FIRST_ROW_ID = 63000

    public fun modern(upstream: DBTable): DBTable {
        if (!DodianMapPaths.isDodian) {
            return upstream
        }
        return upstream.copy(rows = rows())
    }

    private fun rows(): List<DBRow> {
        if (!musicFile.exists()) {
            return emptyList()
        }
        val regions = TomlMapper().readTree(musicFile).path("region")
        return regions.mapIndexed { index, region ->
            val area = region.path("area").asText().removePrefix("area.")
            require(area.isNotEmpty()) { "$musicFile: every [[region]] needs 'area'" }
            val tracks =
                region.path("tracks").map { track ->
                    val name = track.asText()
                    ConstantProvider.getMapping(name)
                }
            require(tracks.isNotEmpty()) { "$musicFile: region '$area' has no tracks" }
            DBRow(
                rowId = FIRST_ROW_ID + index,
                columns =
                    mapOf(
                        0 to arrayOf<Any>(area),
                        1 to tracks.toTypedArray<Any>(),
                        2 to arrayOf<Any>(true),
                    ),
            )
        }
    }
}
