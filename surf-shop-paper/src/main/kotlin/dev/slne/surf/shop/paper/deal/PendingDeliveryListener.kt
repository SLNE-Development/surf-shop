package dev.slne.surf.shop.paper.deal

import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.paper.plugin
import kotlinx.coroutines.delay
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import kotlin.time.Duration.Companion.seconds

/** Hands out purchases that were paid while the buyer was offline. */
object PendingDeliveryListener : Listener {
    /** Give the player a moment to finish joining before touching the inventory. */
    private val JOIN_DELAY = 2.seconds

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        plugin.launch {
            delay(JOIN_DELAY)

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
