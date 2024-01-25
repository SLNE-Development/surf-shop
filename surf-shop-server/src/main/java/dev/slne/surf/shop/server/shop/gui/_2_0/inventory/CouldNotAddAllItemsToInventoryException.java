package dev.slne.surf.shop.server.shop.gui._2_0.inventory;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

import java.io.Serial;
import java.util.Map;

@Getter
public class CouldNotAddAllItemsToInventoryException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5514500532349576130L;
    private final Map<Integer, ItemStack> leftOver;

    public CouldNotAddAllItemsToInventoryException(Map<Integer, ItemStack> leftOver) {
        super("Could not add all items to inventory");

        this.leftOver = leftOver;
    }

}
