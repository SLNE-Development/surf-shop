package dev.slne.shop.shop.member;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.annotations.SerializedName;

import dev.slne.shop.instance.BukkitApi;
import dev.slne.shop.shop.Shop;

public class ShopMember {

    @SerializedName("id")
    private long id;

    @SerializedName("shop_id")
    private long shopId;

    @SerializedName("member_uuid")
    private UUID memberUuid;

    /**
     * A new {@link ShopMember} instance
     *
     * @param shop       the shop
     * @param memberUuid the member uuid
     */
    public ShopMember(Shop shop, UUID memberUuid) {
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
    public UUID getMemberUuid() {
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
     * Returns the {@link Shop} instance of the member
     *
     * @return the {@link Shop} instance
     */
    public Shop getShop() {
        return BukkitApi.getInstance().getShopManager().getShop(id);
    }

}
