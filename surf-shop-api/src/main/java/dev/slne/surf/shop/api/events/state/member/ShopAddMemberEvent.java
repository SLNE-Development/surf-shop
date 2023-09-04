package dev.slne.surf.shop.api.events.state.member;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.member.ShopMember;

public class ShopAddMemberEvent extends ShopEvent {

    private final ShopMember memberToAdd;

    /**
     * Constructs a new shop event.
     *
     * @param shop        The shop.
     * @param memberToAdd the member to add
     */
    public ShopAddMemberEvent(Shop shop, ShopMember memberToAdd, boolean async) {
        super(shop, null, async);
        this.memberToAdd = memberToAdd;
    }

    public ShopMember getMemberToAdd() {
        return memberToAdd;
    }
}
