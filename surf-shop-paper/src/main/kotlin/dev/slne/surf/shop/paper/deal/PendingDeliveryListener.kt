package dev.slne.surf.shop.paper.deal

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.paper.plugin
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

/** Hands out purchases that were paid while the buyer was offline. */
object PendingDeliveryListener : Listener {

    /**
     * Fires once the client has loaded the world (or the server stopped waiting for it), so the
     * player is fully in the game. Later firings (e.g. after a respawn) find an empty queue.
     */
    @EventHandler
    fun onClientLoadedWorld(event: PlayerClientLoadedWorldEvent) {
        val player = event.player

        plugin.launch {
            val delivered = DealService.deliverPendingPurchases(player.uniqueId)
            if (delivered > 0 && player.isOnline) {
                player.sendText {
                    appendSuccessPrefix()
                    if (delivered == 1) {
                        success("Dein Kauf wurde abgeschlossen, während du offline warst. ")
                    } else {
                        variableValue("$delivered Käufe")
                        success(" wurden abgeschlossen, während du offline warst. ")
                    }
                    success("Du hast die Items jetzt erhalten.")
                }
            }
        }
    }
}
