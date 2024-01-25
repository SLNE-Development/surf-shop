package dev.slne.surf.shop.api.shop.visualizer;

import dev.slne.surf.shop.api.ShopApi;
import dev.slne.surf.shop.api.shop.visualizer.settings.Alignment;
import dev.slne.surf.shop.api.shop.visualizer.settings.BillboardConstraint;
import dev.slne.surf.shop.api.shop.visualizer.settings.DisplayType;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface VisualizerSettings {

    /**
     * Gets the height of the visualizer (in blocks) above the shop (default: 1.25)
     *
     * @return the height
     */
    double getAboveShopHeight();

    /**
     * Sets the height of the visualizer (in blocks) above the shop
     *
     * @param height the height
     * @return this
     */
    VisualizerSettings setAboveShopHeight(@Range(from = 0, to = Short.MAX_VALUE) double height);

    /**
     * Gets the space height (in blocks) between each line (default: 0.35)
     *
     * @return the space height
     */
    double getLineHeight();

    /**
     * Gets the space between the material and the first line (in blocks) (default: 0.1)
     *
     * @return the space height
     */
    double getMaterialLineSpacing();

    /**
     * Sets the space between the material and the first line (in blocks)
     *
     * @param height the space height
     * @return this
     */
    VisualizerSettings setMaterialLineSpacing(@Range(from = 0, to = Short.MAX_VALUE) double height);

    /**
     * Sets the space height (in blocks) between each line
     *
     * @param height the space height
     * @return this
     */
    VisualizerSettings setLineHeight(@Range(from = 0, to = Short.MAX_VALUE) double height);

    /**
     * Gets the <b>squared</b> range in wich the visualizer will be displayed for the player (default: 100 ≙ 10 blocks)
     *
     * @return the range
     */
    int getRangeSquared();

    /**
     * Sets the <b>squared</b> range in wich the visualizer will be displayed for the player
     *
     * @param rangeSquared the range
     * @return this
     */
    VisualizerSettings setRangeSquared(@Range(from = 0, to = Integer.MAX_VALUE) int rangeSquared);

    /**
     * Gets the material settings
     *
     * @return the material settings
     */
    MaterialSettings getMaterialSettings();

    /**
     * Gets the line settings
     *
     * @return the line settings
     */
    LineSettings getLineSettings();

    static VisualizerSettings get() {
        return ShopApi.getInstance().getVisualizerSettings();
    }

    /**
     * Setting for the material display
     */
    interface MaterialSettings {

        /**
         * Gets the scale of the material (1.0 = 100% ≙ 1 block) (default: 0.65)
         *
         * @return the scale
         */
        float getMaterialScale();

        /**
         * Sets the scale of the material (1.0 = 100% ≙ 1 block)
         *
         * @param scale the scale
         * @return this
         */
        MaterialSettings setMaterialScale(float scale);

        /**
         * Gets how the material will rotate to the player (default: {@link BillboardConstraint#FIXED})
         *
         * @return the display type
         * @see BillboardConstraint
         */
        BillboardConstraint getBillboardConstraint();

        /**
         * Sets how the material will rotate to the player
         *
         * @param constraint the display type
         * @return this
         * @see BillboardConstraint
         */
        MaterialSettings setBillboardConstraint(@NotNull BillboardConstraint constraint);

        /**
         * Gets how the material will be displayed (default: {@link DisplayType#FIXED})
         *
         * @return the display type
         */
        DisplayType getDisplayType();

        /**
         * Sets how the material will be displayed
         *
         * @param displayType the display type
         * @return this
         */
        MaterialSettings setDisplayType(@NotNull DisplayType displayType);

        /**
         * Returns to the parent settings
         *
         * @return the parent settings
         */
        VisualizerSettings back();

        /**
         * Gets the material settings
         *
         * @return the material settings
         */
        static MaterialSettings get() {
            return VisualizerSettings.get().getMaterialSettings();
        }
    }

    /**
     * Setting for the line display
     */
    interface LineSettings {

        /**
         * Gets scale of the lines (1.0 = 100%) (default: 0.75)
         *
         * @return the scale
         */
        float getLineScale();

        /**
         * Sets scale of the lines (1.0 = 100%)
         *
         * @param scale the scale
         * @return this
         */
        LineSettings setLineScale(@Range(from = 0, to = Short.MAX_VALUE) float scale);

        /**
         * Gets how the lines will rotate to the player (default: {@link BillboardConstraint#CENTER})
         *
         * @return the display type
         * @see BillboardConstraint
         */
        BillboardConstraint getBillboardConstraint();

        /**
         * Sets how the lines will rotate to the player
         *
         * @param constraint the display type
         * @return this
         * @see BillboardConstraint
         */
        LineSettings setBillboardConstraint(@NotNull BillboardConstraint constraint);

        /**
         * Gets the width of the lines (default: 200)
         *
         * @return the width
         */
        int getLineWidth();

        /**
         * Sets the width of the lines
         *
         * @param width the width
         * @return this
         */
        LineSettings setLineWidth(@Range(from = 0, to = Short.MAX_VALUE) int width);

        /**
         * Gets the color of the background of the text (default: {@link TextColor}{@code .color(0x40000000)})
         *
         * @return the color
         */
        TextColor getBackgroundColor();

        /**
         * Sets the color of the background of the text
         *
         * @param color the color
         * @return this
         */
        LineSettings setBackgroundColor(@NotNull TextColor color);

        /**
         * Gets the text opacity (default: -1 (fully opaque))
         *
         * @return the opacity
         */
        byte getTextOpacity();

        /**
         * Sets the text opacity (default: -1 (fully opaque))
         *
         * @param opacity the opacity
         * @return this
         */
        LineSettings setTextOpacity(byte opacity);

        /**
         * Gets the alignment of the text (default: {@link Alignment#CENTER})
         *
         * @return the alignment
         */
        Alignment getTextAlignment();

        /**
         * Sets the alignment of the text
         *
         * @param alignment the alignment
         * @return this
         */
        LineSettings setTextAlignment(@NotNull Alignment alignment);

        /**
         * Whether the text has a shadow (default: false)
         *
         * @return whether the text has a shadow
         */
        boolean hasShadow();

        /**
         * Sets whether the text has a shadow
         *
         * @param hasShadow whether the text has a shadow
         * @return this
         */
        LineSettings setHasShadow(boolean hasShadow);

        /**
         * Whether the text is see through (default: false)
         *
         * @return whether the text is see through
         */
        boolean isSeeThrough();

        /**
         * Sets whether the text is see through
         *
         * @param isSeeThrough whether the text is see through
         * @return this
         */
        LineSettings setIsSeeThrough(boolean isSeeThrough);

        /**
         * Whether to use the default background color (will override {@link #getBackgroundColor()}) (default: false)
         *
         * @return whether to use the default background color
         */
        boolean useDefaultBackgroundColor();

        /**
         * Sets whether to use the default background color (will override {@link #getBackgroundColor()})
         *
         * @param useDefaultBackgroundColor whether to use the default background color
         * @return this
         */
        LineSettings setUseDefaultBackgroundColor(boolean useDefaultBackgroundColor);

        /**
         * Returns to the parent settings
         *
         * @return the parent settings
         */
        VisualizerSettings back();

        /**
         * Gets the line settings
         *
         * @return the line settings
         */
        static LineSettings get() {
            return VisualizerSettings.get().getLineSettings();
        }
    }
}