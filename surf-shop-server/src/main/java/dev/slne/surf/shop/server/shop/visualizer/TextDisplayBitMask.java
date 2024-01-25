package dev.slne.surf.shop.server.shop.visualizer;

import com.google.common.base.Preconditions;
import dev.slne.surf.shop.api.shop.visualizer.VisualizerSettings;
import dev.slne.surf.shop.api.shop.visualizer.settings.Alignment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.*;

/**
 * A class that represents a bitmask for the text display
 *
 * @see <a href="https://wiki.vg/Entity_metadata#Text_Display">Text Display</a>
 */
public enum TextDisplayBitMask {
    /**
     * Whether the text has a shadow
     */
    HAS_SHADOW(0x01),

    /**
     * Whether the text is see through
     */
    IS_SEE_THROUGH(0x02),

    /**
     * Whether to use the default background color
     */
    USE_DEFAULT_BACKGROUND_COLOR(0x04);

    private final int bitmask;

    TextDisplayBitMask(int bitmask) {
        this.bitmask = bitmask;
    }


    /**
     * Creates a new bitmask with the given alignment and bitmasks
     *
     * @param alignment the alignment
     * @param bitmasks  the bitmasks
     * @return the bitmask
     */
    public static byte createBitMask(Alignment alignment, TextDisplayBitMask @NotNull ... bitmasks) {
        Preconditions.checkArgument(bitmasks.length < 3, "bitmasks must not be greater than 3");

        byte bitmask = 0;

        for (TextDisplayBitMask textDisplayBitMask : bitmasks) {
            bitmask |= (byte) textDisplayBitMask.bitmask;
        }

        bitmask |= (byte) (alignment.getAlignment() << 3);

        return bitmask;
    }

    /**
     * Creates a new bitmask with the given bitmasks
     *
     * @param bitmasks the bitmasks
     * @return the bitmask
     */
    public static byte createBitMask(TextDisplayBitMask... bitmasks) {
        return createBitMask(Alignment.CENTER, bitmasks);
    }

    /**
     * Creates a new bitmask with the given {@link VisualizerSettings.LineSettings}
     *
     * @param fromSetting the settings
     * @return the bitmask
     */
    public static byte createBitMask(VisualizerSettings.@NotNull LineSettings fromSetting) {
        checkNotNull(fromSetting, "fromSetting must not be null");

        final List<TextDisplayBitMask> enabledBitmasks = new ArrayList<>(3);
        final Alignment alignment = fromSetting.getTextAlignment();
        final boolean hasShadow = fromSetting.hasShadow();
        final boolean seeThrough = fromSetting.isSeeThrough();
        final boolean useDefaultBackgroundColor = fromSetting.useDefaultBackgroundColor();

        if (hasShadow) {
            enabledBitmasks.add(HAS_SHADOW);
        }

        if (seeThrough) {
            enabledBitmasks.add(IS_SEE_THROUGH);
        }

        if (useDefaultBackgroundColor) {
            enabledBitmasks.add(USE_DEFAULT_BACKGROUND_COLOR);
        }

        return createBitMask(alignment, enabledBitmasks.toArray(TextDisplayBitMask[]::new));
    }
}
