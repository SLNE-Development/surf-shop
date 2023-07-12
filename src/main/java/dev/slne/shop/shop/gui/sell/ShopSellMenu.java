package dev.slne.shop.shop.gui.sell;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;

import dev.slne.shop.BukkitMain;
import dev.slne.shop.listener.events.transaction.sell.ShopItemSellEvent;
import dev.slne.shop.message.MessageManager;
import dev.slne.shop.shop.Shop;
import dev.slne.shop.shop.gui.ShopGui;
import dev.slne.shop.shop.gui.inventory.InventoryTransfer;
import dev.slne.shop.shop.gui.inventory.InventoryTransfer.InventoryResultReason;
import dev.slne.shop.shop.gui.inventory.InventoryTransfer.InventoryTransferResult;
import dev.slne.shop.shop.gui.inventory.bukkit.LimitedShadowInventory;
import dev.slne.shop.shop.gui.inventory.exception.ShadowInventoryTransactionException;
import dev.slne.shop.shop.gui.utils.ConfirmationGui;
import dev.slne.shop.shop.gui.utils.GuiSound;
import dev.slne.shop.shop.gui.utils.GuiUtils;
import dev.slne.shop.shop.gui.utils.ItemUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class ShopSellMenu extends ShopGui {

	private StaticPane shopPane;
	private int selectedAmount;

	/**
	 * Creates a new shop sell menu.
	 *
	 * @param parent        the parent gui
	 * @param shop          the shop
	 * @param viewingPlayer the player viewing the shop
	 */
	public ShopSellMenu(ShopGui parent, Shop shop, Player viewingPlayer) {
		super(parent, 6, "Shop - Kaufen", shop, viewingPlayer);

		this.selectedAmount = 0;

		shopPane = new StaticPane(0, 0, 9, 6);

		setIncreaseDecreaseItems(viewingPlayer);

		// X == 4
		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.owner")) {
			shopPane.addItem(new GuiItem(ItemUtils.head(shop.getOwnerUuid())), 4, 0);
		}

		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.info")) {
			shopPane.addItem(new GuiItem(ItemUtils.infoItem(shop)), 4, 2);
		}

		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.shop-item")) {
			shopPane.addItem(new GuiItem(ItemUtils.shopItem(shop)), 4, 3);
		}

		addPane(shopPane);
	}

	/**
	 * Sets the increase/decrease items
	 *
	 * @param viewingPlayer the player viewing the shop
	 */
	@SuppressWarnings("java:S3776")
	private void setIncreaseDecreaseItems(Player viewingPlayer) {
		int maxAmount = getShop().getAmount();

		// X == 0
		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.decrease-1000")) {
			shopPane.addItem(
					new GuiItem(ItemUtils.decreaseThousandItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(-1000)),
					0, 2);
			shopPane.addItem(
					new GuiItem(ItemUtils.decreaseThousandItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(-1000)),
					0, 3);
		}

		// X == 1
		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.decrease-100")) {
			shopPane.addItem(
					new GuiItem(ItemUtils.decreaseHundredItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(-100)),
					1, 2);
			shopPane.addItem(
					new GuiItem(ItemUtils.decreaseHundredItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(-100)),
					1, 3);
		}

		// X == 2
		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.decrease-10")) {
			shopPane.addItem(
					new GuiItem(ItemUtils.decreaseTenItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(-10)),
					2, 2);
			shopPane.addItem(
					new GuiItem(ItemUtils.decreaseTenItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(-10)),
					2, 3);
		}

		// X == 3
		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.decrease-1")) {
			shopPane.addItem(
					new GuiItem(ItemUtils.decreaseOneItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(-1)),
					3, 2);
			shopPane.addItem(
					new GuiItem(ItemUtils.decreaseOneItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(-1)),
					3, 3);
		}

		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.reset") && selectedAmount > 0) {
			shopPane.addItem(
					new GuiItem(ItemUtils.resetCountItem(selectedAmount, maxAmount), event -> resetItems()), 3, 4);
		} else if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.reset") && selectedAmount == 0) {
			shopPane.removeItem(3, 4);
		}

		// X == 5
		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.increase-1")) {
			shopPane.addItem(
					new GuiItem(ItemUtils.increaseOneItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(1)),
					5, 2);
			shopPane.addItem(
					new GuiItem(ItemUtils.increaseOneItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(1)),
					5, 3);
		}

		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.buy") && selectedAmount > 0) {
			shopPane.addItem(
					new GuiItem(ItemUtils.buyItem(selectedAmount, getShop().getAmount()), event -> buyItems()), 5, 4);
		} else if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.buy") && selectedAmount == 0) {
			shopPane.removeItem(5, 4);
		}

		// X == 6
		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.increase-10")) {
			shopPane.addItem(
					new GuiItem(ItemUtils.increaseTenItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(10)),
					6, 2);
			shopPane.addItem(
					new GuiItem(ItemUtils.increaseTenItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(10)),
					6, 3);
		}

		// X == 7
		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.increase-100")) {
			shopPane.addItem(
					new GuiItem(ItemUtils.increaseHundredItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(100)),
					7, 2);
			shopPane.addItem(
					new GuiItem(ItemUtils.increaseHundredItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(100)),
					7, 3);
		}

		// X == 8
		if (viewingPlayer.hasPermission("surf.shop.item.sell-menu.increase-1000")) {
			shopPane.addItem(
					new GuiItem(ItemUtils.increaseThousandItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(1000)),
					8, 2);
			shopPane.addItem(
					new GuiItem(ItemUtils.increaseThousandItem(selectedAmount, maxAmount),
							event -> increaseDecreaseAmount(1000)),
					8, 3);
		}
	}

	/**
	 * Increases or decreases the amount of items to be bought.
	 *
	 * @param amount the amount to increase or decrease by
	 */
	private void increaseDecreaseAmount(int amount) {
		this.selectedAmount += amount;
		int maxAmount = getShop().getAmount();

		if (this.selectedAmount <= 0) {
			this.selectedAmount = 0;
		} else if (this.selectedAmount > maxAmount) {
			this.selectedAmount = maxAmount;
		}

		setIncreaseDecreaseItems(getViewingPlayer());
		update();
	}

	/**
	 * Buys the selected amount of items.
	 */
	private void buyItems() {
		ItemStack itemStack = getShop().getItemStack();
		if (itemStack == null) {
			return;
		}

		Component displayName = itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()
				? itemStack.getItemMeta().displayName()
				: Component.text(itemStack.getType().name());
		displayName = displayName.colorIfAbsent(MessageManager.VARIABLE_VALUE);

		List<Component> lore = new ArrayList<>();

		lore.add(Component.text("Bist du dir sicher, dass du", NamedTextColor.GRAY));
		lore.add(Component.text(selectedAmount + "x", MessageManager.VARIABLE_VALUE).append(Component.text(" "))
				.append(displayName));
		lore.add(Component.text("kaufen möchtest?", NamedTextColor.GRAY));

		ConfirmationGui confirmationGui = new ConfirmationGui(this, null, null,
				displayName.append(Component.text(" kaufen", NamedTextColor.GOLD)), lore);
		confirmationGui.setOnConfirm(event -> handleBuyConfirm(confirmationGui));
		confirmationGui.show(getViewingPlayer());
	}

	/**
	 * Resets the items.
	 */
	private void resetItems() {
		this.selectedAmount = 0;
		setIncreaseDecreaseItems(getViewingPlayer());
		update();
	}

	/**
	 * Tries to transfer the items to the player.
	 *
	 * @param gui    the gui
	 * @param player the player
	 * @param amount the amount of items to transfer
	 * @return whether the transfer was successful
	 */
	private boolean transferItems(ConfirmationGui gui, Player player, boolean performTransaction, int amount) {
		PlayerInventory inventory = player.getInventory();
		ItemStack itemStack = getShop().getItemStack().clone();

		int maxStackSize = itemStack.getMaxStackSize();
		Inventory transferInventory = Bukkit.createInventory(null, 9 * 6);

		while (amount > 0) {
			int transferAmount = Math.min(amount, maxStackSize);
			itemStack.setAmount(transferAmount);
			transferInventory.addItem(itemStack);
			amount -= transferAmount;
		}

		InventoryTransfer transfer = new InventoryTransfer(new LimitedShadowInventory(transferInventory),
				new LimitedShadowInventory(inventory));

		try {
			InventoryTransferResult result = transfer.transfer(itemStack, selectedAmount);

			if (result.getReason().equals(InventoryResultReason.TO_IS_FULL)) {
				player.sendMessage(MessageManager.getInventoryCannotAcceptNItemsComponent(selectedAmount));
				GuiUtils.playGuiSound(GuiSound.DENY_ACTION, player);
				gui.backToParent(player);
			}

			if (performTransaction) {
				transfer.performTransaction();
			}

			return result.getReason().equals(InventoryResultReason.N_REACHED);
		} catch (ShadowInventoryTransactionException exception) {
			exception.printStackTrace();
			return false;
		}
	}

	/**
	 * Handles the buy confirm.
	 *
	 * @param gui the gui
	 */
	private void handleBuyConfirm(ConfirmationGui gui) {
		ShopItemSellEvent event = new ShopItemSellEvent(getShop(), getViewingPlayer(), getShop().getItemStack(),
				selectedAmount);
		Bukkit.getPluginManager().callEvent(event);

		if (event.isCancelled()) {
			event.applyCancelled(getViewingPlayer());
			return;
		}

		int finalAmount = event.getOutputAmount();

		if (!transferItems(gui, getViewingPlayer(), false, finalAmount)) {
			return;
		}

		// Decrease actual shop items and add transactions
		transferItems(gui, getViewingPlayer(), true, finalAmount);
		getShop().decreaseAmount(finalAmount);

		new BukkitRunnable() {
			@Override
			public void run() {
				ItemStack boughtItemStack = getShop().getItemStack();

				if (boughtItemStack == null) {
					return;
				}

				if (getViewingPlayer() != null) {
					getViewingPlayer().closeInventory();
					getViewingPlayer().sendMessage(
							MessageManager.getShopBoughtAmountBuyerComponent(boughtItemStack, selectedAmount));
				}

				if (getShop().getOwner() != null) {
					getShop().getOwner().sendMessage(
							MessageManager.getShopBoughtAmountOwnerComponent(getViewingPlayer(), boughtItemStack,
									selectedAmount));
				}
			}
		}.runTask(BukkitMain.getInstance());
	}

}
