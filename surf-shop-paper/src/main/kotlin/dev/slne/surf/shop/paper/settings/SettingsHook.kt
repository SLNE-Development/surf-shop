package dev.slne.surf.shop.paper.settings

import dev.slne.surf.api.paper.util.namespacedKey
import dev.slne.surf.settings.api.SurfSettingsApi
import dev.slne.surf.settings.api.setting.SettingKey
import java.util.UUID

object SettingsHook {
    private val dealMadeMessagesKey = SettingKey.ofBoolean(namespacedKey("deal-made-messages"), true)
    private val shopSoundsKey = SettingKey.ofBoolean(namespacedKey("click-sounds"), true)

    fun hasDealMadeMessagesEnabled(playerUuid: UUID) =
        SurfSettingsApi.getSettingValue(playerUuid, dealMadeMessagesKey)

    fun hasShopSoundsEnabled(playerUuid: UUID) =
        SurfSettingsApi.getSettingValue(playerUuid, shopSoundsKey)


    suspend fun setHasDealMadeMessagesEnabled(playerUuid: UUID, enabled: Boolean) =
        SurfSettingsApi.saveSetting(playerUuid, dealMadeMessagesKey, enabled)

    suspend fun setShopSoundsEnabled(playerUuid: UUID, enabled: Boolean) =
        SurfSettingsApi.saveSetting(playerUuid, shopSoundsKey, enabled)


    suspend fun registerSettings() {
        SurfSettingsApi.createSetting(dealMadeMessagesKey)
        SurfSettingsApi.createSetting(shopSoundsKey)
    }
}