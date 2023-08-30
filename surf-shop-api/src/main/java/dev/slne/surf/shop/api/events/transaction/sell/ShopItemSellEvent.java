package dev.slne.surf.shop.api.events.transaction.sell;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class ShopItemSellEvent extends ShopEvent {

    private final ItemStack boughtItemStack;
    private final int boughtAmount;
    private int outputAmount;

    /**
     * Constructs a new shop sell buy event.
     *
     * @param shop            The shop.
     * @param player          The player.
     * @param boughtItemStack The item stack bought.
     */
    public ShopItemSellEvent(Shop shop, Player player, ItemStack boughtItemStack, int boughtAmount) {
        super(shop, player);

        this.boughtItemStack = boughtItemStack;
        this.boughtAmount = boughtAmount;
        this.outputAmount = boughtAmount;
    }

    /**
     * @return the boughtAmount
     */
    public int getBoughtAmount() {
        return boughtAmount;
    }

    /**
     * @return the boughtItemStack
     */
    public ItemStack getBoughtItemStack() {
        return boughtItemStack;
    }

    /**
     * @return the outputAmount
     */
    public int getOutputAmount() {
        return outputAmount;
    }

    /**
     * @param outputAmount the outputAmount to set
     */
    public void setOutputAmount(int outputAmount) {
        this.outputAmount = outputAmount;
    }

}
