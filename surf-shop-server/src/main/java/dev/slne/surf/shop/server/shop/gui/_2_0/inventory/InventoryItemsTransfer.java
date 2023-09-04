package dev.slne.surf.shop.server.shop.gui._2_0.inventory;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.*;

public class InventoryItemsTransfer {

    private final @NotNull ItemStack stacksToTransfer;
    private final @Range(from = 1, to = Integer.MAX_VALUE) int totalItemsToTransfer;
    private final @NotNull Inventory to;
    private final @NotNull Inventory toClone;

    @SuppressWarnings("ConstantValue") // We need to check if the totalItemsToTransfer is in its bounds
    public InventoryItemsTransfer(
            final @NotNull ItemStack stackToTransfer,
            final @Range(from = 1, to = Integer.MAX_VALUE) int totalItemsToTransfer,
            final @NotNull Inventory to
    ) {
        checkNotNull(stackToTransfer, "stackToTransfer");
        checkNotNull(to, "to");
        checkArgument(totalItemsToTransfer > 0, "totalItemsToTransfer must be greater than 0");

        this.stacksToTransfer = stackToTransfer;
        this.totalItemsToTransfer = totalItemsToTransfer;
        this.to = to;
        this.toClone = Bukkit.createInventory(null, InventoryType.PLAYER);

        this.toClone.setContents(to.getContents());
    }

    public List<ItemStack> commit(int amount) {
        final ItemStack[] toTransfer = IntStream.range(0, amount)
                .mapToObj(__ -> this.stacksToTransfer.clone())
                .toArray(ItemStack[]::new);

        return this.to.addItem(toTransfer).values().stream()
                .filter(itemStack -> !itemStack.getType().isAir())
                .filter(itemStack -> itemStack.getAmount() > 0)
                .toList();
    }

    public List<ItemStack> addItems() {
        final ItemStack[] toTransfer = IntStream.range(0, this.totalItemsToTransfer)
                .mapToObj(__ -> this.stacksToTransfer.clone())
                .toArray(ItemStack[]::new);

        return this.toClone.addItem(toTransfer).values().stream()
                .filter(itemStack -> !itemStack.getType().isAir())
                .filter(itemStack -> itemStack.getAmount() > 0)
                .toList();
    }
}
