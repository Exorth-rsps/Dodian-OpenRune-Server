package dev.openrune.dodian

import dev.openrune.cache.CONFIGS
import dev.openrune.cache.MAP_ELEMENT
import dev.openrune.cache.WORLDMAPAREAS
import dev.openrune.cache.tools.TaskPriority
import dev.openrune.cache.tools.WorldMapPacker
import dev.openrune.cache.tools.incremental.PackedMapSquares
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.cache.worldmap.worldmap.WorldMapArea
import dev.openrune.cache.worldmap.worldmap.WorldMapAreaDetails
import dev.openrune.cache.worldmap.worldmap.WorldMapElement
import dev.openrune.cache.worldmap.worldmap.WorldMapFormat
import dev.openrune.cache.worldmap.worldmap.builder.test.WorldMap
import dev.openrune.cache.worldmap.worldmap.config.WorldMapConfig
import dev.openrune.cache.worldmap.worldmap.providers.Providers
import dev.openrune.cache.worldmap.worldmap.utils.Coordinate
import dev.openrune.definition.codec.MapElementCodec
import dev.openrune.definition.type.MapElementType
import dev.openrune.filesystem.Cache
import io.netty.buffer.Unpooled
import java.io.File
import readCacheRevision

/**
 * Rebuilds the world map for dodian mode.
 *
 * Runs when this build removed or packed map squares, or when the label file changed:
 * 1. Writes the labels from `.data/dodian/worldmap/labels.toml` as map element configs, appended
 *    after the last OSRS element (the client loads element ids `0 until fileCount`, so they must
 *    stay contiguous). Earlier Dodian elements are recognised by [LABEL_MARKER] and replaced.
 * 2. Regenerates every area from the squares still in the cache. The upstream incremental
 *    [dev.openrune.cache.tools.tasks.impl.PackWorldMap] keeps the old image of removed squares.
 * 3. Deletes geography/ground files of squares no area references any more; the client would
 *    otherwise still draw their icons on an empty background.
 */
public class DodianWorldMap(private val packDir: File, private val labelFile: File) : CacheTask() {
    override val priority: TaskPriority = TaskPriority.MAPS

    override fun init(cache: Cache) {
        val areas = areaDetails(cache)
        if (areas.isEmpty()) {
            return
        }
        val keep = DodianMapClear.packedSquares(packDir)
        val labelText = if (labelFile.exists()) labelFile.readText() else ""
        val fingerprint = "${keep.sorted().hashCode()}:${labelText.hashCode()}"
        val squaresChanged = PackedMapSquares.packed.isNotEmpty()
        if (!squaresChanged && stateFile.takeIf { it.exists() }?.readText() == fingerprint) {
            return
        }

        val labels = DodianLabels.parse(labelFile)
        val (firstCustomId, customIds) = writeLabelElements(cache, labels.labels)

        val config =
            WorldMapConfig().apply {
                cacheRevision =
                    runCatching { readCacheRevision(cache) }.getOrDefault(WorldMapFormat.REVISION)
            }
        val providers = WorldMapPacker(cache, progress).buildProviders()
        val worldMap = WorldMap(config, cache)
        for (area in areas) {
            if (!worldMap.exists(providers, area.id, area.internalName)) {
                continue
            }
            val custom =
                labels.labels.indices
                    .filter { labels.labels[it].area == area.internalName }
                    .map { index ->
                        val label = labels.labels[index]
                        WorldMapElement(
                            customIds[index],
                            Coordinate(label.x, label.y, label.level),
                            false,
                        )
                    }
            worldMap.update(
                area.id,
                area.internalName,
                providers,
                detailsTransformer = { it },
                labelsTransformer = { existing ->
                    val osrs =
                        if (labels.keepOsrsLabels) {
                            existing.filter {
                                it.elementId < firstCustomId && it.location.square() in keep
                            }
                        } else {
                            emptyList()
                        }
                    osrs + custom
                },
                regenerateGeography = squaresChanged,
            )
        }
        removeUnusedGeography(cache, providers, areas, config.cacheRevision)
        cache.update()
        stateFile.writeText(fingerprint)
        println(
            "Dodian world map: rebuilt ${areas.size} areas, ${labels.labels.size} custom labels, " +
                "OSRS labels ${if (labels.keepOsrsLabels) "kept" else "removed"}."
        )
    }

