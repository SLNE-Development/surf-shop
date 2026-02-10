package dev.slne.surf.shop.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.shop.core.database.databaseLoader
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.shop.core.service.dealService
import dev.slne.surf.shop.paper.command.auctionCommand
import dev.slne.surf.shop.paper.menu.AuctionListView
import dev.slne.surf.shop.paper.menu.CreateAuctionView
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        databaseLoader.connect(plugin.dataPath)

        auctionService.fetchAuctions()
        dealService.fetchDeals()

        viewFrame.with(AuctionListView)
        viewFrame.with(CreateAuctionView)
    }

    override suspend fun onEnableAsync() {
        auctionCommand()
    }

    override suspend fun onDisableAsync() {
        databaseLoader.disconnect()
    }
}