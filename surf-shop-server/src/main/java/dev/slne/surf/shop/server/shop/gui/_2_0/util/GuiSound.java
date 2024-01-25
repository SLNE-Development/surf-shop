package dev.slne.surf.shop.server.shop.gui._2_0.util;

import lombok.Getter;
import org.bukkit.Sound;

@Getter
public enum GuiSound {

    DENY_ACTION(Sound.ENTITY_VILLAGER_NO),
    CONFIRM_ACTION(Sound.ENTITY_VILLAGER_YES);

    /**
     * -- GETTER --
     * Gets the sound.
     *
     * @return The sound.
     */
    private final Sound sound;

    GuiSound(Sound sound) {
        this.sound = sound;
    }

}
