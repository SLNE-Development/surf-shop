package dev.slne.surf.shop.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.shop.api.shop.ShopSortingType
import dev.slne.surf.shop.core.database.databaseLoader
import dev.slne.surf.shop.core.service.dealService
import dev.slne.surf.shop.core.service.shopService
import dev.slne.surf.shop.paper.command.shopCommand
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.hook.SurfNpcHook
import dev.slne.surf.shop.paper.menu.CreateShopView
import dev.slne.surf.shop.paper.menu.ShopListView
import dev.slne.surf.shop.paper.menu.buy.BuyShopItemView
import dev.slne.surf.shop.paper.menu.delete.DeleteShopView
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.menu.edit.PriceEditView
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageInsertView
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageRemoveView
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageView
import dev.slne.surf.shop.paper.menu.select.PlayerInventorySelectItemView
import dev.slne.surf.shop.paper.menu.select.PriceSelectView
import dev.slne.surf.surfapi.bukkit.api.event.register
import dev.slne.surf.surfapi.bukkit.api.extensions.pluginManager
import dev.slne.surf.surfapi.bukkit.api.inventory.framework.viewFrame
import dev.slne.surf.surfapi.core.api.util.mutableObject2ObjectMapOf
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)

class PaperMain : SuspendingJavaPlugin() {
    val sorts = mutableObject2ObjectMapOf<UUID, ShopSortingType>()
    fun getSorting(player: UUID) = sorts.getOrDefault(player, ShopSortingType.ITEM_NAME)
    fun setSorting(player: UUID, sortType: ShopSortingType) {
        sorts[player] = sortType
    }

    override suspend fun onLoadAsync() {
        databaseLoader.connect(plugin.dataPath)

        shopService.fetchShops()
        dealService.fetchDeals()

        viewFrame.with(ShopListView)
        viewFrame.with(CreateShopView)
        viewFrame.with(PlayerInventorySelectItemView)
        viewFrame.with(PriceSelectView)
        viewFrame.with(EditShopView)
        viewFrame.with(PriceEditView)
        viewFrame.with(ItemStorageView)
        viewFrame.with(ItemStorageInsertView)
        viewFrame.with(ItemStorageRemoveView)
        viewFrame.with(BuyShopItemView)
        viewFrame.with(DeleteShopView)
    }

    override suspend fun onEnableAsync() {
        if (auxProtectHook) {
            AuxProtectHook.create()
        }

        if (surfNpcHook) {
            SurfNpcHook.create()

            SurfNpcHook.NpcListener.register()
        }

        shopCommand()
    }

    override suspend fun onDisableAsync() {
        databaseLoader.disconnect()
    }

    val auxProtectHook get() = pluginManager.isPluginEnabled("AuxProtect")
    val surfNpcHook get() = pluginManager.isPluginEnabled("surf-npc-paper")
}