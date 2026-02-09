package dev.slne.surf.shop.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.shop.core.service.dealService
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        auctionService.fetchAuctions()
        dealService.fetchDeals()
    }
}