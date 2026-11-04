package dev.slne.surf.shop.core.paper.util

import kotlinx.coroutines.suspendCancellableCoroutine
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import kotlin.coroutines.resume

/**
 * Runs [block] on the region thread that owns this entity (Folia entity scheduler) and suspends until it ran.
 *
 * Unlike MCCoroutine's entity dispatcher – which runs the block on the *calling* thread once the entity was
 * removed – this uses the scheduler's retired callback and reports removal explicitly.
 *
 * @return the block's result, or `null` if the entity was removed (e.g. the player left) before the block ran
 */
suspend fun <T : Any> Entity.runOnOwningThread(plugin: Plugin, block: () -> T): T? =
    suspendCancellableCoroutine { continuation ->
        val task = scheduler.run(
            plugin,
            { continuation.resumeWith(runCatching(block)) },
            { continuation.resume(null) }
        )

        if (task == null) {
            continuation.resume(null)
        } else {
            continuation.invokeOnCancellation { task.cancel() }
        }
    }

/**
 * Gives [amount] x [item] with Paper's vanilla give logic; whatever does not fit is dropped and owned by the
 * player. Must run on the player's thread, e.g. inside [runOnOwningThread].
 */
fun Player.giveItems(item: ItemStack, amount: Int = item.amount) {
    give(splitIntoStacks(item, amount), true).drops().forEach { it.owner = uniqueId }
}

/** Splits [amount] x [item] into stacks of at most the item's max stack size. */
fun splitIntoStacks(item: ItemStack, amount: Int): List<ItemStack> {
    val maxStackSize = item.maxStackSize.coerceAtLeast(1)
    val stacks = ArrayList<ItemStack>(amount / maxStackSize + 1)
    var remaining = amount

    while (remaining > 0) {
        val stackSize = minOf(remaining, maxStackSize)
        stacks += item.clone().apply { this.amount = stackSize }
        remaining -= stackSize
    }

    return stacks
}
