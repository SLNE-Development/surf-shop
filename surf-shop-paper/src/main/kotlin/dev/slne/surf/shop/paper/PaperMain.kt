package dev.slne.surf.shop.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.shop.api.auction.AuctionSortType
import dev.slne.surf.shop.core.database.databaseLoader
import dev.slne.surf.shop.core.service.auctionService
import dev.slne.surf.shop.core.service.dealService
import dev.slne.surf.shop.paper.command.auctionCommand
import dev.slne.surf.shop.paper.menu.AuctionListView
import dev.slne.surf.shop.paper.menu.CreateAuctionView
import dev.slne.surf.shop.paper.menu.edit.EditAuctionView
import dev.slne.surf.shop.paper.menu.edit.ItemStorageView
import dev.slne.surf.shop.paper.menu.edit.PriceEditView
import dev.slne.surf.shop.paper.menu.select.PlayerInventorySelectItemView
import dev.slne.surf.shop.paper.menu.select.PriceSelectView
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    private val sorts = mutableObject2ObjectMapOf<UUID, AuctionSortType>()
    fun getSorting(player: UUID) = sorts.getOrDefault(player, AuctionSortType.ITEM_NAME)
    fun setSorting(player: UUID, sortType: AuctionSortType) {
        sorts[player] = sortType
    }

    override suspend fun onLoadAsync() {
        databaseLoader.connect(plugin.dataPath)

        auctionService.fetchAuctions()
        dealService.fetchDeals()

        viewFrame.with(AuctionListView)
        viewFrame.with(CreateAuctionView)
        viewFrame.with(PlayerInventorySelectItemView)
        viewFrame.with(PriceSelectView)
        viewFrame.with(EditAuctionView)
        viewFrame.with(PriceEditView)
        viewFrame.with(ItemStorageView)
    }

    override suspend fun onEnableAsync() {
        auctionCommand()
    }

    override suspend fun onDisableAsync() {
        databaseLoader.disconnect()
    }
}