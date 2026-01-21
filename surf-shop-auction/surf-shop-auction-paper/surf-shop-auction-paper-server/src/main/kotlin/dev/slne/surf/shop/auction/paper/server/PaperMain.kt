package dev.slne.surf.shop.auction.paper.server

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.shop.auction.core.auction.db.tables.AuctionBidsTable
import dev.slne.surf.shop.auction.core.auction.db.tables.AuctionsTable
import dev.slne.surf.shop.auction.core.auction.service.AuctionService
import dev.slne.surf.shop.auction.paper.server.commands.auctionCommand
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {
    private lateinit var databaseApi: DatabaseApi

    override suspend fun onLoadAsync() {
        databaseApi = DatabaseApi.create(pluginPath = dataPath)

        suspendTransaction {
            SchemaUtils.create(
                AuctionsTable,
                AuctionBidsTable
            )
        }

        AuctionService.cacheAuctions()
    }

    override suspend fun onEnableAsync() {
        auctionCommand()
    }

    override suspend fun onDisableAsync() {
        databaseApi.shutdown()
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)