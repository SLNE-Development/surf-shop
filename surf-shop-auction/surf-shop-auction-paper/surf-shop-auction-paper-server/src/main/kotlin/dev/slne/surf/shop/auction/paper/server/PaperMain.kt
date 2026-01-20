package dev.slne.surf.shop.auction.paper.server

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.shop.auction.core.auction.service.AuctionsService
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        AuctionsService.cacheAuctions()
    }

    override suspend fun onEnableAsync() {

    }

    override suspend fun onDisableAsync() {

    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)