package dev.slne.surf.shop.api.events.state;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import static com.google.common.base.Preconditions.*;

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

    @Range(from = 1, to = 64)
    public int getNewQuantity() {
        return newQuantity;
    }

    @SuppressWarnings("ConstantValue") // IntelliJ thinks this is a constant value, but it's not
    public void setNewQuantity(@Range(from = 1, to = 64) int newQuantity) {
        checkArgument(newQuantity >= 1 && newQuantity <= 64, "newQuantity must be between 1 and 64");

        this.newQuantity = newQuantity;
    }
}
