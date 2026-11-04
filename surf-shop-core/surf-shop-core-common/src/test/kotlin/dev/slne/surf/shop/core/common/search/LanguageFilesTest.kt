package dev.slne.surf.shop.core.common.search

import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LanguageFilesTest {

    private val cacheDir: Path = Files.createTempDirectory("search-translations")

    @AfterTest
    fun cleanUp() {
        cacheDir.toFile().deleteRecursively()
    }

    private val germanJson = """
        {
          "item.minecraft.diamond_sword": "Diamantschwert",
          "block.minecraft.oak_planks": "Eichenholzbretter",
          "enchantment.minecraft.sharpness": "Schärfe",
          "effect.minecraft.strength": "Stärke",
          "gui.done": "Fertig",
          "options.title": "Optionen"
        }
    """.trimIndent().toByteArray()

    private val frenchJson = """{ "item.minecraft.diamond_sword": "Épée en diamant" }""".toByteArray()

    /** Serves a fake Mojang version manifest, version JSON, asset index and language objects. */
    private inner class FakeMojang(
        version: String = "26.3",
        languages: Map<String, ByteArray> = mapOf("de_de" to germanJson, "fr_fr" to frenchJson),
        hashOverrides: Map<String, String> = emptyMap()
    ) {
        val requests = mutableListOf<String>()
        private val responses = mutableMapOf<String, ByteArray>()

        init {
            responses[LanguageFiles.VERSION_MANIFEST_URL] = """
                {"latest":{"release":"$version"},"versions":[
                  {"id":"26.2","url":"https://meta.test/26.2.json"},
                  {"id":"$version","url":"https://meta.test/version.json"}
                ]}
            """.trimIndent().toByteArray()
            responses["https://meta.test/version.json"] =
                """{"assetIndex":{"id":"34","url":"https://meta.test/index.json"}}""".toByteArray()

            val objects = languages.entries.joinToString(",") { (language, json) ->
                // Served under the advertised hash, so an override tests the hash check, not a missing object.
                val hash = hashOverrides[language] ?: sha1(json)
                responses[LanguageFiles.objectUrl(hash)] = json
                """"minecraft/lang/$language.json":{"hash":"$hash","size":${json.size}}"""
            }
            responses["https://meta.test/index.json"] = """{"objects":{$objects}}""".toByteArray()
        }

        suspend fun fetch(url: String): ByteArray {
            requests += url
            return responses[url] ?: throw java.io.IOException("404 $url")
        }
    }

    private fun sha1(bytes: ByteArray) =
        MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun languageFiles(
        mojang: FakeMojang,
        version: String = "26.3",
        bundled: Map<String, ByteArray> = emptyMap()
    ) = LanguageFiles(cacheDir, version, mojang::fetch) { bundled[it] }

    private fun LanguageFiles.loadAllCollectingFailures(
        languages: List<String>,
        failures: MutableMap<String, Exception> = mutableMapOf()
    ) = runBlocking { loadAll(languages) { language, cause -> failures[language] = cause } }

    @Test
    fun `downloaded language keeps only item, block, enchantment and effect names`() {
        val loaded = languageFiles(FakeMojang()).loadAllCollectingFailures(listOf("de_de"))

        assertEquals(
            mapOf(
                "item.minecraft.diamond_sword" to "Diamantschwert",
                "block.minecraft.oak_planks" to "Eichenholzbretter",
                "enchantment.minecraft.sharpness" to "Schärfe",
                "effect.minecraft.strength" to "Stärke"
            ),
            loaded["de_de"]
        )
    }

    @Test
    fun `language provided by the server is used without downloading`() {
        val mojang = FakeMojang()
        val english = """{ "item.minecraft.cooked_beef": "Steak", "gui.done": "Done" }""".toByteArray()

        val loaded = languageFiles(mojang, bundled = mapOf("en_us" to english))
            .loadAllCollectingFailures(listOf("en_us"))

        assertEquals(mapOf("item.minecraft.cooked_beef" to "Steak"), loaded["en_us"])
        assertTrue(mojang.requests.isEmpty())
    }

    @Test
    fun `language codes are case-insensitive`() {
        val loaded = languageFiles(FakeMojang()).loadAllCollectingFailures(listOf("de_DE"))

        assertEquals("Diamantschwert", loaded["de_de"]?.get("item.minecraft.diamond_sword"))
    }

    @Test
    fun `downloaded language is cached and loaded without network on the next start`() {
        languageFiles(FakeMojang()).loadAllCollectingFailures(listOf("de_de"))

        val offline = FakeMojang(languages = emptyMap())
        val loaded = languageFiles(offline).loadAllCollectingFailures(listOf("de_de"))

        assertEquals("Diamantschwert", loaded["de_de"]?.get("item.minecraft.diamond_sword"))
        assertTrue(offline.requests.isEmpty())
    }

    @Test
    fun `corrupt cached file is downloaded again`() {
        val cached = cacheDir.resolve("26.3").createDirectories().resolve("de_de.json")
        cached.writeText("{ broken")

        val loaded = languageFiles(FakeMojang()).loadAllCollectingFailures(listOf("de_de"))

        assertEquals("Diamantschwert", loaded["de_de"]?.get("item.minecraft.diamond_sword"))
        assertTrue(cached.readText().contains("Diamantschwert"))
    }

    @Test
    fun `asset index is fetched once for several languages`() {
        val mojang = FakeMojang()

        val loaded = languageFiles(mojang).loadAllCollectingFailures(listOf("de_de", "fr_fr"))

        assertEquals(setOf("de_de", "fr_fr"), loaded.keys)
        assertEquals(1, mojang.requests.count { it == LanguageFiles.VERSION_MANIFEST_URL })
        assertEquals(1, mojang.requests.count { it == "https://meta.test/index.json" })
    }

    @Test
    fun `download with a wrong hash is reported and not cached`() {
        val failures = mutableMapOf<String, Exception>()
        val mojang = FakeMojang(hashOverrides = mapOf("de_de" to "0".repeat(40)))

        val loaded = languageFiles(mojang).loadAllCollectingFailures(listOf("de_de"), failures)

        assertTrue(loaded.isEmpty())
        assertTrue("de_de" in failures)
        assertFalse(cacheDir.resolve("26.3").resolve("de_de.json").exists())
    }

    @Test
    fun `unknown language is reported while the others still load`() {
        val failures = mutableMapOf<String, Exception>()

        val loaded = languageFiles(FakeMojang()).loadAllCollectingFailures(listOf("xx_xx", "de_de"), failures)

        assertEquals(setOf("de_de"), loaded.keys)
        assertEquals(setOf("xx_xx"), failures.keys)
    }

    @Test
    fun `server version missing from the manifest is reported`() {
        val failures = mutableMapOf<String, Exception>()

        val loaded = languageFiles(FakeMojang(), version = "26.4-snapshot-1")
            .loadAllCollectingFailures(listOf("de_de"), failures)

        assertTrue(loaded.isEmpty())
        assertEquals(setOf("de_de"), failures.keys)
    }

    @Test
    fun `unreachable Mojang is reported`() {
        val failures = mutableMapOf<String, Exception>()
        val files = LanguageFiles(cacheDir, "26.3", { throw java.io.IOException("offline") }) { null }

        val loaded = files.loadAllCollectingFailures(listOf("de_de"), failures)

        assertTrue(loaded.isEmpty())
        assertEquals(setOf("de_de"), failures.keys)
    }

    @Test
    fun `cached files of other Minecraft versions are removed`() {
        cacheDir.resolve("26.2").createDirectories().resolve("de_de.json").writeText("{}")
        cacheDir.resolve("26.3").createDirectories().resolve("de_de.json").writeText(
            """{ "item.minecraft.stone": "Stein" }"""
        )

        languageFiles(FakeMojang()).loadAllCollectingFailures(listOf("de_de"))

        assertFalse(cacheDir.resolve("26.2").exists())
        assertTrue(cacheDir.resolve("26.3").resolve("de_de.json").readText().contains("Stein"))
    }
}
