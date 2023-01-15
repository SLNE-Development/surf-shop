package dev.slne.base.velocity;

import java.nio.file.Path;

import org.slf4j.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import dev.slne.base.velocity.instance.VelocityApi;
import dev.slne.base.velocity.instance.VelocityInstance;

public class VelocityMain {

    private static VelocityMain instance;
    private static VelocityInstance velocityInstance;

    private ProxyServer proxyServer;
    private Logger logger;
    private Path dataDirectory;

    @Inject
    public VelocityMain(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        VelocityMain.instance = this;

        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        VelocityMain.velocityInstance = new VelocityInstance();
        VelocityApi.setInstance(velocityInstance);
        velocityInstance.onLoad();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        velocityInstance.onEnable();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        velocityInstance.onDisable();
    }

    /**
     * Returns the plugin id which has to be the same as the one in
     * velocity-plugin.json
     * 
     * Used by packet events
     * 
     * @return the plugin id
     */
    public String getPluginId() {
        return "surf-base";
    }

    /**
     * Returns the instance of the plugin
     * 
     * @return the instance of the plugin
     */
    public static VelocityMain getInstance() {
        return instance;
    }

    /**
     * Returns the core instance
     * 
     * @return the core instance
     */
    public static VelocityInstance getVelocityInstance() {
        return velocityInstance;
    }

    /**
     * Returns the data directory
     * 
     * @return the data directory
     */
    public Path getDataDirectory() {
        return dataDirectory;
    }

    /**
     * Returns the logger
     * 
     * @return the logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Returns the proxy server
     * 
     * @return the proxy server
     */
    public ProxyServer getProxyServer() {
        return proxyServer;
    }

}
