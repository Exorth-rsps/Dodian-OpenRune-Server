package org.rsmod.server.shared.loader

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.github.michaelbull.logging.InlineLogger
import com.google.inject.Injector
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import org.rsmod.api.server.config.GameMode
import org.rsmod.api.server.config.ServerConfig

/**
 * Decides which plugin scripts may start for the active `gamemode` in `game.yml`.
 *
 * Normal mode skips only Dodian content, so the server behaves exactly like upstream. Dodian mode
 * additionally skips every package listed in `.data/dodian/disabled-content.toml`, except the ones
 * under `enabled` there. Classes stay on
 * the classpath either way, so shared helpers (e.g. quest state used by skills) keep working.
 */
class DodianScriptFilter(
    private val disabledPrefixes: List<String>,
    private val enabledPrefixes: List<String> = emptyList(),
) {
    fun allows(clazz: Class<*>): Boolean {
        val name = clazz.name
        if (enabledPrefixes.any { name.matchesPackage(it) }) {
            return true
        }
        return disabledPrefixes.none { name.matchesPackage(it) }
    }

    private fun String.matchesPackage(prefix: String): Boolean =
        this == prefix || startsWith("$prefix.")

    companion object {
        private const val DODIAN_PACKAGE = "org.rsmod.content.dodian"
        private val disabledContentFile: Path = Path.of(".data/dodian/disabled-content.toml")
        private val cacheGamemodeFile: Path = Path.of(".data/cache/gamemode.txt")
        private val logger = InlineLogger()

        fun from(injector: Injector): DodianScriptFilter {
            val mode = injector.getInstance(ServerConfig::class.java).gamemode
            warnOnCacheMismatch(mode)
            if (mode == GameMode.Normal) {
                return DodianScriptFilter(listOf(DODIAN_PACKAGE))
            }
            val disabled = readPackages("packages")
            val enabled = readPackages("enabled")
            logger.info { "Dodian gamemode: skipping scripts in $disabled, except $enabled" }
            return DodianScriptFilter(disabled, enabled)
        }

        private fun warnOnCacheMismatch(mode: GameMode) {
            val expected = mode.name.lowercase()
            val built = if (cacheGamemodeFile.exists()) cacheGamemodeFile.readText().trim() else "normal"
            if (built != expected) {
                logger.warn {
                    "game.yml gamemode is '$expected' but the cache was built for '$built'. " +
                        "Map spawns will not match until you run `gradlew buildCache`."
                }
            }
        }

        private fun readPackages(key: String): List<String> {
            if (!disabledContentFile.exists()) {
                logger.warn { "Dodian gamemode: $disabledContentFile not found, no content disabled." }
                return emptyList()
            }
            val tree = TomlMapper().readTree(disabledContentFile.toFile())
            return tree.path(key).map { it.asText().trim() }.filter { it.isNotEmpty() }
        }
    }
}
