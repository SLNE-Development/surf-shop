package dev.slne.surf.shop.auction.core.auction

import dev.slne.surf.core.api.common.player.SurfPlayer
import dev.slne.surf.core.api.common.server.SurfServer
import dev.slne.surf.core.api.common.surfCoreApi
import dev.slne.surf.shop.auction.api.auction.Auction
import dev.slne.surf.shop.auction.api.auction.bid.AuctionBid
import dev.slne.surf.shop.auction.core.auction.service.AuctionsService
import dev.slne.surf.surfapi.core.api.serializer.java.datetime.datetime.offset.SerializableOffsetDateTime
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import dev.slne.surf.surfapi.core.api.util.freeze
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable(with = AuctionSerializer::class)
class AuctionImpl(
    override val uuid: SerializableUUID,
    val ownerUuid: SerializableUUID,
    override val itemData: String,
    override val startingBid: Int,
    override val instantBuyEnabled: Boolean,
    override val instantBuyPrice: Int?,
    override val startsAt: SerializableOffsetDateTime,
    override val endsAt: SerializableOffsetDateTime,
    override val serverName: String,
    bids: List<AuctionBid>
) : Auction {
    private val bidMutex = Mutex()

    @Transient
    private val _bids = mutableObjectListOf(bids)
    override val bids get() = _bids.freeze()
    override val currentBid: AuctionBid?
        get() = _bids.maxByOrNull { it.timestamp }

    override val server: SurfServer?
        get() = surfCoreApi.getServerByName(serverName)

    override suspend fun placeBid(
        bidder: SurfPlayer,
        amount: Int
    ) = bidMutex.withLock {
        AuctionsService.placeBid(this, bidder, amount)
    }

    fun cacheBid(bid: AuctionBid) = _bids.add(bid)

    override suspend fun instantBuy(buyer: SurfPlayer) = bidMutex.withLock {
        AuctionsService.instantBuy(this, buyer)
    }

    override suspend fun owner() = surfCoreApi.getOfflinePlayer(ownerUuid)
        ?: error("Owner with UUID $ownerUuid not found")
}