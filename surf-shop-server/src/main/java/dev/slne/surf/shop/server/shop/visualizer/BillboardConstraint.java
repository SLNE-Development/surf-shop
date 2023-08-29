package dev.slne.surf.shop.server.shop.visualizer;

/**
 * The billboard constraint
 *
 * @see <a href="https://wiki.vg/Entity_metadata#Display">Display</a>
 */
public enum BillboardConstraint {
    FIXED(0),
    VERTICAL(1),
    HORIZONTAL(2),
    CENTER(3);

    private final int id;

    BillboardConstraint(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
