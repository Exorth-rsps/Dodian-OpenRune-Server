package org.rsmod.content.dodian

import org.rsmod.api.instances.InstanceRegionAliases
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Boss instances whose arena was copied to another map square on the Dodian maps. The copy must
 * keep every tile at the same local position, so the instance scripts' coordinates still line up.
 */
class DodianInstances : PluginScript() {
    override fun ScriptContext.startup() {
        InstanceRegionAliases.register(originalRegion = SCURRIUS_LAIR, aliasRegion = DODIAN_SCURRIUS_LAIR)
    }

    private companion object {
        const val SCURRIUS_LAIR = 13210

        /** The Varrock sewer lair copied into the Yanille mine pack. */
        const val DODIAN_SCURRIUS_LAIR = 10133
    }
}
