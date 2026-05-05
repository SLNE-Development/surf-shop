package dev.slne.surf.shop.paper.settings

import dev.slne.surf.api.paper.extensions.pluginManager

fun hasSettingsApi() = pluginManager.isPluginEnabled("surf-settings-paper")