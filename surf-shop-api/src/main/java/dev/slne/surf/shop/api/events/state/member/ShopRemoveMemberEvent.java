package dev.slne.surf.shop.api.events.state.member;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import dev.slne.surf.shop.api.shop.member.ShopMember;
import org.jetbrains.annotations.NotNull;

public class ShopRemoveMemberEvent extends ShopEvent {

    private final ShopMember memberToRemove;

    /**
     * Constructs a new shop event.
     *
     * @param shop           The shop.
     * @param memberToRemove the member to remove
     */
    public ShopRemoveMemberEvent(@NotNull Shop shop, ShopMember memberToRemove, boolean async) {
        super(shop, null, async);
        this.memberToRemove = memberToRemove;
    }

    public ShopMember getMemberToRemove() {
        return memberToRemove;
    }
}
