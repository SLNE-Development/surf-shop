package dev.slne.surf.shop.api.util;

import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;

public class ApiUtils {

    /**
     * Plays a sound to the player.
     *
     * @param toPlay The sound to play.
     * @param player The player to play the sound to.
     */
    public static void playSound(org.bukkit.Sound toPlay, Player player) {
        Sound sound = Sound.sound().type(toPlay.getKey()).volume(0.5f).pitch(1f).source(Sound.Source.MASTER).build();
        Sound.Emitter emitter = Sound.Emitter.self();

        player.playSound(sound, emitter);
    }
}
