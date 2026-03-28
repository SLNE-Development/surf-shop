package dev.slne.surf.shop.backend

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.shop.backend.rabbit.handler.DealHandler
import dev.slne.surf.shop.backend.rabbit.handler.ShopHandler
import dev.slne.surf.shop.backend.rabbit.handler.StaticShopChestHandler
import dev.slne.surf.shop.backend.table.DealsTable
import dev.slne.surf.shop.backend.table.ShopsTable
import dev.slne.surf.shop.backend.table.StaticShopChestsTable
import kotlin.io.path.Path

@AutoService(Microservice::class)
class ShopMicroservice : Microservice() {
    private val databaseApi = DatabaseApi.create(Path("config"))
    private val rabbitApi = ServerRabbitMQApi.create("surf-shop", Path("config"))

    override suspend fun onBootstrap(args: List<String>) {
        suspendTransaction {
            SchemaUtils.create(
                DealsTable,
                ShopsTable,
                StaticShopChestsTable
            )
        }

        rabbitApi.registerRequestHandler(DealHandler)
        rabbitApi.registerRequestHandler(ShopHandler)
        rabbitApi.registerRequestHandler(StaticShopChestHandler)
        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}