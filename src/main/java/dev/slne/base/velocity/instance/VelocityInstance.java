package dev.slne.base.velocity.instance;

import dev.slne.base.core.instance.CoreInstance;
import dev.slne.base.velocity.command.VelocityCommandManager;
import dev.slne.base.velocity.listener.VelocityListenerManager;

public class VelocityInstance implements CoreInstance {

    private VelocityCommandManager commandManager;
    private VelocityListenerManager listenerManager;

    @Override
    public void onLoad() {
        commandManager = new VelocityCommandManager();
        listenerManager = new VelocityListenerManager();
    }

    @Override
    public void onEnable() {
        commandManager.registerCommands();
        listenerManager.registerListeners();
    }

    @Override
    public void onDisable() {
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
