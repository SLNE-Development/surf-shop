package dev.slne.surf.shop.server.shop.gui._2_0.util;

import org.bukkit.Sound;

public enum GuiSound {

    DENY_ACTION(Sound.ENTITY_VILLAGER_NO),
    CONFIRM_ACTION(Sound.ENTITY_VILLAGER_YES);

    private final Sound sound;

    GuiSound(Sound sound) {
        this.sound = sound;
    }

    /**
     * Gets the sound.
     *
     * @return The sound.
     */
    public Sound getSound() {
        return sound;
    }

}
