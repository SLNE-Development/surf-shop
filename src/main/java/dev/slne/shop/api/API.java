package dev.slne.shop.api;

public class API {

    /**
     * Private constructor to hide the implicit public one
     */
    private API() {
    }

    private static final String API_ENDPOINT = "https://admin.slne.dev/api/v1/";

    public static final String SHOPS = API_ENDPOINT + "shop";
    public static final String SHOP = API_ENDPOINT + "shop/%s";

    public static final String SHOP_MEMBERS = API_ENDPOINT + "shop/%s/members";
    public static final String SHOP_MEMBER = API_ENDPOINT + "shop/%s/members/%s";

}
