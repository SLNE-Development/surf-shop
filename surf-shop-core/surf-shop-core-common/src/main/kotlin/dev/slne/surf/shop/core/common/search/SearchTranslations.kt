package dev.slne.surf.shop.core.common.search

import net.kyori.adventure.translation.Translatable

/**
 * Translated names per translation key (e.g. `item.minecraft.diamond_sword`) in every loaded search language,
 * lowercased for matching against search input.
 */
class SearchTranslations(languages: Collection<Map<String, String>>) {
    private val names: Map<String, List<String>> = buildMap<String, MutableList<String>> {
        for (language in languages) {
            for ((key, name) in language) {
                val names = getOrPut(key) { mutableListOf() }
                val lowercase = name.lowercase()
                if (lowercase !in names) names += lowercase
            }
        }
    }

    /** The lowercased names of [translationKey] in every loaded language; empty if unknown. */
    fun namesFor(translationKey: String): List<String> = names[translationKey].orEmpty()

    /**
     * The lowercased names of [translatable] in every loaded language; empty if it has no translation key.
     * Paper throws for enchantments whose description is plain text (custom enchantments), so this never throws.
     */
    fun namesOf(translatable: Translatable): List<String> {
        val key = try {
            translatable.translationKey()
        } catch (_: RuntimeException) {
            return emptyList()
        }
        return namesFor(key)
    }

    companion object {
        val EMPTY = SearchTranslations(emptyList())

        /** The translations used for search tokens; [EMPTY] until the languages were loaded. */
        @Volatile
        var current: SearchTranslations = EMPTY
    }
}
