package dev.slne.surf.shop.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.api.paper.inventory.framework.register
import dev.slne.surf.shop.api.shop.ShopSortingType
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StaticShopChestService
import dev.slne.surf.shop.core.paper.PaperShopInstance
import dev.slne.surf.shop.core.paper.service.DealServiceImpl
import dev.slne.surf.shop.paper.chest.ShopChestListener
import dev.slne.surf.shop.paper.chest.ShopChestRecipe
import dev.slne.surf.shop.paper.chest.shopChestSelectShopView
import dev.slne.surf.shop.paper.chest.shopChestSetupView
import dev.slne.surf.shop.paper.command.denyShopCommand
import dev.slne.surf.shop.paper.command.shopCommand
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.hook.SurfNpcHook
import dev.slne.surf.shop.paper.menu.buy.buyShopItemView
import dev.slne.surf.shop.paper.menu.createShopView
import dev.slne.surf.shop.paper.menu.deal.doneDealsView
import dev.slne.surf.shop.paper.menu.delete.deleteShopView
import dev.slne.surf.shop.paper.menu.edit.editShopView
import dev.slne.surf.shop.paper.menu.edit.priceEditView
import dev.slne.surf.shop.paper.menu.edit.storage.itemStorageInsertView
import dev.slne.surf.shop.paper.menu.edit.storage.itemStorageRemoveView
import dev.slne.surf.shop.paper.menu.edit.storage.itemStorageView
import dev.slne.surf.shop.paper.menu.ownShopsListView
import dev.slne.surf.shop.paper.menu.select.playerInventorySelectItemView
import dev.slne.surf.shop.paper.menu.select.priceSelectView
import dev.slne.surf.shop.paper.menu.shopListView
import org.bukkit.Bukkit
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
        PaperShopInstance.paperLoader.onLoad()
        DealServiceImpl.plugin = this

        shopListView.register()
        createShopView.register()
        priceSelectView.register()
        playerInventorySelectItemView.register()
        editShopView.register()
        priceEditView.register()
        itemStorageView.register()
        itemStorageInsertView.register()
        itemStorageRemoveView.register()
        buyShopItemView.register()
        deleteShopView.register()
        ownShopsListView.register()
        doneDealsView.register()

        shopChestSetupView.register()
        shopChestSelectShopView.register()
    }

    override suspend fun onEnableAsync() {
        PaperShopInstance.paperLoader.onEnable()
        if (auxProtectHook) {
            AuxProtectHook.create()
        }

        if (surfNpcHook) {
            SurfNpcHook.create()
            SurfNpcHook.NpcListener.register()
        }

        ShopService.fetchShops()
        DealService.fetchDeals()
        StaticShopChestService.fetchChests()

        Bukkit.addRecipe(ShopChestRecipe.createRecipe())
        ShopChestListener.register()

        shopCommand()
        denyShopCommand()
    }

    override suspend fun onDisableAsync() {
        PaperShopInstance.paperLoader.onDisable()
    }

    val hasFancyHolograms get() = pluginManager.isPluginEnabled("FancyHolograms")
    val auxProtectHook get() = pluginManager.isPluginEnabled("AuxProtect")
    val surfNpcHook get() = pluginManager.isPluginEnabled("surf-npc-paper")
}