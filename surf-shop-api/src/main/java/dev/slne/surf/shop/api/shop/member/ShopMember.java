package dev.slne.surf.shop.api.shop.member;

import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.util.Interable;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public interface ShopMember extends Interable<ShopMember> {

    long getId();

    UUID getUUID();

    OfflinePlayer getPlayer();

    long getShopId();

    Shop getShop();
}
