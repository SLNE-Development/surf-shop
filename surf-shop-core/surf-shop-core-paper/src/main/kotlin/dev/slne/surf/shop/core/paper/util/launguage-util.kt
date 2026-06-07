package dev.slne.surf.shop.core.paper.util

import com.google.gson.JsonParser
import dev.slne.surf.shop.core.common.util.logger
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.Material
import org.bukkit.Registry
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffectType
import java.io.File
import java.io.InputStreamReader
import java.net.JarURLConnection
import java.util.*

private val languageJsonCache = mutableMapOf<String, Map<String, String>>()

private fun getAvailableLanguages(): List<String> { //TODO: rework? dont know what this does, but works (lol)
    val resource = object {}.javaClass.classLoader.getResource("languages") ?: return emptyList()
    return try {
        when (resource.protocol) {
            "file" -> {
                val dir = File(resource.toURI())
                dir.listFiles { file -> file.isFile && file.name.endsWith(".json") }
                    ?.map { it.name.removeSuffix(".json") } ?: emptyList()
            }
            "jar" -> {
                val conn = resource.openConnection()
                val jar = (conn as JarURLConnection).jarFile
                jar.entries().asSequence()
                    .map { it.name }
                    .filter { it.startsWith("languages/") && it.endsWith(".json") }
                    .map { it.substringAfterLast('/').removeSuffix(".json") }
                    .toList()
            }
            else -> {
                emptyList()
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

private fun getLanguageJson(lang: String): Map<String, String> {
    return languageJsonCache.getOrPut(lang) {
        try {
            val resourceStream =
                object {}.javaClass.classLoader
                    .getResourceAsStream("languages/$lang.json") ?: return@getOrPut emptyMap()
            val jsonObject = JsonParser.parseReader(InputStreamReader(resourceStream)).asJsonObject
            jsonObject.entrySet().associate { it.key to it.value.asString }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }
}

private val materialTranslationCache = mutableMapOf<Material, Map<String, String>>()
private val enchantmentTranslationCache = mutableMapOf<Enchantment, Map<String, String>>()
private val potionTranslationCache = mutableMapOf<PotionEffectType, Map<String, String>>()

fun populateTranslationCaches() {
    materialTranslationCache.clear()
    enchantmentTranslationCache.clear()
    potionTranslationCache.clear()

    val languages = getAvailableLanguages()

    logger.info("Found ${languages.size} language files for translation caching. [$languages]")

    Material.entries.forEach { material ->
        materialTranslationCache[material] = getMaterialTranslations(material, languages)
    }

    RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).forEach { ench ->
        enchantmentTranslationCache[ench] = getEnchantmentTranslations(ench, languages)
    }

    Registry.POTION_EFFECT_TYPE.forEach { effect ->
        potionTranslationCache[effect] = getPotionTranslations(effect, languages)
    }
}


private fun getMaterialTranslations(
    material: Material,
    languages: List<String>
): Map<String, String> {
    val key = material.key.key.lowercase(Locale.getDefault())
    val keysToTry = listOf(
        "block.minecraft.$key",
        "item.minecraft.$key"
    )

    return languages.mapNotNull { lang ->
        val json = getLanguageJson(lang)
        val match = keysToTry.firstNotNullOfOrNull { json[it] }
        match?.let { lang to it }
    }.toMap()
}

private fun getEnchantmentTranslations(
    enchantment: Enchantment,
    languages: List<String>
): Map<String, String> {
    val key = enchantment.key.key.lowercase(Locale.getDefault())
    val lookup = "enchantment.minecraft.$key"

    return languages.mapNotNull { lang ->
        val json = getLanguageJson(lang)
        json[lookup]?.let { lang to it }
    }.toMap()
}

private fun getPotionTranslations(
    effect: PotionEffectType,
    languages: List<String>
): Map<String, String> {
    val key = effect.key.key.lowercase(Locale.getDefault())
    val lookup = "effect.minecraft.$key"

    return languages.mapNotNull { lang ->
        val json = getLanguageJson(lang)
        json[lookup]?.let { lang to it }
    }.toMap()
}


fun getAllCachedTranslationsFor(itemStack: ItemStack): Set<String> {
    return buildSet {
        val material = itemStack.type
        val meta = itemStack.itemMeta

        materialTranslationCache[material]?.values?.forEach { add(it) }

        itemStack.enchantments.keys.forEach { ench ->
            enchantmentTranslationCache[ench]?.values?.forEach { add(it) }
        }

        if (meta is PotionMeta) {
            meta.basePotionType?.effectType?.let { effectType ->
                potionTranslationCache[effectType]?.values?.forEach { add(it) }
            }
            meta.customEffects.forEach { effect ->
                potionTranslationCache[effect.type]?.values?.forEach { add(it) }
            }
        }
    }
}