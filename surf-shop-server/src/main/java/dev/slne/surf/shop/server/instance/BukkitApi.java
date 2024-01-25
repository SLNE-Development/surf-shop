package dev.slne.surf.shop.server.instance;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public class BukkitApi {

    /**
     * the {@link BukkitInstance} instance
     */
    @Getter
    @Setter(onParam_ = @NotNull)
    private static BukkitInstance instance;

    /**
     * Private constructor to hide the implicit public one
     */
    private BukkitApi() {
    }
}
