package dev.slne.surf.shop.server.shop.gui._2_0.inventory;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.*;

public class PlayerInventoryItemsTransfer {

    private final @NotNull ItemStack stacksToTransfer;
    private final @Range(from = 1, to = Integer.MAX_VALUE) int stacksToTransferAmount;
    private final @NotNull PlayerInventory to;
    private final @NotNull Inventory toClone;

    @SuppressWarnings("ConstantValue") // We need to check if the stacksToTransfer is in its bounds
    public PlayerInventoryItemsTransfer(
            final @NotNull ItemStack stacksToTransfer,
            final @Range(from = 1, to = Integer.MAX_VALUE) int stacksToTransferAmount,
            final @NotNull PlayerInventory to
    ) {
        checkNotNull(stacksToTransfer, "stacksToTransfer");
        checkNotNull(to, "to");
        checkArgument(stacksToTransferAmount > 0, "stacksToTransferAmount must be greater than 0");

        this.stacksToTransfer = stacksToTransfer;
        this.stacksToTransferAmount = stacksToTransferAmount;
        this.to = to;

        switch (to.getType()) {
            case DISPENSER -> this.toClone = Bukkit.createInventory(null, InventoryType.DISPENSER);
            case DROPPER -> this.toClone = Bukkit.createInventory(null, InventoryType.DROPPER);
            case FURNACE -> this.toClone = Bukkit.createInventory(null, InventoryType.FURNACE);
            case WORKBENCH -> this.toClone = Bukkit.createInventory(null, InventoryType.WORKBENCH);
            case CRAFTING -> this.toClone = Bukkit.createInventory(null, InventoryType.CRAFTING);
            case ENCHANTING -> this.toClone = Bukkit.createInventory(null, InventoryType.ENCHANTING);
            case BREWING -> this.toClone = Bukkit.createInventory(null, InventoryType.BREWING);
            case PLAYER -> this.toClone = Bukkit.createInventory(null, InventoryType.PLAYER);
            case CREATIVE -> this.toClone = Bukkit.createInventory(null, InventoryType.CREATIVE);
            case MERCHANT -> this.toClone = Bukkit.createInventory(null, InventoryType.MERCHANT);
            case ENDER_CHEST -> this.toClone = Bukkit.createInventory(null, InventoryType.ENDER_CHEST);
            case ANVIL -> this.toClone = Bukkit.createInventory(null, InventoryType.ANVIL);
            case SMITHING -> this.toClone = Bukkit.createInventory(null, InventoryType.SMITHING);
            case BEACON -> this.toClone = Bukkit.createInventory(null, InventoryType.BEACON);
            case HOPPER -> this.toClone = Bukkit.createInventory(null, InventoryType.HOPPER);
            case SHULKER_BOX -> this.toClone = Bukkit.createInventory(null, InventoryType.SHULKER_BOX);
            case BARREL -> this.toClone = Bukkit.createInventory(null, InventoryType.BARREL);
            case BLAST_FURNACE -> this.toClone = Bukkit.createInventory(null, InventoryType.BLAST_FURNACE);
            case LECTERN -> this.toClone = Bukkit.createInventory(null, InventoryType.LECTERN);
            case SMOKER -> this.toClone = Bukkit.createInventory(null, InventoryType.SMOKER);
            case LOOM -> this.toClone = Bukkit.createInventory(null, InventoryType.LOOM);
            case CARTOGRAPHY -> this.toClone = Bukkit.createInventory(null, InventoryType.CARTOGRAPHY);
            case GRINDSTONE -> this.toClone = Bukkit.createInventory(null, InventoryType.GRINDSTONE);
            case STONECUTTER -> this.toClone = Bukkit.createInventory(null, InventoryType.STONECUTTER);
            case COMPOSTER -> this.toClone = Bukkit.createInventory(null, InventoryType.COMPOSTER);
            case CHISELED_BOOKSHELF -> this.toClone = Bukkit.createInventory(null, InventoryType.CHISELED_BOOKSHELF);
            case JUKEBOX -> this.toClone = Bukkit.createInventory(null, InventoryType.JUKEBOX);
            default -> this.toClone = Bukkit.createInventory(null, to.getSize());
        }

        this.toClone.setContents(to.getContents());
    }

    public boolean transferItems() throws CouldNotAddAllItemsToInventoryException {
        final ItemStack[] toTransfer = IntStream.range(0, this.stacksToTransferAmount)
                .mapToObj(__ -> this.stacksToTransfer.clone())
                .toArray(ItemStack[]::new);

        if (!this.toClone.addItem(toTransfer).isEmpty()) {
            return false;
        }

        final HashMap<Integer, ItemStack> leftOvers = this.to.addItem(toTransfer);
        if (!leftOvers.isEmpty()) {
            throw new CouldNotAddAllItemsToInventoryException(leftOvers);
        }

        return true;
    }
}
