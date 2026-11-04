package dev.slne.surf.shop.core.paper.util

import dev.slne.surf.shop.core.common.search.LanguageFiles
import dev.slne.surf.shop.core.common.search.SearchTranslations
import dev.slne.surf.shop.core.common.service.ShopService
import dev.slne.surf.shop.core.common.util.logger
import io.papermc.paper.ServerBuildInfo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.time.Duration
import java.util.logging.Level

/**
 * Loads the search [languages] (e.g. `en_us`, `de_de`) into [SearchTranslations] and rebuilds the search tokens
 * of all loaded shops. A language that cannot be loaded is skipped with a warning; the search then matches the
 * item keys as before.
 *
 * @param cacheDir where downloaded language files are kept, one folder per Minecraft version
 * @param languages reads the configured languages; called here, so an invalid config only disables translated names
 */
suspend fun loadSearchTranslations(cacheDir: Path, languages: () -> List<String>) = withContext(Dispatchers.IO) {
    val minecraftVersion = ServerBuildInfo.buildInfo().minecraftVersionId()

    try {
        val loaded = newHttpClient().use { client ->
            LanguageFiles(cacheDir, minecraftVersion, { download(client, it) }, ::serverLanguageFile)
                .loadAll(languages()) { language, cause ->
                    logger.log(
                        Level.WARNING,
                        "Could not load search language $language for Minecraft $minecraftVersion; " +
                                "the search will not know its item names",
                        cause
                    )
                }
        }

        SearchTranslations.current = SearchTranslations(loaded.values)
        ShopService.loadedShopsSnapshot().forEach { it.rebuildSearchTokens() }

        logger.info("Search languages loaded: ${loaded.keys.joinToString().ifEmpty { "none" }}")
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        logger.log(Level.WARNING, "Could not load search languages; the search matches item keys only", e)
    }
}

/** Only used while loading and closed afterwards, so no client threads outlive a plugin reload. */
private fun newHttpClient(): HttpClient = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build()

private suspend fun download(client: HttpClient, url: String): ByteArray {
    val request = HttpRequest.newBuilder(URI(url))
        .timeout(Duration.ofSeconds(30))
        .header("User-Agent", "surf-shop")
        .GET()
        .build()

    val response = client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray()).await()
    check(response.statusCode() == 200) { "GET $url returned HTTP ${response.statusCode()}" }
    return response.body()
}

/** A language file shipped in the server jar; vanilla only ships `en_us`. */
private fun serverLanguageFile(language: String): ByteArray? =
    Bukkit.getServer().javaClass.getResourceAsStream("/assets/minecraft/lang/$language.json")?.use { it.readBytes() }
