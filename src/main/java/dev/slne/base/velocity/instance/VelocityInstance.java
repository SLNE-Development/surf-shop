package dev.slne.base.velocity.instance;

import com.github.retrooper.packetevents.PacketEvents;
import com.velocitypowered.api.plugin.PluginContainer;

import dev.slne.base.core.instance.CoreInstance;
import dev.slne.base.velocity.VelocityMain;
import dev.slne.base.velocity.command.VelocityCommandManager;
import dev.slne.base.velocity.listener.VelocityListenerManager;
import dev.slne.data.velocity.VelocityDataSource;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;

public class VelocityInstance extends CoreInstance {

    private VelocityCommandManager commandManager;
    private VelocityListenerManager listenerManager;

    private VelocityDataSource velocityDataSource;

    @Override
    public void onLoad() {
        super.onLoad();

        commandManager = new VelocityCommandManager();
        listenerManager = new VelocityListenerManager();

        velocityDataSource = new VelocityDataSource(VelocityMain.getInstance().getProxyServer(),
                VelocityMain.getInstance().getLogger(), VelocityMain.getInstance().getDataDirectory());
        velocityDataSource.onLoad();
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

        PacketEvents.setAPI(
                VelocityPacketEventsBuilder.build(VelocityMain.getInstance().getProxyServer(), pluginContainer));
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().getSettings().checkForUpdates(true).bStats(false).debug(true).readOnlyListeners(false);
        PacketEvents.getAPI().init();

        velocityDataSource.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        listenerManager.unregisterListeners();

        PacketEvents.getAPI().terminate();
        velocityDataSource.onDisable();
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
