package org.rsmod.content.dodian.quest.common

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Talk-to handlers for npcs that several quests speak through (Traiborn, Viyeldi). Handlers run by
 * descending priority until one returns true; the first registration binds the npc's op1.
 */
object SharedTalk {
    private class Handler(val priority: Int, val talk: suspend Dialogue.() -> Boolean)

    private val handlers = ConcurrentHashMap<String, CopyOnWriteArrayList<Handler>>()

    fun ScriptContext.onSharedTalk(npc: String, priority: Int, talk: suspend Dialogue.() -> Boolean) {
        var first = false
        val list = handlers.computeIfAbsent(npc) { first = true; CopyOnWriteArrayList() }
        list += Handler(priority, talk)
        if (first) {
            onOpNpc1(npc) {
                startDialogue(it.npc) {
                    for (handler in list.sortedByDescending { h -> h.priority }) {
                        if (handler.talk(this)) {
                            return@startDialogue
                        }
                    }
                }
            }
        }
    }
}
