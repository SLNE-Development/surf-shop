package dev.slne.surf.shop.api.shop

enum class ShopSortingType {
    ITEM_NAME,
    PRICE_ASC,
    PRICE_DESC,
    TIME_ASC,
    TIME_DESC,
    MOST_STORED,
    MOST_DEALS,
    SELLER_NAME;

    fun next() = entries[(ordinal + 1) % entries.size]
    fun previous() = entries[(ordinal - 1 + entries.size) % entries.size]
}