package dev.slne.base.core.instance;

public class CoreApi {

    private static CoreInstance instance;

    /**
     * Sets the instance of the plugin
     * 
     * @param instance The instance of the plugin
     */
    public static void setInstance(CoreInstance instance) {
        CoreApi.instance = instance;
    }

    /**
     * Returns the instance of the plugin
     * 
     * @return The instance of the plugin
     */
    public static CoreInstance getInstance() {
        return instance;
    }

}
