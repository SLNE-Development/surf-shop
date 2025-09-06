@file:OptIn(InternalShopApi::class)

package dev.slne.surf.shop.api.common.utils

import dev.slne.surf.surfapi.core.api.util.requiredService
import org.springframework.context.ApplicationContext

private val holder = requiredService<InternalContextHolder>()

@InternalShopApi
interface InternalContextHolder {
    val context: ApplicationContext

    companion object : InternalContextHolder by holder {
        val INSTANCE get() = holder
    }
}