package dev.slne.shop.bukkit.instance;

public class BukkitApi {

    private static BukkitInstance instance;

    /**
     * Private constructor to hide the implicit public one
     */
    private BukkitApi() {
    }

    /**
     * Sets the instance of the plugin
     *
     * @param instance The instance of the plugin
     */
    public static void setInstance(BukkitInstance instance) {
        BukkitApi.instance = instance;
    }

    /**
     * Returns the instance of the plugin
     *
     * @return The instance of the plugin
     */
    public static BukkitInstance getInstance() {
        return instance;
    }

}
