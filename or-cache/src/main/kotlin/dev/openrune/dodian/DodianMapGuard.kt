package dev.openrune.dodian

import dev.openrune.cache.MAPS
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.filesystem.Cache
import kotlin.system.exitProcess

/**
 * Stops a normal-mode build when the cache still only holds the Dodian maps. The cache tool
 * swallows task exceptions, so this exits the process to make `buildCache` actually fail.
 */
public class DodianMapGuard : CacheTask() {
    override fun init(cache: Cache) {
        if (LUMBRIDGE in cache.archives(MAPS)) {
            return
        }
        System.err.println(
            "ERROR: the cache still has the Dodian maps (original OSRS maps were removed). " +
                "Run `gradlew freshCache` to restore them for gamemode \"normal\"."
        )
        exitProcess(1)
    }

    private companion object {
        const val LUMBRIDGE = (50 shl 8) or 50
    }
}
