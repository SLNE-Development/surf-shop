package dev.slne.surf.shop.api.shop.visualizer.settings;

/**
 * The billboard constraint
 *
 * @see <a href="https://wiki.vg/Entity_metadata#Display">Display</a>
 */
public enum BillboardConstraint {
    /**
     * The item will always face the player
     */
    FIXED(0),

    /**
     * The item will face the player horizontally
     */
    VERTICAL(1),

    /**
     * The item will face the player vertically
     */
    HORIZONTAL(2),

    /**
     * The item will always face the player, but will not rotate
     */
    CENTER(3);

    private final byte id;

    BillboardConstraint(int id) {
        this.id = (byte) id;
    }

    public byte getId() {
        return id;
    }
}
