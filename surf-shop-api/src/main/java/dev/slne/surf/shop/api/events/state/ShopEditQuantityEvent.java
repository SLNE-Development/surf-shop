package dev.slne.surf.shop.api.events.state;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.jetbrains.annotations.Nullable;

public class ShopEditQuantityEvent extends ShopEvent {

    private int newQuantity;

    /**
     * Constructs a new shop event.
     *
     * @param shop        The shop.
     * @param newQuantity the new quantity
     */
    public ShopEditQuantityEvent(@Nullable Shop shop, int newQuantity, boolean async) {
        super(shop, null, async);
        this.newQuantity = newQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public void setNewQuantity(int newQuantity) {
        this.newQuantity = newQuantity;
    }
}
