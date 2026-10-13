package dev.openrune.dodian

import dev.openrune.cache.CONFIGS
import dev.openrune.cache.DBROW
import dev.openrune.cache.NPC
import dev.openrune.cache.WORLDMAPAREAS
import dev.openrune.cache.tools.TaskPriority
import dev.openrune.cache.tools.WorldMapPacker
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.cache.worldmap.worldmap.MapsquareSingleSection
import dev.openrune.cache.worldmap.worldmap.WorldMapAreaDetails
import dev.openrune.cache.worldmap.worldmap.WorldMapFormat
import dev.openrune.cache.worldmap.worldmap.builder.test.WorldMap
import dev.openrune.cache.worldmap.worldmap.config.WorldMapConfig
import dev.openrune.cache.worldmap.worldmap.utils.Coordinate
import dev.openrune.definition.codec.DBRowCodec
import dev.openrune.definition.codec.NPCCodec
import dev.openrune.definition.constants.ConstantProvider
import dev.openrune.filesystem.Cache
import java.io.File
import javax.imageio.ImageIO
import readCacheRevision

/**
 * Writes the data the Dodian music editor (`tools/dodian-editor`) needs to
 * `.data/cache/dodian-editor/`: one PNG per group of touching Dodian map squares (so dungeons and
 * altars get their own image) plus `maps.json` with their tile bounds, `tracks.json` with every
 * music track and `npcs.json` with every named npc in the cache. Maps are only re-rendered when the Dodian map squares change.
 */
public class DodianEditorExport(private val packDir: File) : CacheTask() {
    override val priority: TaskPriority = TaskPriority.MAPS

    override fun init(cache: Cache) {
        if (!hasWorldMap(cache)) {
            return
        }
        outputDir.mkdirs()
        writeTracks(cache)
        writeNpcs(cache)

        val keep = DodianMapClear.packedSquares(packDir)
        val stamp = keep.sorted().joinToString(",") + "|" + packStamp()
        val stampFile = File(outputDir, "maps.stamp")
        if (stampFile.exists() && stampFile.readText() == stamp) {
            return
        }
        writeMaps(cache, keep)
        stampFile.writeText(stamp)
    }

    private fun writeMaps(cache: Cache, keep: Set<Int>) {
        outputDir.listFiles { file -> file.name.startsWith("map_") }?.forEach(File::delete)
        val config =
            WorldMapConfig().apply {
                cacheRevision =
                    runCatching { readCacheRevision(cache) }.getOrDefault(WorldMapFormat.REVISION)
            }
        val providers = WorldMapPacker(cache, progress).buildProviders()
        val worldMap = WorldMap(config, cache)
        val entries = mutableListOf<String>()
        for ((index, group) in groups(keep).withIndex()) {
            val xs = group.map { it shr 8 }
            val ys = group.map { it and 0xFF }
            val details =
                WorldMapAreaDetails(
                    id = index,
                    internalName = "dodian_editor_$index",
                    displayName = "dodian_editor_$index",
                    origin = Coordinate(xs.min() * 64, ys.min() * 64, 0),
                    backgroundColour = 0,
                    zoom = 100,
                    sections =
                        group.map { square ->
                            val x = square shr 8
                            val y = square and 0xFF
                            MapsquareSingleSection(0, 1, x, y, x, y)
                        },
                )
            val image = worldMap.generateImage(providers, details, emptyList(), PIXELS_PER_TILE)
            val file = "map_$index.png"
            ImageIO.write(image, "png", File(outputDir, file))
            entries +=
                """{"file":"$file","minX":${xs.min() * 64},"minY":${ys.min() * 64},""" +
                    """"maxX":${xs.max() * 64 + 63},"maxY":${ys.max() * 64 + 63},""" +
                    """"squares":${group.size},"ppt":$PIXELS_PER_TILE,""" +
                    """"width":${image.width},"height":${image.height}}"""
        }
        File(outputDir, "maps.json").writeText(entries.joinToString(",\n", "[\n", "\n]\n"))
        println("Dodian editor: rendered ${entries.size} maps to $outputDir")
    }

    private fun writeTracks(cache: Cache) {
        val codec = DBRowCodec()
        val knownTrack = ConstantProvider.getMapping("dbrow.music_baroque")
        val musicTable =
            cache.data(CONFIGS, DBROW, knownTrack)?.let { codec.loadData(knownTrack, it).tableId }
                ?: return
        val tracks = mutableListOf<String>()
        for (id in cache.files(CONFIGS, DBROW)) {
            val data = cache.data(CONFIGS, DBROW, id) ?: continue
            val row = runCatching { codec.loadData(id, data) }.getOrNull() ?: continue
            if (row.tableId != musicTable) {
                continue
            }
            val name = row.definedColumns()[1]?.values?.firstOrNull()?.toString() ?: continue
            val hint = row.definedColumns()[2]?.values?.firstOrNull()?.toString().orEmpty()
            tracks += """{"row":$id,"name":${json(name)},"hint":${json(hint)}}"""
        }
        File(outputDir, "tracks.json").writeText(tracks.joinToString(",\n", "[\n", "\n]\n"))
    }

    private fun writeNpcs(cache: Cache) {
        val revision = runCatching { readCacheRevision(cache) }.getOrDefault(WorldMapFormat.REVISION)
        val codec = NPCCodec(revision)
        val npcs = mutableListOf<String>()
        for (id in cache.files(CONFIGS, NPC)) {
            val data = cache.data(CONFIGS, NPC, id) ?: continue
            val npc = runCatching { codec.loadData(id, data) }.getOrNull() ?: continue
            val name = npc.name
            if (name.isBlank() || name == "null") {
                continue
            }
            npcs += """{"id":$id,"name":${json(name)},"combat":${npc.combatLevel},"size":${npc.size}}"""
        }
        File(outputDir, "npcs.json").writeText(npcs.joinToString(",\n", "[\n", "\n]\n"))
    }

    /** Groups map squares that touch (including diagonally) so each group renders as one image. */
    private fun groups(squares: Set<Int>): List<List<Int>> {
        val left = squares.toMutableSet()
        val result = mutableListOf<List<Int>>()
        while (left.isNotEmpty()) {
            val start = left.first()
            left.remove(start)
            val group = mutableListOf(start)
            val queue = ArrayDeque(listOf(start))
            while (queue.isNotEmpty()) {
                val square = queue.removeFirst()
                for (dx in -1..1) {
                    for (dy in -1..1) {
                        val next = (((square shr 8) + dx) shl 8) or ((square and 0xFF) + dy)
                        if (left.remove(next)) {
                            group += next
                            queue += next
                        }
                    }
                }
            }
            result += group
        }
        return result.sortedByDescending { it.size }
    }

    private fun hasWorldMap(cache: Cache): Boolean =
        runCatching { cache.files(WORLDMAPAREAS, WorldMapFormat.detailsGroupId()).isNotEmpty() }
            .getOrDefault(false)

    private fun packStamp(): String =
        packDir
            .listFiles { file -> file.extension == "pack" }
            .orEmpty()
            .sortedBy { it.name }
            .joinToString(",") { "${it.name}:${it.length()}:${it.lastModified()}" }

    private fun json(value: String): String =
        buildString {
            append('"')
            for (char in value) {
                when {
                    char == '"' -> append("\\\"")
                    char == '\\' -> append("\\\\")
                    char < ' ' -> append(" ")
                    else -> append(char)
                }
            }
            append('"')
        }

    private companion object {
        const val PIXELS_PER_TILE = 4
        val outputDir = File("../.data/cache/dodian-editor")
    }
}
