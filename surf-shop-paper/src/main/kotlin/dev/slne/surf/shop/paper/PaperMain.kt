package dev.slne.surf.shop.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.core.util.mutableObject2ObjectMapOf
import dev.slne.surf.api.paper.event.register
import dev.slne.surf.api.paper.extensions.pluginManager
import dev.slne.surf.api.paper.inventory.framework.viewFrame
import dev.slne.surf.shop.api.shop.ShopSortingType
import dev.slne.surf.shop.core.common.service.DealService
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.service.StaticShopChestService
import dev.slne.surf.shop.core.paper.PaperShopInstance
import dev.slne.surf.shop.core.paper.service.DealServiceImpl
import dev.slne.surf.shop.paper.chest.ShopChestListener
import dev.slne.surf.shop.paper.chest.ShopChestRecipe
import dev.slne.surf.shop.paper.chest.ShopChestSelectShopView
import dev.slne.surf.shop.paper.chest.ShopChestSetupView
import dev.slne.surf.shop.paper.command.shopCommand
import dev.slne.surf.shop.paper.hook.AuxProtectHook
import dev.slne.surf.shop.paper.hook.SurfNpcHook
import dev.slne.surf.shop.paper.menu.CreateShopView
import dev.slne.surf.shop.paper.menu.OwnShopsListView
import dev.slne.surf.shop.paper.menu.ShopListView
import dev.slne.surf.shop.paper.menu.buy.BuyShopItemView
import dev.slne.surf.shop.paper.menu.deal.DoneDealsView
import dev.slne.surf.shop.paper.menu.delete.DeleteShopView
import dev.slne.surf.shop.paper.menu.edit.EditShopView
import dev.slne.surf.shop.paper.menu.edit.PriceEditView
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageInsertView
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageRemoveView
import dev.slne.surf.shop.paper.menu.edit.storage.ItemStorageView
import dev.slne.surf.shop.paper.menu.select.PlayerInventorySelectItemView
import dev.slne.surf.shop.paper.menu.select.PriceSelectView
import dev.slne.surf.shop.paper.settings.SettingsHook
import dev.slne.surf.shop.paper.settings.hasSettingsApi
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
        viewFrame.with(OwnShopsListView)
        viewFrame.with(ShopChestSetupView)
        viewFrame.with(ShopChestSelectShopView)
        viewFrame.with(DoneDealsView)

        if (hasSettingsApi()) {
            SettingsHook.registerSettings()
        }
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
    }

    override suspend fun onDisableAsync() {
        PaperShopInstance.paperLoader.onDisable()
    }

    val hasFancyHolograms get() = pluginManager.isPluginEnabled("FancyHolograms")
    val auxProtectHook get() = pluginManager.isPluginEnabled("AuxProtect")
    val surfNpcHook get() = pluginManager.isPluginEnabled("surf-npc-paper")
}