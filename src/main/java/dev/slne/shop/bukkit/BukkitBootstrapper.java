package dev.slne.shop.bukkit;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;

public class BukkitBootstrapper implements PluginBootstrap {

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return new BukkitMain();
    }

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // Currently not used
    }

}
