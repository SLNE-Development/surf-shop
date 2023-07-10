package dev.slne.base.bukkit.instance;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.slne.base.bukkit.BukkitMain;
import dev.slne.base.bukkit.command.BukkitCommandManager;
import dev.slne.base.bukkit.listener.BukkitListenerManager;
import dev.slne.base.core.instance.CoreInstance;

public class BukkitInstance implements CoreInstance {

    private BukkitCommandManager commandManager;
    private BukkitListenerManager listenerManager;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(BukkitMain.getInstance()));
        commandManager = new BukkitCommandManager();

        listenerManager = new BukkitListenerManager();
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        commandManager.registerCommands();

        listenerManager.registerListeners();
    }

    @Override
    public void onDisable() {
        listenerManager.unregisterListeners();
        CommandAPI.onDisable();
    }

    /**
     * Returns the {@link BukkitListenerManager}
     *
     * @return the {@link BukkitListenerManager}
     */
    public BukkitListenerManager getListenerManager() {
        return listenerManager;
    }

}
