package dev.slne.surf.shop.server.shop.gui._2_0.edit.storage.actions;

import dev.slne.gui.api.anvil.SurfAnvilGui;
import dev.slne.gui.api.anvil.requirement.AnvilRequirement;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.BukkitMain;
import dev.slne.surf.shop.server.message.MessageManager;
import dev.slne.surf.shop.server.shop.gui._2_0.SurfShopGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.SurfShopAnvilGui;
import dev.slne.surf.shop.server.shop.gui._2_0.edit.anvil.requirement.AnvilOnlyNumberRequirement;
import dev.slne.surf.shop.server.shop.gui._2_0.inventory.InventoryItemsTransfer;
import net.kyori.adventure.text.Component;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShopEditStorageRemoveAnvilMenu extends SurfShopAnvilGui {

    public ShopEditStorageRemoveAnvilMenu(@Nullable SurfShopGui parent, @NotNull Shop shop) {
        super(shop, parent, Component.text("Items entnehmen", MessageManager.PRIMARY));
    }

    /**
     * Returns the copied gui
     *
     * @return the copied gui
     */
    @Override
    public SurfAnvilGui copyGui() {
        return new ShopEditStorageRemoveAnvilMenu(getParent(), getShop());
    }

    /**
     * Returns the requirements
     *
     * @param requirements the requirements
     *
     * @return the requirements
     */
    @Override
    public List<AnvilRequirement> getRequirements(List<AnvilRequirement> requirements) {
        requirements.add(new AnvilOnlyNumberRequirement<>(0, getShop().amount()));
        return requirements;
    }

    /**
     * Called when the player submits the anvil gui
     *
     * @param player the player
     * @param input  the input
     */
    @Override
    public List<AnvilGUI.ResponseAction> onSubmit(Player player, String input) {
        final int totalItems = Integer.parseInt(input);
        final ItemStack shopItem = getShop().item().clone();

        shopItem.setAmount(1);

        final InventoryItemsTransfer transfer = new InventoryItemsTransfer(shopItem, totalItems, player.getInventory());
        final List<ItemStack> leftOvers = transfer.addItems();

        int leftOverAmount = leftOvers.stream().mapToInt(ItemStack::getAmount).sum();
        int itemsToTransfer = totalItems - leftOverAmount;

        getShop().decreaseAmount(player.getUniqueId(), itemsToTransfer).thenAccept(__ -> {
            new SyncTransferItems(player, itemsToTransfer, transfer).execute();

            player.sendMessage(MessageManager.removeItems(getShop(), itemsToTransfer));
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        });

        return new ArrayList<>();
    }

    /**
     * Called when the player cancels the anvil gui
     *
     * @param player the player
     * @param input  the input
     */
    @Override
    public List<AnvilGUI.ResponseAction> onCancel(Player player, String input) {
        backToParent(player);
        return new ArrayList<>();
    }

    /**
     * Transfers the items from the {@link InventoryItemsTransfer}
     * to the player inventory and drops the rest. This needs to be done Sync
     */
    @SuppressWarnings("InnerClassMayBeStatic") // Just why?
    private class SyncTransferItems extends BukkitRunnable {

        private final Player player;
        private final int itemsToTransfer;
        private final InventoryItemsTransfer transfer;

        /**
         * Creates a new {@link SyncTransferItems} instance
         *
         * @param player          the player
         * @param itemsToTransfer the items to transfer
         * @param transfer        the transfer
         */
        SyncTransferItems(Player player, int itemsToTransfer, InventoryItemsTransfer transfer) {
            this.player = player;
            this.itemsToTransfer = itemsToTransfer;
            this.transfer = transfer;
        }

        /**
         * Runs the task
         */
        @Override
        public void run() {
            final World world = player.getWorld();
            final Location location = player.getLocation();

            transfer.commit(itemsToTransfer)
                    .forEach(itemStack -> world.dropItem(location, itemStack, item -> {
                        item.setOwner(player.getUniqueId());
                        item.setThrower(player.getUniqueId());
                    }));
        }

        /**
         * Executes the task synchronously
         */
        public void execute() {
            this.runTask(BukkitMain.getInstance());
        }
    }
}
