package dev.slne.surf.shop.core.common.search

import net.kyori.adventure.translation.Translatable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchTranslationsTest {

    @Test
    fun `namesFor returns the name in every loaded language in lowercase`() {
        val translations = SearchTranslations(
            listOf(
                mapOf("item.minecraft.diamond_sword" to "Diamond Sword"),
                mapOf("item.minecraft.diamond_sword" to "Diamantschwert")
            )
        )

        assertEquals(listOf("diamond sword", "diamantschwert"), translations.namesFor("item.minecraft.diamond_sword"))
    }

    @Test
    fun `namesFor lowercases umlauts`() {
        val translations = SearchTranslations(listOf(mapOf("enchantment.minecraft.sharpness" to "Schärfe")))

        assertEquals(listOf("schärfe"), translations.namesFor("enchantment.minecraft.sharpness"))
    }

    @Test
    fun `namesFor lists a name shared by several languages once`() {
        val translations = SearchTranslations(
            listOf(
                mapOf("item.minecraft.cooked_beef" to "Steak"),
                mapOf("item.minecraft.cooked_beef" to "Steak")
            )
        )

        assertEquals(listOf("steak"), translations.namesFor("item.minecraft.cooked_beef"))
    }

    @Test
    fun `namesOf looks up the translation key of the object`() {
        val translations = SearchTranslations(listOf(mapOf("item.minecraft.diamond_sword" to "Diamantschwert")))

        assertEquals(listOf("diamantschwert"), translations.namesOf(Translatable { "item.minecraft.diamond_sword" }))
    }

    @Test
    fun `namesOf skips objects without a translation key like custom enchantments`() {
        // Paper's CraftEnchantment throws this for enchantments whose description is plain text.
        val customEnchantment = Translatable { throw UnsupportedOperationException("Description isn't translatable!") }

        assertTrue(SearchTranslations(listOf(mapOf("a" to "b"))).namesOf(customEnchantment).isEmpty())
    }

    @Test
    fun `namesFor returns nothing for unknown keys`() {
        assertTrue(SearchTranslations(listOf(mapOf("a" to "b"))).namesFor("item.minecraft.stone").isEmpty())
        assertTrue(SearchTranslations.EMPTY.namesFor("item.minecraft.stone").isEmpty())
    }
}
