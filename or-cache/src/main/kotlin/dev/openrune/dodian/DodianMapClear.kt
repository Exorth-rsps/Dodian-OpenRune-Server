package dev.openrune.dodian

import dev.openrune.cache.MAPS
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.filesystem.Cache
import java.io.File
import java.nio.ByteBuffer

/**
 * Removes every map square from the cache that is not part of a Dodian `.pack` file, so only the
 * Dodian maps exist in dodian mode. Squares covered by the packs are kept because [PackMaps] skips
 * unchanged pack files on incremental builds and would not write them again.
 */
public class DodianMapClear(private val packDir: File) : CacheTask() {
    override fun init(cache: Cache) {
        val keep = packedSquares(packDir)
        val removed = cache.archives(MAPS).filterNot { it in keep }
        removed.forEach { cache.remove(MAPS, it) }
        println("Dodian maps: kept ${keep.size} map squares, removed ${removed.size} original squares.")
    }

    public companion object {
        public fun packedSquares(packDir: File): Set<Int> {
            val files = packDir.listFiles { file -> file.extension == "pack" }.orEmpty()
            return files.flatMapTo(mutableSetOf()) { squaresIn(it) }
        }

        private fun squaresIn(file: File): List<Int> {
            val baseRegion =
                Regex("""(\d+)$""").find(file.nameWithoutExtension)?.groupValues?.get(1)?.toInt()
                    ?: error("Unable to determine base region id from ${file.name}")
            val baseX = (baseRegion shr 8) and 0xFF
            val baseZ = baseRegion and 0xFF
            val buffer = ByteBuffer.wrap(file.readBytes())
            return List(buffer.int) {
                buffer.int
                buffer.int
                val x = baseX + buffer.int
                val z = baseZ + buffer.int
                repeat(2) {
                    val length = buffer.int
                    buffer.position(buffer.position() + length)
                }
                (x shl 8) or (z and 0xFF)
            }
        }
    }
}
