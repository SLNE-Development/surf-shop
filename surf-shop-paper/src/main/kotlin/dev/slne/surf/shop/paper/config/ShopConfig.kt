package dev.slne.surf.shop.paper.config

import dev.slne.surf.api.core.config.SpongeYmlConfigClass
import dev.slne.surf.shop.paper.plugin
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Comment

@ConfigSerializable
data class ShopConfig(
    @Comment(
        "Minecraft languages whose item, block, enchantment and effect names the shop search understands, " +
                "e.g. en_us or de_de. en_us comes from the server; other languages are downloaded from Mojang " +
                "for the server's Minecraft version on start and cached in search-translations/."
    )
    val searchLanguages: List<String> = listOf("en_us", "de_de")
) {
    companion object : SpongeYmlConfigClass<ShopConfig>(
        ShopConfig::class.java,
        plugin.dataPath,
        "config.yml"
    )
}
