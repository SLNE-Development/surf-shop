package dev.slne.surf.shop.core.util

import dev.slne.surf.shop.api.shop.Shop
import dev.slne.surf.shop.core.service.dealService
import dev.slne.surf.shop.core.service.shopService

val Shop.dealCount get() = dealService.loadedDeals.count { it.shopInternalId == this.internalId }
val Shop.updatedShop
    get() = shopService.loadedShops.firstOrNull { it.internalId == this.internalId }