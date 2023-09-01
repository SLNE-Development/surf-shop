package dev.slne.surf.shop.server.shop.gui._2_0.util;

import dev.slne.surf.shop.api.util.ApiUtils;
import org.bukkit.entity.Player;

public class GuiUtils extends ApiUtils {

    /**
     * Prevents instantiation.
     */
    private GuiUtils() {
    }

    /**
     * Plays a gui sound to the player.
     *
     * @param sound  The gui sound to play.
     * @param player The player to play the gui sound to.
     */
    public static void playGuiSound(GuiSound sound, Player player) {
        playSound(sound.getSound(), player);
    }
}
