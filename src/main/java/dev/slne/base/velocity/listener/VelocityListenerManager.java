package dev.slne.base.velocity.listener;

import com.velocitypowered.api.event.EventManager;

import dev.slne.base.velocity.VelocityMain;

public class VelocityListenerManager {

    /**
     * Registers all listeners
     */
    public void registerListeners() {

    }

    /**
     * Unregisters all listeners
     */
    public void unregisterListeners() {
        EventManager eventManager = VelocityMain.getInstance().getProxyServer().getEventManager();
        VelocityMain plugin = VelocityMain.getInstance();

        eventManager.unregisterListeners(plugin);
    }

    /**
     * Registers one listener to the {@link EventManager}
     * 
     * @param listener the listener to register
     */
    public void registerListener(Object listener) {
        EventManager eventManager = VelocityMain.getInstance().getProxyServer().getEventManager();
        VelocityMain plugin = VelocityMain.getInstance();

        eventManager.register(plugin, listener);
    }

}