    private fun writeLabelElements(
        cache: Cache,
        labels: List<DodianLabels.Label>,
    ): Pair<Int, List<Int>> {
        val codec = MapElementCodec()
        val lastId = cache.lastFileId(CONFIGS, MAP_ELEMENT)
        val firstDodian =
            (0..lastId).firstOrNull { id ->
                val data = cache.data(CONFIGS, MAP_ELEMENT, id) ?: return@firstOrNull false
                codec.loadData(id, data).menuTargetName == LABEL_MARKER
            }
        val base = firstDodian ?: (lastId + 1)
        for (id in base..lastId) {
            cache.remove(CONFIGS, MAP_ELEMENT, id)
        }
        val ids =
            labels.mapIndexed { index, label ->
                val id = base + index
                val type =
                    MapElementType(
                        id = id,
                        name = label.text,
                        fontColor = label.style.colour,
                        textSize = label.style.textSize,
                        menuTargetName = LABEL_MARKER,
                        category = LABEL_CATEGORY,
                    )
                val buffer = Unpooled.buffer()
                with(codec) { buffer.encode(type) }
                val bytes = ByteArray(buffer.readableBytes()).also(buffer::readBytes)
                cache.write(CONFIGS, MAP_ELEMENT, id, bytes)
                id
            }
        return base to ids
    }

    private fun removeUnusedGeography(
        cache: Cache,
        providers: Providers,
        areas: List<WorldMapAreaDetails>,
        revision: Int,
    ) {
        val used = HashSet<Pair<Int, Int>>()
        for (area in areas) {
            val placements =
                runCatching {
                        WorldMapArea.decode(
                            providers.cacheProvider,
                            area.id,
                            area.internalName,
                            revision,
                            geographyFilter = { _, _ -> false },
                        )
                    }
                    .getOrNull() ?: continue
            for (square in placements.data.mapsquares) {
                val data = square.data
                used += regionKey(data.mapsquareDestinationX, data.mapsquareDestinationY) to area.id
            }
            for (zone in placements.data.zones) {
                val data = zone.data
                used += regionKey(data.mapsquareDestinationX, data.mapsquareDestinationY) to area.id
            }
        }
        var removed = 0
        for (archive in intArrayOf(WORLD_MAP_GEOGRAPHY, WORLD_MAP_GROUND)) {
            for (group in cache.archives(archive)) {
                for (file in cache.files(archive, group)) {
                    if (group to file !in used) {
                        cache.remove(archive, group, file)
                        removed++
                    }
                }
            }
        }
        println("Dodian world map: removed $removed unused geography/ground files.")
    }

    private fun areaDetails(cache: Cache): List<WorldMapAreaDetails> {
        val group = WorldMapFormat.detailsGroupId()
        val files: List<Int> =
            runCatching { cache.files(WORLDMAPAREAS, group).toList() }.getOrNull()
                ?: return emptyList()
        return files.mapNotNull { fileId: Int ->
            val data = cache.data(WORLDMAPAREAS, group, fileId) ?: return@mapNotNull null
            runCatching { WorldMapAreaDetails.decode(fileId, Unpooled.wrappedBuffer(data)) }
                .getOrNull()
        }
    }

    private fun Coordinate.square(): Int = regionKey(x shr 6, y shr 6)

    private fun regionKey(x: Int, y: Int): Int = WorldMapFormat.regionGroupKey(x, y)

    private companion object {
        const val LABEL_MARKER = "dodian-label"
        const val LABEL_CATEGORY = 1129
        const val WORLD_MAP_GEOGRAPHY = 18
        const val WORLD_MAP_GROUND = 20
        val stateFile = File("../.data/cache/dodian-worldmap.txt")
    }
}
