package dev.slne.surf.shop.api.shop.transaction;

public enum ShopTransactionResult {

    /**
     * The transaction failed.
     */
    FAILED,

    /**
     * The transaction was cancelled.
     */
    CANCELLED,

    /**
     * The transaction was successful.
     */
    SUCCESS
}
