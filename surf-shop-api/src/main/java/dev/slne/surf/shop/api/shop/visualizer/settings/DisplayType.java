package dev.slne.surf.shop.api.shop.visualizer.settings;

/**
 * The display type
 *
 * @see <a href="https://wiki.vg/Entity_metadata#Item_Display">Item Display</a>
 */
public enum DisplayType {

    /**
     * No special display
     */
    NONE(0),

    /**
     * The item will be displayed as the player would see it in third person, in the left hand
     */
    THIRD_PERSON_LEFT_HAND(1),

    /**
     * The item will be displayed as the player would see it in third person, in the right hand
     */
    THIRD_PERSON_RIGHT_HAND(2),

    /**
     * The item will be displayed as the player would see it in first person, in the left hand
     */
    FIRST_PERSON_LEFT_HAND(3),

    /**
     * The item will be displayed as the player would see it in first person, in the right hand
     */
    FIRST_PERSON_RIGHT_HAND(4),

    /**
     * The item will be displayed as the player has it in the head slot
     */
    HEAD(5),

    /**
     * The item will be displayed as the player has it in the GUI
     */
    GUI(6),

    /**
     * The item will be displayed as the item is dropped on the ground
     */
    GROUND(7),

    /**
     * The item will be displayed as the item is in the frame
     */
    FIXED(8);

    private final byte id;

    DisplayType(int id) {
        this.id = (byte) id;
    }

    public byte getId() {
        return id;
    }
}
