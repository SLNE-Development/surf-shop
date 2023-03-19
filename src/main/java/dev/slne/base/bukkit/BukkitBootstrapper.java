package dev.slne.base.bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;

public class BukkitBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(PluginProviderContext context) {

    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return new BukkitMain();
    }

}
