package dev.slne.shop.shop.gui.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import dev.slne.shop.message.MessageManager;
import dev.slne.shop.shop.gui.ShopGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class ItemUtils {

    /**
     * Prevents instantiation.
     */
    private ItemUtils() {
    }

    /**
     * Creates an {@link ItemStack} with the given parameters.
     *
     * @param material    The material of the item.
     * @param amount      The amount of the item.
     * @param durability  The durability of the item.
     * @param displayName The display name of the item.
     * @param lore        The lore of the item.
     * @return The created item.
     */
    public static ItemStack item(Material material, int amount, int durability, Component displayName,
            Component... lore) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta instanceof Damageable damageable) {
            damageable.setDamage(durability);
        }

        if (displayName == null) {
            displayName = Component.empty();
        }

        meta.displayName(displayName.decoration(TextDecoration.ITALIC, false));

        if (lore != null) {
            List<Component> loreList = Arrays.asList(lore);
            loreList.replaceAll(line -> line.decoration(TextDecoration.ITALIC, false));

            meta.lore(loreList);
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Creates a pane item.
     *
     * @return The created pane item.
     */
    public static ItemStack paneItem() {
        return item(Material.GRAY_STAINED_GLASS_PANE, 1, 0, Component.space());
    }

    /**
     * Creates a close item.
     *
     * @return The created close item.
     */
    public static ItemStack closeItem() {
        return item(Material.BARRIER, 1, 0, Component.text("Schließen", NamedTextColor.RED),
                Component.empty(), Component.text("Schließt das Menü", NamedTextColor.GRAY), Component.empty());
    }

    /**
     * Creates a back item.
     *
     * @return The created back item.
     */
    public static ItemStack backItem(ShopGui shopGui) {
        List<ShopGui> parents = shopGui.walkParents();
        List<Component> parentNames = new ArrayList<>();

        String formatterParent = "<< %s";
        String formatterCurrent = ">> %s";

        parentNames.add(
                Component.text(String.format(formatterCurrent, shopGui.getTitle()), MessageManager.VARIABLE_VALUE));
        for (ShopGui parent : parents) {
            parentNames.add(Component.text(String.format(formatterParent, parent.getTitle()), NamedTextColor.GRAY));
        }

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());

        if (parentNames.size() > 1) {
            lore.add(Component.text("Geht zurück zum vorherigen Menü.", NamedTextColor.GRAY));
        } else {
            lore.add(Component.text("Schließt das Menü", NamedTextColor.GRAY));
        }

        lore.add(Component.empty());
        lore.addAll(parentNames);
        lore.add(Component.empty());

        return item(Material.ARROW, 1, 0, Component.text("Zurück", NamedTextColor.RED),
                lore.stream().toArray(size -> new Component[size]));
    }

}
