package dev.slne.surf.shop.core.util

import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.service.dealService

val Shop.dealCount get() = dealService.loadedDeals.count { it.shopInternalId == this.internalId }