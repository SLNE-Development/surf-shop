package dev.slne.surf.shop.server;

import com.github.retrooper.packetevents.PacketEvents;
import dev.slne.surf.shop.server.instance.BukkitApi;
import dev.slne.surf.shop.server.instance.BukkitInstance;
import dev.slne.transaction.api.TransactionApi;
import dev.slne.transaction.api.currency.Currency;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.*;

public class BukkitMain extends JavaPlugin {

    private static BukkitMain instance;
    private static BukkitInstance bukkitInstance;

    private Random random;
    private Currency defaultCurrency;
    private final List<Currency> otherCurrencies = new ArrayList<>();

    @Override
    @SuppressWarnings({"java:S3252", "java:S2696"})
    public void onLoad() {
        instance = this;
        bukkitInstance = new BukkitInstance(getClassLoader());
        BukkitApi.setInstance(bukkitInstance);

        random = new Random();

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(true).bStats(true);
        PacketEvents.getAPI().load();

        saveDefaultConfig();

        setCurrencies();
        bukkitInstance.onLoad();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();

        bukkitInstance.onEnable();
    }

    @Override
    public void onDisable() {
        bukkitInstance.onDisable();

        PacketEvents.getAPI().terminate();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        otherCurrencies.clear();

        setCurrencies();
    }

    /**
     * Returns the instance of the plugin
     *
     * @return The instance of the plugin
     */
    public static BukkitMain getInstance() {
        return instance;
    }

    /**
     * Returns the core instance of the plugin
     *
     * @return The core instance of the plugin
     */
    public static BukkitInstance getBukkitInstance() {
        return bukkitInstance;
    }

    /**
     * @return the random
     */
    public Random getRandom() {
        return random;
    }

    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    public List<Currency> getOtherCurrencies() {
        return otherCurrencies;
    }

    private void setCurrencies() {
        final Currency defaultCurrency = TransactionApi.getCurrency(getConfig().getString("default-shop-currency", ""));
        checkNotNull(defaultCurrency, "Default currency cannot be null");

        final List<String> otherCurrenciesNameList = getConfig().getStringList("other-shop-currencies");
        TransactionApi.getCurrencyManager().getCurrencies().stream()
                .filter(currency -> otherCurrenciesNameList.contains(currency.getName()))
                .forEach(otherCurrencies::add);

        this.defaultCurrency = defaultCurrency;
    }
}
