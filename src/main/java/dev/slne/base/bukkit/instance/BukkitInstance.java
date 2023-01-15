package dev.slne.base.bukkit.instance;

import dev.slne.base.bukkit.BukkitMain;
import dev.slne.base.bukkit.command.BukkitCommandManager;
import dev.slne.base.bukkit.listener.BukkitListenerManager;
import dev.slne.base.core.instance.CoreInstance;
import dev.slne.data.bukkit.BukkitDataSource;

public class BukkitInstance extends CoreInstance {

    private BukkitCommandManager commandManager;
    private BukkitListenerManager listenerManager;

    private BukkitDataSource dataSource;

    @Override
    public void onLoad() {
        super.onLoad();

        commandManager = new BukkitCommandManager();
        listenerManager = new BukkitListenerManager();

        dataSource = new BukkitDataSource(BukkitMain.getInstance());
        dataSource.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        commandManager.registerCommands();
        listenerManager.registerListeners();

        dataSource.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        listenerManager.unregisterListeners();
        dataSource.onDisable();
    }

    /**
     * Returns the {@link BukkitCommandManager}
     * 
     * @return the {@link BukkitCommandManager}
     */
    public BukkitCommandManager getCommandManager() {
        return commandManager;
    }

    /**
     * Returns the {@link BukkitListenerManager}
     * 
     * @return the {@link BukkitListenerManager}
     */
    public BukkitListenerManager getListenerManager() {
        return listenerManager;
    }

    /**
     * Returns the {@link BukkitDataSource}
     * 
     * @return the {@link BukkitDataSource}
     */
    public BukkitDataSource getDataSource() {
        return dataSource;
    }

}
