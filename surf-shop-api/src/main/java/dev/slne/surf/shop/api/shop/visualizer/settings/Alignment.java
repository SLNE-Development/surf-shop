package dev.slne.surf.shop.api.shop.visualizer.settings;

/**
 * The alignment of the text
 */
public enum Alignment {
    /**
     * The text is centered
     */
    CENTER(0),

    /**
     * The text is left aligned
     */
    LEFT(1),

    /**
     * The text is right aligned
     */
    RIGHT(2);

    private final int alignment;

    Alignment(int alignment) {
        this.alignment = alignment;
    }

    public int getAlignment() {
        return alignment;
    }
}
