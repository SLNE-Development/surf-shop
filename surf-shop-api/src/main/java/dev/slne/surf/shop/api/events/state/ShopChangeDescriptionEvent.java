package dev.slne.surf.shop.api.events.state;

import dev.slne.surf.shop.api.events.ShopEvent;
import dev.slne.surf.shop.api.shop.Shop;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ShopChangeDescriptionEvent extends ShopEvent {

    private @Nullable Component newDescription;

    /**
     * Constructs a new shop event.
     *
     * @param shop           The shop.
     * @param newDescription the new description
     */
    @ApiStatus.Internal
    public ShopChangeDescriptionEvent(@Nullable Shop shop, @Nullable Component newDescription, boolean async) {
        super(shop, null, async);
        this.newDescription = newDescription;
    }

    @ApiStatus.Internal
    public ShopChangeDescriptionEvent(Shop shop, @Nullable Component originDescription) {
        super(shop, null);
        this.newDescription = originDescription;
    }

    public Optional<Component> getNewDescription() {
        return Optional.ofNullable(newDescription);
    }

    public void setNewDescription(@Nullable Component newDescription) {
        this.newDescription = newDescription;
    }
}
