package org.rsmod.content.dodian.quest.common

import org.rsmod.api.player.protect.ProtectedAccess

fun ProtectedAccess.carries(item: String): Boolean = inv.contains(item)

fun ProtectedAccess.carriesOrWears(item: String): Boolean = inv.contains(item) || worn.contains(item)

/** True when [item] is in the inventory, worn, or in the bank. */
fun ProtectedAccess.ownsAnywhere(item: String): Boolean =
    inv.contains(item) || worn.contains(item) || bank.contains(item)

fun ProtectedAccess.carriedCount(item: String): Int = invTotal(inv, item)

/** Takes up to [max] of [item] from the inventory and returns how many were taken. */
fun ProtectedAccess.takeUpTo(item: String, max: Int): Int {
    val count = minOf(invTotal(inv, item), max)
    if (count <= 0 || invDel(inv, item, count).failure) {
        return 0
    }
    return count
}

/** Removes every [item] from the inventory, equipment and bank (quest items that are used up). */
fun ProtectedAccess.removeEverywhere(item: String) {
    for (container in listOf(inv, worn, bank)) {
        val count = invTotal(container, item)
        if (count > 0) {
            invDel(container, item, count)
        }
    }
}
