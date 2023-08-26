package dev.slne.shop.shop.visualizer;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

/**
 * A class that represents a bitmask for the text display
 *
 * @see <a href="https://wiki.vg/Entity_metadata#Text_Display">Text Display</a>
 */
public enum TextDisplayBitMask {
    HAS_SHADOW(0x01),
    IS_SEE_THROUGH(0x02),
    USE_DEFAULT_BACKGROUND_COLOR(0x04);

    private final int bitmask;

    TextDisplayBitMask(int bitmask) {
        this.bitmask = bitmask;
    }

    /**
     * The alignment of the text
     */
    public enum Alignment {
        LEFT(0),
        CENTER(1),
        RIGHT(2);

        private final int alignment;

        Alignment(int alignment) {
            this.alignment = alignment;
        }
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

        bitmask |= (byte) (alignment.alignment << 3);

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


}
