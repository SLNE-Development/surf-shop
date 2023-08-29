package dev.slne.surf.shop.server.shop.member;

import java.util.UUID;

import dev.slne.surf.shop.api.shop.member.ShopMember;
import dev.slne.surf.shop.server.shop.ServerShop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.annotations.SerializedName;

import dev.slne.surf.shop.server.instance.BukkitApi;

public class ServerShopMember implements ShopMember {

    @SerializedName("id")
    private long id;

    @SerializedName("shop_id")
    private long shopId;

    @SerializedName("member_uuid")
    private UUID memberUuid;

    /**
     * A new {@link ServerShopMember} instance
     *
     * @param shop       the shop
     * @param memberUuid the member uuid
     */
    public ServerShopMember(ServerShop shop, UUID memberUuid) {
        this.shopId = shop.getId();
        this.memberUuid = memberUuid;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the memberUuid
     */
    public UUID getUUID() {
        return memberUuid;
    }

    /**
     * Returns the {@link Player} instance of the member
     *
     * @return the {@link Player} instance
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(memberUuid);
    }

    /**
     * @return the shopId
     */
    public long getShopId() {
        return shopId;
    }

    /**
     * Returns the {@link ServerShop} instance of the member
     *
     * @return the {@link ServerShop} instance
     */
    public ServerShop getShop() {
        return BukkitApi.getInstance().getShopManager().getShop(id);
    }

    @Override
    public ShopMember inter() {
        return this;
    }
}
