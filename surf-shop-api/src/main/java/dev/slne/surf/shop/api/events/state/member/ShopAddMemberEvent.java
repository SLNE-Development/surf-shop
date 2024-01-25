package dev.slne.surf.shop.api.events.state.member;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.member.ShopMember;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class ShopAddMemberEvent extends ShopEvent {

    private final ShopMember memberToAdd;

    /**
     * Constructs a new shop event.
     *
     * @param shop        The shop.
     * @param memberToAdd the member to add
     */
    @ApiStatus.Internal
    public ShopAddMemberEvent(Shop shop, ShopMember memberToAdd, boolean async) {
        super(shop, null, async);
        this.memberToAdd = memberToAdd;
    }

    /**
     * Constructs a new shop event.
     *
     * @param shop        The shop.
     * @param memberToAdd the member to add
     */
    @ApiStatus.Internal
    public ShopAddMemberEvent(@Nullable Shop shop, ShopMember memberToAdd) {
        super(shop, null);
        this.memberToAdd = memberToAdd;
    }

    /**
     * Gets the member to add.
     * <p>
     * <b>Note:</b> This member is not yet added to the shop and does not have a {@link ShopMember#getShop()} or {@link ShopMember#getId()}.
     * </p>
     *
     * @return the member to add
     */
    public ShopMember getMemberToAdd() {
        return memberToAdd;
    }
}
