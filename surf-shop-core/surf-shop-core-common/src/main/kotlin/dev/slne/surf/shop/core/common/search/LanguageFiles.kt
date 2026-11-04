package dev.slne.surf.shop.core.common.search

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import kotlin.io.path.createDirectories
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

/**
 * Loads Minecraft language files (e.g. `de_de`) for the shop search.
 *
 * A language the server ships itself ([bundled], in practice `en_us`) is used directly. Every other language is
 * downloaded from Mojang for [minecraftVersion] (version manifest → version JSON → asset index → object) and cached
 * in [cacheDir]/[minecraftVersion], so later starts need no network. Only item, block, enchantment and effect names
 * are kept.
 *
 * @param fetch downloads a URL; throws on any failure
 * @param bundled returns a language file the server provides itself, or `null`
 */
class LanguageFiles(
    private val cacheDir: Path,
    private val minecraftVersion: String,
    private val fetch: suspend (url: String) -> ByteArray,
    private val bundled: (language: String) -> ByteArray?
) {
    private val versionDir = cacheDir.resolve(minecraftVersion)

    /** Asset hashes by path for [minecraftVersion]; fetched at most once per instance. */
    private var assetHashes: Map<String, String>? = null

    /**
     * Loads [languages]; a language that cannot be loaded is passed to [onFailure] and skipped.
     * Removes cached files of other Minecraft versions.
     *
     * @return the names of each loaded language by translation key, keyed by the lowercased language code
     */
    suspend fun loadAll(
        languages: List<String>,
        onFailure: (language: String, cause: Exception) -> Unit
    ): Map<String, Map<String, String>> {
        removeOtherVersions()

        return buildMap {
            for (language in languages.map { it.trim().lowercase() }.distinct()) {
                try {
                    put(language, load(language))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    onFailure(language, e)
                }
            }
        }
    }

    private suspend fun load(language: String): Map<String, String> {
        bundled(language)?.let { return searchNames(it) }

        val cached = versionDir.resolve("$language.json")
        if (cached.exists()) {
            try {
                return searchNames(cached.readBytes())
            } catch (_: IllegalArgumentException) {
                // Not valid JSON (e.g. damaged on disk): download it again instead of losing the language.
                cached.deleteIfExists()
            }
        }

        val names = searchNames(download(language))
        writeCache(cached, Json.encodeToString(JsonObject(names.mapValues { JsonPrimitive(it.value) })).toByteArray())
        return names
    }

    private suspend fun download(language: String): ByteArray {
        val hashes = assetHashes ?: fetchAssetHashes().also { assetHashes = it }
        val hash = hashes["minecraft/lang/$language.json"]
            ?: error("Language $language is not available for Minecraft $minecraftVersion")

        val bytes = fetch(objectUrl(hash))
        val actual = sha1(bytes)
        check(actual == hash) { "Language $language has hash $actual instead of $hash" }
        return bytes
    }

    private suspend fun fetchAssetHashes(): Map<String, String> {
        val manifest = parse(fetch(VERSION_MANIFEST_URL))
        val versionUrl = manifest.getValue("versions").jsonArray
            .map { it.jsonObject }
            .firstOrNull { it.getValue("id").jsonPrimitive.content == minecraftVersion }
            ?.getValue("url")?.jsonPrimitive?.content
            ?: error("Minecraft $minecraftVersion is not in Mojang's version manifest")

        val indexUrl = parse(fetch(versionUrl)).getValue("assetIndex").jsonObject.getValue("url").jsonPrimitive.content
        val objects = parse(fetch(indexUrl)).getValue("objects").jsonObject

        return objects.mapValues { it.value.jsonObject.getValue("hash").jsonPrimitive.content }
    }

    /** Best effort: if the cache cannot be written, the language is still used and downloaded again next start. */
    private fun writeCache(file: Path, bytes: ByteArray) {
        val temp = file.resolveSibling("${file.name}.tmp")
        try {
            versionDir.createDirectories()
            temp.writeBytes(bytes)
            temp.moveTo(file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: IOException) {
            runCatching { temp.deleteIfExists() }
        }
    }

    /** Best effort: a folder that cannot be deleted must not keep the current languages from loading. */
    @OptIn(kotlin.io.path.ExperimentalPathApi::class)
    private fun removeOtherVersions() {
        try {
            if (!cacheDir.isDirectory()) return
            cacheDir.listDirectoryEntries()
                .filter { it.isDirectory() && it.name != minecraftVersion }
                .forEach { it.deleteRecursively() }
        } catch (_: IOException) {
        }
    }

    companion object {
        const val VERSION_MANIFEST_URL = "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json"

        private val SEARCH_KEY_PREFIXES = listOf("item.", "block.", "enchantment.", "effect.")

        /** Download URL of an asset object with the given SHA-1 [hash]. */
        fun objectUrl(hash: String) = "https://resources.download.minecraft.net/${hash.take(2)}/$hash"

        private fun parse(bytes: ByteArray): JsonObject = Json.parseToJsonElement(bytes.decodeToString()).jsonObject

        /** The item, block, enchantment and effect names of a language file. */
        private fun searchNames(bytes: ByteArray): Map<String, String> = buildMap {
            for ((key, value) in parse(bytes)) {
                if (SEARCH_KEY_PREFIXES.none { key.startsWith(it) }) continue
                val name = (value as? JsonPrimitive)?.takeIf { it.isString }?.content ?: continue
                put(key, name)
            }
        }

        private fun sha1(bytes: ByteArray) =
            MessageDigest.getInstance("SHA-1").digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
