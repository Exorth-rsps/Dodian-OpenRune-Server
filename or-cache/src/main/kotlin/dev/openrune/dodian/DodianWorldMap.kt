package dev.openrune.dodian

import dev.openrune.cache.WORLDMAPAREAS
import dev.openrune.cache.tools.TaskPriority
import dev.openrune.cache.tools.WorldMapPacker
import dev.openrune.cache.tools.incremental.PackedMapSquares
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.cache.worldmap.worldmap.WorldMapAreaDetails
import dev.openrune.cache.worldmap.worldmap.WorldMapFormat
import dev.openrune.cache.worldmap.worldmap.builder.test.WorldMap
import dev.openrune.cache.worldmap.worldmap.config.WorldMapConfig
import dev.openrune.filesystem.Cache
import io.netty.buffer.Unpooled
import java.io.File
import readCacheRevision

/**
 * Rebuilds every world map area from scratch whenever this build removed or packed map squares.
 *
 * The upstream incremental [dev.openrune.cache.tools.tasks.impl.PackWorldMap] keeps the old image of
 * a square that no longer exists, so removed OSRS squares would stay visible. A full regeneration
 * only draws squares still in the cache, and labels/icons outside the Dodian squares are dropped.
 */
public class DodianWorldMap(private val packDir: File) : CacheTask() {
    override val priority: TaskPriority = TaskPriority.MAPS

    override fun init(cache: Cache) {
        if (PackedMapSquares.packed.isEmpty()) {
            return
        }
        val areas = areaDetails(cache)
        if (areas.isEmpty()) {
            return
        }
        val keep = DodianMapClear.packedSquares(packDir)
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
            worldMap.update(
                area.id,
                area.internalName,
                providers,
                detailsTransformer = { it },
                labelsTransformer = { labels ->
                    labels.filter {
                        val square = ((it.location.x shr 6) shl 8) or (it.location.y shr 6)
                        square in keep
                    }
                },
            )
        }
        cache.update()
        println("Dodian world map: rebuilt ${areas.size} areas for ${keep.size} map squares.")
    }

    private fun areaDetails(cache: Cache): List<WorldMapAreaDetails> {
        val group = WorldMapFormat.detailsGroupId()
        val files: List<Int> =
            runCatching { cache.files(WORLDMAPAREAS, group).toList() }.getOrNull() ?: return emptyList()
        return files.mapNotNull { fileId: Int ->
            val data = cache.data(WORLDMAPAREAS, group, fileId) ?: return@mapNotNull null
            runCatching { WorldMapAreaDetails.decode(fileId, Unpooled.wrappedBuffer(data)) }
                .getOrNull()
        }
    }
}
