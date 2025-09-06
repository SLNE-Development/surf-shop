package dev.slne.surf.shop.core.common.impl

import dev.slne.surf.surfapi.core.api.util.requiredService

private val bridge = requiredService<InternalShopBridge>()

interface InternalShopBridge {

    companion object : InternalShopBridge by bridge {
        val INSTANCE get() = bridge
    }

}