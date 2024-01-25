package dev.slne.surf.shop.api.shop.transaction;

public enum ShopTransactionResult {

    /**
     * The transaction failed.
     */
    FAILED,

    /**
     * The transaction failed due to insufficient funds.
     */
    FAILED_INSUFFICIENT_FUNDS,

    /**
     * The transaction was cancelled.
     */
    CANCELLED,

    /**
     * The transaction was successful.
     */
    SUCCESS
}
