package dev.slne.surf.shop.server.api;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public class API {

    private static final String API_ENDPOINT = "https://admin.slne.dev/api/v1/";
    public static final String SHOPS = API_ENDPOINT + "shop";
    public static final String SHOP = API_ENDPOINT + "shop/%s";
    public static final String SHOP_TRANSACTIONS = API_ENDPOINT + "shop/%s/transactions";
    public static final String SHOP_TRANSACTION = API_ENDPOINT + "shop/%s/transactions/%s";
    public static final String SHOP_MEMBERS = API_ENDPOINT + "shop/%s/members";
    public static final String SHOP_MEMBER = API_ENDPOINT + "shop/%s/members/%s";

    /**
     * Private constructor to hide the implicit public one
     */
    private API() {
    }

}
