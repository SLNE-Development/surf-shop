package dev.slne.surf.shop.api.util;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface Colors {
    TextColor PRIMARY = TextColor.fromHexString("#3b92d1");
    TextColor SECONDARY = TextColor.fromHexString("#5b5b5b");

    TextColor INFO = TextColor.fromHexString("#40d1db");
    TextColor SUCCESS = TextColor.fromHexString("#65ff64");
    TextColor WARNING = TextColor.fromHexString("#f9c353");
    TextColor ERROR = TextColor.fromHexString("#ee3d51");

    TextColor VARIABLE_KEY = INFO;
    TextColor VARIABLE_VALUE = WARNING;
    TextColor SPACER = NamedTextColor.GRAY;
    TextColor DARK_SPACER = NamedTextColor.DARK_GRAY;
}
