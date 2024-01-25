package dev.slne.surf.shop.server.shop.visualizer.setting;

import dev.slne.surf.shop.api.shop.visualizer.VisualizerSettings;
import dev.slne.surf.shop.api.shop.visualizer.settings.Alignment;
import dev.slne.surf.shop.api.shop.visualizer.settings.BillboardConstraint;
import dev.slne.surf.shop.api.shop.visualizer.settings.DisplayType;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static com.google.common.base.Preconditions.*;

/**
 * Implementation of {@link VisualizerSettings}
 */
public class VisualizerSettingsImpl implements VisualizerSettings {

    private final MaterialSettingsImpl materialSettings = new MaterialSettingsImpl();
    private final LineSettingsImpl lineSettings = new LineSettingsImpl();

    private int rangeSquared = (int) Math.pow(10, 2);
    private double aboveShopHeight = 1.25, lineHeight = 0.35, materialLineSpacing = 0.35;

    @Override
    public double getAboveShopHeight() {
        return aboveShopHeight;
    }

    @Override
    public VisualizerSettings setAboveShopHeight(double height) {
        checkArgument(height >= 0, "height must be positive");

        aboveShopHeight = height;
        return this;
    }

    @Override
    public double getLineHeight() {
        return lineHeight;
    }

    @Override
    public double getMaterialLineSpacing() {
        return materialLineSpacing;
    }

    @Override
    public VisualizerSettings setMaterialLineSpacing(double height) {
        checkArgument(height >= 0, "height must be positive");

        materialLineSpacing = height;
        return this;
    }

    @Override
    public VisualizerSettings setLineHeight(double height) {
        checkArgument(height >= 0, "height must be positive");

        lineHeight = height;
        return this;
    }

    @Override
    public int getRangeSquared() {
        return rangeSquared;
    }

    @Override
    public VisualizerSettings setRangeSquared(int rangeSquared) {
        checkArgument(rangeSquared >= 0, "rangeSquared must be positive");

        this.rangeSquared = rangeSquared;
        return this;
    }

    @Override
    public MaterialSettings getMaterialSettings() {
        return materialSettings;
    }

    @Override
    public LineSettings getLineSettings() {
        return lineSettings;
    }

    private class MaterialSettingsImpl implements MaterialSettings {

        private float materialScale = 0.65f;
        private BillboardConstraint billboardConstraint = BillboardConstraint.FIXED;
        private DisplayType displayType = DisplayType.FIXED;

        @Contract(pure = true)
        @Override
        public float getMaterialScale() {
            return materialScale;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public MaterialSettings setMaterialScale(float scale) {
            checkArgument(scale >= 0, "scale must be positive");

            materialScale = scale;
            return this;
        }

        @Contract(pure = true)
        @Override
        public BillboardConstraint getBillboardConstraint() {
            return billboardConstraint;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public MaterialSettings setBillboardConstraint(@NotNull BillboardConstraint constraint) {
            billboardConstraint = checkNotNull(constraint, "constraint must not be null");

            return this;
        }

        @Contract(pure = true)
        @Override
        public DisplayType getDisplayType() {
            return displayType;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public MaterialSettings setDisplayType(@NotNull DisplayType displayType) {
            this.displayType = checkNotNull(displayType, "displayType must not be null");

            return this;
        }

        @Contract(value = " -> this", pure = true)
        @Override
        public VisualizerSettings back() {
            return VisualizerSettingsImpl.this;
        }
    }

    private class LineSettingsImpl implements LineSettings {

        private float lineScale = 0.75f;
        private int lineWidth = 200;
        private byte textOpacity = -1;
        private boolean hasShadow, isSeeThrough, useDefaultBackgroundColor;

        private BillboardConstraint billboardConstraint = BillboardConstraint.CENTER;
        private TextColor backgroundColor = TextColor.color(0x40000000);
        private Alignment textAlignment = Alignment.CENTER;


        @Contract(pure = true)
        @Override
        public float getLineScale() {
            return lineScale;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public LineSettings setLineScale(float scale) {
            checkArgument(scale >= 0, "scale must be positive");

            lineScale = scale;
            return this;
        }

        @Contract(pure = true)
        @Override
        public BillboardConstraint getBillboardConstraint() {
            return billboardConstraint;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public LineSettings setBillboardConstraint(@NotNull BillboardConstraint constraint) {
            billboardConstraint = checkNotNull(constraint, "constraint must not be null");

            return this;
        }

        @Contract(pure = true)
        @Override
        public int getLineWidth() {
            return lineWidth;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public LineSettings setLineWidth(int width) {
            checkArgument(width >= 0, "width must be positive");

            lineWidth = width;
            return this;
        }

        @Contract(pure = true)
        @Override
        public TextColor getBackgroundColor() {
            return backgroundColor;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public LineSettings setBackgroundColor(@NotNull TextColor color) {
            backgroundColor = checkNotNull(color, "color must not be null");

            return this;
        }

        @Contract(pure = true)
        @Override
        public byte getTextOpacity() {
            return textOpacity;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public LineSettings setTextOpacity(byte opacity) {
            textOpacity = opacity;
            return this;
        }

        @Contract(pure = true)
        @Override
        public Alignment getTextAlignment() {
            return textAlignment;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public LineSettings setTextAlignment(@NotNull Alignment alignment) {
            textAlignment = checkNotNull(alignment, "alignment must not be null");

            return this;
        }

        @Contract(pure = true)
        @Override
        public boolean hasShadow() {
            return hasShadow;
        }

        @Contract(value = "_ -> this", mutates = "this")
        @Override
        public LineSettings setHasShadow(boolean hasShadow) {
            this.hasShadow = hasShadow;
            return this;
        }

        @Contract(pure = true)
        @Override
        public boolean isSeeThrough() {
            return isSeeThrough;
        }

        @Contract("_ -> this")
        @Override
        public LineSettings setIsSeeThrough(boolean isSeeThrough) {
            this.isSeeThrough = isSeeThrough;
            return this;
        }

        @Contract(pure = true)
        @Override
        public boolean useDefaultBackgroundColor() {
            return useDefaultBackgroundColor;
        }

        @Contract("_ -> this")
        @Override
        public LineSettings setUseDefaultBackgroundColor(boolean useDefaultBackgroundColor) {
            this.useDefaultBackgroundColor = useDefaultBackgroundColor;
            return this;
        }

        @Contract(value = " -> this", pure = true)
        @Override
        public VisualizerSettings back() {
            return VisualizerSettingsImpl.this;
        }
    }
}
