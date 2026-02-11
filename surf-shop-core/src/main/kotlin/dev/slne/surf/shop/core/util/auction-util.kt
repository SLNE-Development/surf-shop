package dev.slne.surf.shop.core.util

import dev.slne.surf.shop.api.auction.Auction
import dev.slne.surf.shop.core.service.dealService

val Auction.dealCount get() = dealService.loadedDeals.count { it.auctionInternalId == this.internalId }