package dev.slne.surf.shop.core.common

import com.google.auto.service.AutoService
import dev.slne.surf.shop.api.common.utils.InternalContextHolder
import dev.slne.surf.shop.api.common.utils.InternalShopApi
import org.springframework.context.ApplicationContext

@AutoService(InternalContextHolder::class)
@OptIn(InternalShopApi::class)
class ContextHolderImpl : InternalContextHolder {

    override lateinit var context: ApplicationContext

    companion object {
        val INSTANCE = InternalContextHolder.INSTANCE as ContextHolderImpl
    }
}