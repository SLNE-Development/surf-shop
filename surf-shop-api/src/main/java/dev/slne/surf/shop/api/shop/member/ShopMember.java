package dev.slne.surf.shop.api.shop.member;

import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.util.Interable;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Represents a member of a shop.
 */
public interface ShopMember extends Interable<ShopMember> {

    /**
     * Get the id of the member.
     *
     * @return the id
     */
    long getId();

    /**
     * Get the uuid of the member.
     *
     * @return the uuid
     */
    UUID getUuid();

    /**
     * Get the player of the member.
     *
     * @return the player
     */
    OfflinePlayer getPlayer();

    /**
     * Returns the shop wich is associated with this member
     *
     * @return the shop
     */
    Shop getShop();
}
