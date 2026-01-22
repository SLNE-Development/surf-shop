package dev.slne.surf.shop.auction.core.auction.service

import com.github.benmanes.caffeine.cache.Caffeine
import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.leftJoin
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.shop.auction.api.auction.Auction
import dev.slne.surf.shop.auction.api.auction.bid.AuctionBid
import dev.slne.surf.shop.auction.core.auction.AuctionImpl
import dev.slne.surf.shop.auction.core.auction.db.tables.AuctionBidsTable
import dev.slne.surf.shop.auction.core.auction.db.tables.AuctionsTable
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.toObjectList
import it.unimi.dsi.fastutil.objects.ObjectList
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import org.jetbrains.annotations.Unmodifiable
import java.time.OffsetDateTime
import java.util.*

object AuctionServiceImpl : AuctionService {
    private val log = logger()

    private val auctionCache = Caffeine.newBuilder()
        .build<UUID, Auction>()

    override val auctions: @Unmodifiable ObjectList<Auction>
        get() = auctionCache.asMap().values.toObjectList()

    override suspend fun cacheAuctions() {
        val auctions = findAuctions()

        log.atInfo().log("Caching ${auctions.size} auctions")

        auctionCache.invalidateAll()
        auctionCache.putAll(auctions.associateBy { it.uuid })
    }

    override fun cacheAuction(auction: Auction) {
        auctionCache.put(auction.uuid, auction)
    }

    override suspend fun createAuction(
        ownerUuid: UUID,
        itemData: String,
        startingBid: Int,
        instantBuyEnabled: Boolean,
        instantBuyPrice: Int?,
        startsAt: OffsetDateTime,
        endsAt: OffsetDateTime
    ): Auction {
        val auction = AuctionImpl(
            uuid = UUID.randomUUID(),
            ownerUuid = ownerUuid,
            itemData = itemData,
            startingBid = startingBid,
            instantBuyEnabled = instantBuyEnabled,
            instantBuyPrice = instantBuyPrice,
            startsAt = startsAt,
            endsAt = endsAt,
            serverName = surfCoreApi.getCurrentServerName(),
        )

        cacheAuction(auction)

        suspendTransaction {
            AuctionsTable.insert {
                it[AuctionsTable.uuid] = auction.uuid
                it[AuctionsTable.ownerUuid] = auction.ownerUuid
                it[AuctionsTable.itemData] = auction.itemData
                it[AuctionsTable.startingBid] = auction.startingBid
                it[AuctionsTable.instantBuyEnabled] = auction.instantBuyEnabled
                it[AuctionsTable.instantBuyPrice] = auction.instantBuyPrice
                it[AuctionsTable.startsAt] = auction.startsAt
                it[AuctionsTable.endsAt] = auction.endsAt
                it[AuctionsTable.serverName] = auction.serverName
            }
        }

        return auction
    }

    override suspend fun findAuctions(): ObjectList<Auction> = suspendTransaction {
        val rows = AuctionsTable
            .leftJoin(AuctionBidsTable, { id }, { auction })
            .selectAll()
            .toList()

        rows.groupBy { it[AuctionsTable.id] }.mapNotNull { (_, groupedRows) ->
            val first = groupedRows.first()

            @Suppress("UNNECESSARY_SAFE_CALL")
            val bids = groupedRows.mapNotNull { row ->
                row[AuctionBidsTable.id]?.let {
                    AuctionBid(
                        bidderUuid = row[AuctionBidsTable.bidderUuid],
                        amount = row[AuctionBidsTable.amount],
                        timestamp = row[AuctionBidsTable.createdAt]
                    )
                }
            }.toObjectList()

            AuctionImpl(
                uuid = first[AuctionsTable.uuid],
                ownerUuid = first[AuctionsTable.ownerUuid],
                itemData = first[AuctionsTable.itemData],
                startingBid = first[AuctionsTable.startingBid],
                instantBuyEnabled = first[AuctionsTable.instantBuyEnabled],
                instantBuyPrice = first[AuctionsTable.instantBuyPrice],
                startsAt = first[AuctionsTable.startsAt],
                endsAt = first[AuctionsTable.endsAt],
                serverName = first[AuctionsTable.serverName],
                bids = bids
            )
        }.toObjectList()
    }

    override suspend fun placeBid(auction: Auction, bidder: SurfPlayer, amount: Int) {
        val bid = AuctionBid(
            bidderUuid = bidder.uuid,
            amount = amount,
            timestamp = OffsetDateTime.now()
        )

        if (auction !is AuctionImpl) return

        // TODO: Remove transaction amount

        auction.cacheBid(bid)

        suspendTransaction {
            val dbAuction = AuctionsTable.selectAll().where {
                AuctionsTable.uuid eq auction.uuid
            }.single()

            AuctionBidsTable.insert { row ->
                row[AuctionBidsTable.auction] = dbAuction[AuctionsTable.id]
                row[AuctionBidsTable.bidderUuid] = bid.bidderUuid
                row[AuctionBidsTable.amount] = bid.amount
                row[AuctionBidsTable.createdAt] = bid.timestamp
            }
        }
    }

    override suspend fun instantBuy(auction: Auction, buyer: SurfPlayer) {
        TODO("Not yet implemented")
    }
}