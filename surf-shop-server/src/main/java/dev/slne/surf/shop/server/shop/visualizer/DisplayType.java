package dev.slne.surf.shop.server.shop.visualizer;

/**
 * The display type
 *
 * @see <a href="https://wiki.vg/Entity_metadata#Item_Display">Item Display</a>
 */
public enum DisplayType {

    NONE(0),
    THIRD_PERSON_LEFT_HAND(1),
    THIRD_PERSON_RIGHT_HAND(2),
    FIRST_PERSON_LEFT_HAND(3),
    FIRST_PERSON_RIGHT_HAND(4),
    HEAD(5),
    GUI(6),
    GROUND(7),
    FIXED(8);

    private final int id;

    DisplayType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
