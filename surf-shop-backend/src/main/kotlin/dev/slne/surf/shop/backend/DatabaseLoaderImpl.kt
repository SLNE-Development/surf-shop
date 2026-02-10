package dev.slne.surf.shop.backend

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.shop.backend.table.AuctionsTable
import dev.slne.surf.shop.backend.table.DealsTable
import dev.slne.surf.shop.core.database.DatabaseLoader
import net.kyori.adventure.util.Services
import java.nio.file.Path

@AutoService(DatabaseLoader::class)
class DatabaseLoaderImpl : DatabaseLoader, Services.Fallback {
    lateinit var databaseApi: DatabaseApi
    override suspend fun connect(dataPath: Path) {
        databaseApi = DatabaseApi.create(dataPath)

        suspendTransaction {
            SchemaUtils.create(
                AuctionsTable, DealsTable
            )
        }
    }

    override fun disconnect() {
        databaseApi.shutdown()
    }
}