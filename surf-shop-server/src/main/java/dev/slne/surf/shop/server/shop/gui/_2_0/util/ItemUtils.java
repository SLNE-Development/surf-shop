package dev.slne.surf.shop.server.shop.gui._2_0.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.server.message.MessageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class ItemUtils {

    private static final LoadingCache<URL, ItemStack> customHeadCache = Caffeine.newBuilder()
            .build(key -> {
                final PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                final PlayerTextures textures = profile.getTextures();

                textures.clear();
                textures.setSkin(key);

                final ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
                head.editMeta(SkullMeta.class, meta -> {
                   meta.setPlayerProfile(profile);
                });

                return head;
            });

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
     * Creates a skull item.
     *
     * @param ownerUuid The owner uuid of the skull.
     * @param lore      The lore of the skull.
     * @return The created skull item.
     */
    public static ItemStack head(UUID ownerUuid, Component... lore) {
        OfflinePlayer owner = Bukkit.getOfflinePlayer(ownerUuid);

        return head(owner, Component.text(owner.getName(), NamedTextColor.GOLD), lore);
    }

    public static ItemStack head(String url, int amount, Component displayName, Component... lore) {

        try {
            final ItemStack head = customHeadCache.get(URI.create(url).toURL()).clone();

            head.setAmount(amount);

            if (displayName == null) {
                displayName = Component.empty();
            }

            final Component finalDisplayName = displayName;
            head.editMeta(meta -> {
                meta.displayName(finalDisplayName.decoration(TextDecoration.ITALIC, false));

                if (lore != null) {
                    List<Component> loreList = Arrays.asList(lore);
                    loreList.replaceAll(line -> line.decoration(TextDecoration.ITALIC, false));

                    meta.lore(loreList);
                }
            });

            return head;
        } catch (MalformedURLException e) {
            ComponentLogger.logger(ItemUtils.class).error("Failed to create custom head", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a skull item.
     *
     * @param owner       The owner of the skull.
     * @param displayName The display name of the skull.
     * @param lore        The lore of the skull.
     * @return The created skull item.
     */
    public static ItemStack head(OfflinePlayer owner, Component displayName, Component... lore) {
        final ItemStack head = item(Material.PLAYER_HEAD, 1, 0, displayName.colorIfAbsent(NamedTextColor.GOLD), lore);

        head.editMeta(SkullMeta.class, meta -> meta.setOwningPlayer(owner));

        return head;
    }

    /**
     * Returns the shop edit item
     *
     * @return the shop edit item
     */
    public static ItemStack editShopItem() {
        return item(Material.WRITABLE_BOOK, 1, 0, Component.text("Shop bearbeiten", NamedTextColor.GOLD),
                Component.empty(), Component.text("Öffnet das Shop Bearbeitungs Menü", NamedTextColor.GRAY),
                Component.empty());
    }

    /**
     * Returns the shop sell item
     *
     * @return the shop sell item
     */
    public static ItemStack buyItem() {
        return item(Material.RED_CONCRETE, 1, 0, Component.text("Ankauf", NamedTextColor.GOLD),
                Component.empty(), Component.text("WiP", NamedTextColor.GRAY), Component.empty());
    }

    /**
     * Returns the shop sell item
     *
     * @return the shop sell item
     */
    public static ItemStack sellItem(Shop shop) {
        ItemStack toReturn;

        if (shop.item() == null) {
            toReturn = disabledItem();
        } else if (shop.item() != null && shop.isInventoryEmpty()) {
            toReturn = item(Material.BARRIER, 1, 0, Component.text("Leer", NamedTextColor.GOLD),
                    Component.empty(), Component.text("Der Shop ist leer", NamedTextColor.GRAY),
                    Component.empty());
        } else {
            toReturn = item(Material.GREEN_CONCRETE, 1, 0, Component.text("Verkauf", NamedTextColor.GOLD),
                    Component.empty(), Component.text("Hier können Items gekauft werden", NamedTextColor.GRAY),
                    Component.empty());
        }

        return toReturn;
    }

    /**
     * Returns the shop info item
     *
     * @param shop the shop
     * @return the shop info item
     */
    public static ItemStack infoItem(Shop shop) {
        List<Component> lore = new ArrayList<>();

        String space = " ".repeat(3);

        lore.add(Component.empty());

        lore.add(Component.text("Anzahl: ", MessageManager.VARIABLE_KEY));
        lore.add(Component.text(space + shop.amount(), MessageManager.VARIABLE_VALUE));
        lore.add(Component.empty());

        lore.add(Component.text("ServerShop UUID: ", MessageManager.VARIABLE_KEY));
        lore.add(Component.text(space + shop.getUUID().toString(), MessageManager.VARIABLE_VALUE));

        lore.add(Component.empty());

        return item(Material.OAK_SIGN, 1, 0, Component.text("Info", NamedTextColor.GOLD),
                lore.toArray(Component[]::new));
    }

    /**
     * Returns the shop item
     *
     * @param shop the shop
     * @return the shop item
     */
    public static ItemStack shopItem(Shop shop) {
        ItemStack toShow = shop.item();

        if (toShow == null) {
            toShow = ItemUtils.item(Material.BARRIER, 1, 0, Component.text("Nicht eingerichtet", NamedTextColor.GOLD),
                    Component.empty(), Component.text("Dieser Shop ist nicht eingerichtet", NamedTextColor.GRAY),
                    Component.empty());
        } else {
            toShow = toShow.clone();
        }

        return toShow;
    }

    /**
     * Returns the owner item
     *
     * @param shop the shop
     * @return the owner item
     */
    public static @NotNull ItemStack ownerItem(@NotNull Shop shop) {
        final OfflinePlayer owner = Bukkit.getOfflinePlayer(shop.getOwnerUUID());
        final String name = owner.getName();

        assert name != null : "Owner name cannot be null, as the owner is required to create a shop";

        return head(owner, Component.text("%s´s Shop".formatted(name), MessageManager.VARIABLE_VALUE));
    }

    /**
     * Returns the disabled item
     *
     * @return the disabled item
     */
    public static ItemStack disabledItem() {
        return item(Material.BARRIER, 1, 0, Component.text("Nicht verfügbar", NamedTextColor.GOLD),
                Component.empty(), Component.text("Aktuell nicht verfügbar", NamedTextColor.GRAY),
                Component.empty());
    }

    /**
     * Returns the increase/decrease item
     *
     * @param material       the material
     * @param amount         the increase/decrease amount
     * @param increase       if the item should increase or decrease
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the increase/decrase item
     */
    private static ItemStack increaseDecreaseItem(Material material, int amount, boolean increase, int selectedAmount,
                                                  int maxAmount) {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(
                Component.text((increase ? "Erhöht" : "Verringert") + " die Anzahl um " + amount, NamedTextColor.GRAY));
        lore.add(Component.empty());

        TextComponent.Builder builder = Component.text();
        builder.append(Component.text(selectedAmount, MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" / ", NamedTextColor.GRAY));
        builder.append(Component.text(maxAmount, MessageManager.VARIABLE_VALUE));
        lore.add(Component.text("Ausgewählt: ", NamedTextColor.GRAY)
                .append(builder.build())
                .append(Component.text(" Items", MessageManager.VARIABLE_VALUE)));

        lore.add(Component.empty());

        return item(material, 1, 0, Component.text((increase ? "+" : "-") + amount, NamedTextColor.GOLD), lore.toArray(Component[]::new));
    }

    /**
     * Returns the increase one item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the increase one item
     */
    public static ItemStack increaseOneItem(int selectedAmount, int maxAmount) {
        return increaseDecreaseItem(Material.LIME_STAINED_GLASS_PANE, 1, true, selectedAmount, maxAmount);
    }

    /**
     * Returns the increase ten item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the increase ten item
     */
    public static ItemStack increaseTenItem(int selectedAmount, int maxAmount) {
        return increaseDecreaseItem(Material.LIME_STAINED_GLASS, 10, true, selectedAmount, maxAmount);
    }

    /**
     * Returns the increase hundred item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the increase hundred item
     */
    public static ItemStack increaseHundredItem(int selectedAmount, int maxAmount) {
        return increaseDecreaseItem(Material.LIME_CONCRETE, 100, true, selectedAmount, maxAmount);
    }

    /**
     * Returns the increase thousand item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the increase thousand item
     */
    public static ItemStack increaseThousandItem(int selectedAmount, int maxAmount) {
        return increaseDecreaseItem(Material.EMERALD_BLOCK, 1000, true, selectedAmount, maxAmount);
    }

    /**
     * Returns the decrease one item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the decrease one item
     */
    public static ItemStack decreaseOneItem(int selectedAmount, int maxAmount) {
        return increaseDecreaseItem(Material.RED_STAINED_GLASS_PANE, 1, false, selectedAmount, maxAmount);
    }

    /**
     * Returns the decrease ten item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the decrease ten item
     */
    public static ItemStack decreaseTenItem(int selectedAmount, int maxAmount) {
        return increaseDecreaseItem(Material.RED_STAINED_GLASS, 10, false, selectedAmount, maxAmount);
    }

    /**
     * Returns the decrease hundred item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the decrease hundred item
     */
    public static ItemStack decreaseHundredItem(int selectedAmount, int maxAmount) {
        return increaseDecreaseItem(Material.RED_CONCRETE, 100, false, selectedAmount, maxAmount);
    }

    /**
     * Returns the decrease thousand item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the decrease thousand item
     */
    public static ItemStack decreaseThousandItem(int selectedAmount, int maxAmount) {
        return increaseDecreaseItem(Material.REDSTONE_BLOCK, 1000, false, selectedAmount, maxAmount);
    }

    /**
     * Returns the sell item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the sell item
     */
    public static ItemStack buyItem(int selectedAmount, int maxAmount) {
        List<Component> lore = new ArrayList<>();

        String space = " ".repeat(1);

        lore.add(Component.empty());
        lore.add(Component.text("Kauft die ausgewählte Anzahl von", NamedTextColor.GRAY));

        TextComponent.Builder builder = Component.text();
        builder.append(Component.text(selectedAmount, MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" / ", NamedTextColor.GRAY));
        builder.append(Component.text(maxAmount, MessageManager.VARIABLE_VALUE));

        lore.add(Component.text(space + " ", NamedTextColor.GRAY)
                .append(builder.build())
                .append(Component.text(" Items", MessageManager.VARIABLE_VALUE)));

        lore.add(Component.empty());

        return item(Material.GOLD_BLOCK, 1, 0, Component.text("Kaufen", NamedTextColor.GOLD),
                lore.toArray(Component[]::new));
    }

    /**
     * Returns the reset count item
     *
     * @param selectedAmount the selected amount
     * @param maxAmount      the max amount
     * @return the reset count item
     */
    public static ItemStack resetCountItem(int selectedAmount, int maxAmount) {
        List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Setzt die Anzahl zurück", NamedTextColor.GRAY));

        TextComponent.Builder builder = Component.text();
        builder.append(Component.text(selectedAmount, MessageManager.VARIABLE_VALUE));
        builder.append(Component.text(" / ", NamedTextColor.GRAY));
        builder.append(Component.text(maxAmount, MessageManager.VARIABLE_VALUE));
        lore.add(Component.text("Ausgewählt: ", NamedTextColor.GRAY)
                .append(builder.build())
                .append(Component.text(" Items", MessageManager.VARIABLE_VALUE)));

        lore.add(Component.empty());

        return item(Material.BRUSH, 1, 0, Component.text("Zurücksetzen", NamedTextColor.GOLD),
                lore.toArray(Component[]::new));
    }

    /**
     * Returns the confirmation item
     *
     * @return the confirmation item
     */
    public static ItemStack confirmationConfirmItem() {
        return item(Material.LIME_CONCRETE, 1, 0, Component.text("Bestätigen", NamedTextColor.GOLD),
                Component.empty(), Component.text("Bestätigt die Aktion", NamedTextColor.GRAY),
                Component.empty());
    }

    /**
     * Returns the cancel item
     *
     * @return the cancel item
     */
    public static ItemStack confirmationCancelItem() {
        return item(Material.RED_CONCRETE, 1, 0, Component.text("Abbrechen", NamedTextColor.GOLD),
                Component.empty(), Component.text("Bricht die Aktion ab", NamedTextColor.GRAY),
                Component.empty());
    }

    /**
     * Returns the question item
     *
     * @param displayName the display name
     * @param lore        the lore
     * @return the question item
     */
    public static ItemStack confirmationQuestionItem(Component displayName, Component... lore) {
        return item(Material.ENCHANTED_BOOK, 1, 0, displayName, lore);
    }

    public static ItemStack editSellPrice(Shop shop) {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());

        if (shop.isSelling()) {
            lore.add(Component.text("Aktueller Verkaufspreis: ", MessageManager.VARIABLE_KEY)
                    .append(shop.renderSellPrice()));
        } else {
            lore.add(Component.text("Der Verkauf ist aktuell ", NamedTextColor.GRAY)
                    .append(Component.text("Deaktiviert", NamedTextColor.RED)));
        }

        lore.add(Component.empty());

        if (shop.isSelling()) {
            lore.add(Component.text("Klicke um den Verkaufspreis zu ändern.", MessageManager.INFO));
        } else {
            lore.add(Component.text("Klicke um den Verkauf zu aktivieren.", MessageManager.INFO));
        }

        lore.add(Component.empty());

        if (shop.isSelling()) {
            lore.add(Component.text("Gib einen 0 ein um den Verkauf zu deaktivieren.", NamedTextColor.GRAY, TextDecoration.ITALIC));
            lore.add(Component.empty());
        }


        return item(
                Material.GOLD_INGOT,
                1,
                0,
                Component.text("Verkaufspreis", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack editBuyPrice(Shop shop) {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());

        if (shop.isBuying()) {
            lore.add(Component.text("Aktueller Ankaufspreis: ", MessageManager.VARIABLE_KEY)
                    .append(shop.renderBuyPrice()));
        } else {
            lore.add(Component.text("Der Ankauf ist aktuell ", NamedTextColor.GRAY)
                    .append(Component.text("Deaktiviert", NamedTextColor.RED)));
        }

        lore.add(Component.empty());
        lore.add(Component.empty());
        lore.add(Component.text("Klicke um den Ankaufspreis zu %s".formatted((shop.isBuying()) ? "ändern" : "aktivieren"), MessageManager.INFO));
        lore.add(Component.empty());

        if (shop.isBuying()) {
            lore.add(Component.text("Gib einen 0 ein um den Ankaufs zu deaktivieren.", NamedTextColor.GRAY, TextDecoration.ITALIC));
            lore.add(Component.empty());
        }

        return item(
                Material.EMERALD,
                1,
                0,
                Component.text("Ankaufspreis", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack editAmount(Shop shop) {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());

        lore.add(Component.text("Aktuelle Stückzahl: ", MessageManager.VARIABLE_KEY)
                .append(Component.text(shop.quantity(), MessageManager.VARIABLE_VALUE)));

        lore.add(Component.empty());
        lore.add(Component.empty());
        lore.add(Component.text("Klicke um die Anzahl zu ändern", MessageManager.INFO));
        lore.add(Component.empty());

        return item(
                Material.REDSTONE_TORCH,
                1,
                1,
                Component.text("Stückzahl", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack editDescription(Shop shop) {
        final Optional<Component> optionalDescription = shop.description();
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());

        if (optionalDescription.isEmpty()) {
            lore.add(Component.text("Aktuell ist keine Beschreibung gesetzt", MessageManager.INFO));
        } else {
            lore.add(Component.text("Aktuelle Beschreibung: ", MessageManager.VARIABLE_KEY)
                    .append(optionalDescription.get()));
        }

        lore.add(Component.empty());
        lore.add(Component.empty());
        lore.add(Component.text("Klicke um die Beschreibung zu ändern", MessageManager.INFO));
        lore.add(Component.empty());

        return item(
                Material.WRITABLE_BOOK,
                1,
                0,
                Component.text("Beschreibung", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack editMembers() {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Klicke um die Mitglieder zu bearbeiten", MessageManager.INFO));
        lore.add(Component.empty());

        return item(
                Material.PLAYER_HEAD,
                1,
                0,
                Component.text("Mitglieder", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack addMemberItem() {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Klicke um eine Mitglied hinzuzufügen", MessageManager.INFO));
        lore.add(Component.empty());

        return head(
                /* Green plus */
                "https://textures.minecraft.net/texture/5ff31431d64587ff6ef98c0675810681f8c13bf96f51d9cb07ed7852b2ffd1",
                1,
                Component.text("Mitglied hinzufügen", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack removeMemberItem() {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Klicke um eine Mitglied zu entfernen", MessageManager.INFO));
        lore.add(Component.empty());

        return head(
                /* Red minus */
                "https://textures.minecraft.net/texture/4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46",
                1,
                Component.text("Mitglied entfernen", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack listMembersItem() {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Klicke um dir alle Mitglieder anzuzeigen", MessageManager.INFO));
        lore.add(Component.empty());

        return head(
                /* Box of Infinite Books*/
                "https://textures.minecraft.net/texture/b2bcddc5e30285132b18ffbc3c11f52f4047726a45d042465bf14bdd900739e7",
                1,
                Component.text("Mitgliederliste", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack editStorage(Shop shop) {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Klicke um das Lager zu bearbeiten", MessageManager.INFO));
        lore.add(Component.empty());
        lore.add(Component.text("Aktueller Bestand: ", MessageManager.VARIABLE_KEY)
                .append(Component.text(shop.amount(), MessageManager.VARIABLE_VALUE)));
        lore.add(Component.empty());


        return item(
                Material.CHEST_MINECART,
                1,
                0,
                Component.text("Lager", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack addStorageItem() {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Klicke um Items zum Lager hinzuzufügen", MessageManager.INFO));
        lore.add(Component.empty());

        return head(
                /* Green plus */
                "https://textures.minecraft.net/texture/5ff31431d64587ff6ef98c0675810681f8c13bf96f51d9cb07ed7852b2ffd1",
                1,
                Component.text("Items hinzufügen", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack removeStorageItem() {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Klicke um Items aus dem Lager zu entnehmen", MessageManager.INFO));
        lore.add(Component.empty());

        return head(
                /* Red minus */
                "https://textures.minecraft.net/texture/4e4b8b8d2362c864e062301487d94d3272a6b570afbf80c2c5b148c954579d46",
                1,
                Component.text("Items entnehmen", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }

    public static ItemStack shopStorageInfoItem(Shop shop) {
        final List<Component> lore = new ArrayList<>();

        lore.add(Component.empty());
        lore.add(Component.text("Aktueller Bestand: ", MessageManager.VARIABLE_KEY)
                .append(Component.text(shop.amount(), MessageManager.VARIABLE_VALUE))
                .append(Component.text("x ", MessageManager.VARIABLE_VALUE))
                .append(shop.renderItem()));
        lore.add(Component.empty());

        return item(
                Material.CHEST_MINECART,
                1,
                0,
                Component.text("Lagerbestand", MessageManager.VARIABLE_VALUE),
                lore.toArray(Component[]::new)
        );
    }
}
