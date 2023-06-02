package dev.slne.base.velocity.instance;

import com.velocitypowered.api.plugin.PluginContainer;

import dev.slne.base.core.instance.CoreInstance;
import dev.slne.base.velocity.VelocityMain;
import dev.slne.base.velocity.command.VelocityCommandManager;
import dev.slne.base.velocity.listener.VelocityListenerManager;

public class VelocityInstance extends CoreInstance {

    private VelocityCommandManager commandManager;
    private VelocityListenerManager listenerManager;

    @Override
    public void onLoad() {
        super.onLoad();

        commandManager = new VelocityCommandManager();
        listenerManager = new VelocityListenerManager();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        commandManager.registerCommands();
        listenerManager.registerListeners();

        PluginContainer pluginContainer = VelocityMain.getInstance().getProxyServer().getPluginManager()
                .getPlugin(VelocityMain.getInstance().getPluginId()).orElse(null);

        if (pluginContainer == null) {
            throw new IllegalStateException(
                    "PluginContainer should not be null as this message is send by the same plugin. What is happening?");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        listenerManager.unregisterListeners();
    }

    /**
     * Returns the {@link VelocityCommandManager}
     *
     * @return the {@link VelocityCommandManager}
     */
    public VelocityCommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Returns the {@link VelocityListenerManager}
     *
     * @return the {@link VelocityListenerManager}
     */
    public VelocityListenerManager getListenerManager() {
        return listenerManager;
    }

}
