package org.rsmod.server.shared.loader

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import com.github.michaelbull.logging.InlineLogger
import com.google.inject.Injector
import java.nio.file.Path
import kotlin.io.path.exists
import org.rsmod.api.server.config.GameMode
import org.rsmod.api.server.config.ServerConfig

/**
 * Decides which plugin scripts may start for the active `gamemode` in `game.yml`.
 *
 * Normal mode skips only Dodian content, so the server behaves exactly like upstream. Dodian mode
 * additionally skips every package listed in `.data/dodian/disabled-content.toml`. Classes stay on
 * the classpath either way, so shared helpers (e.g. quest state used by skills) keep working.
 */
class DodianScriptFilter(private val disabledPrefixes: List<String>) {
    fun allows(clazz: Class<*>): Boolean {
        val name = clazz.name
        return disabledPrefixes.none { name == it || name.startsWith("$it.") }
    }

    companion object {
        private const val DODIAN_PACKAGE = "org.rsmod.content.dodian"
        private val disabledContentFile: Path = Path.of(".data/dodian/disabled-content.toml")
        private val logger = InlineLogger()

        fun from(injector: Injector): DodianScriptFilter {
            val mode = injector.getInstance(ServerConfig::class.java).gamemode
            if (mode == GameMode.Normal) {
                return DodianScriptFilter(listOf(DODIAN_PACKAGE))
            }
            val disabled = readDisabledPackages()
            logger.info { "Dodian gamemode: skipping scripts in $disabled" }
            return DodianScriptFilter(disabled)
        }

        private fun readDisabledPackages(): List<String> {
            if (!disabledContentFile.exists()) {
                logger.warn { "Dodian gamemode: $disabledContentFile not found, no content disabled." }
                return emptyList()
            }
            val tree = TomlMapper().readTree(disabledContentFile.toFile())
            return tree.path("packages").map { it.asText().trim() }.filter { it.isNotEmpty() }
        }
    }
}
