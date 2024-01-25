package dev.slne.surf.shop.api.util;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public class ApiUtils {

    /**
     * Plays a sound to the player.
     *
     * @param toPlay The sound to play.
     * @param player The player to play the sound to.
     */
    public static void playSound(org.bukkit.Sound toPlay, Player player) {
        player.playSound(Sound.sound(toPlay.key(), Sound.Source.MASTER, 0.5f, 1f), Sound.Emitter.self());
    }

    public static Component getOfflineDisplayName(OfflinePlayer offlinePlayer) {
        final String name = offlinePlayer.getName();

        if (!offlinePlayer.isOnline()) {
            return name != null ? Component.text(name, Colors.VARIABLE_VALUE) : Component.text("#UNKNOWN", NamedTextColor.DARK_GRAY);
        }

        return getDisplayName(offlinePlayer.getPlayer());
    }

    public static @NotNull Component getDisplayName(CommandSender sender) {
        Component displayName;
        if (sender instanceof org.bukkit.entity.Entity entity) {
            if (entity instanceof Player player) {
                displayName = player.displayName();
            } else if (entity.customName() != null) {
                displayName = Objects.requireNonNull(entity.customName());
            } else {
                displayName = entity.name();
            }
        } else {
            displayName = sender.name();
        }

        return displayName.colorIfAbsent(Colors.VARIABLE_VALUE);
    }
}
