package com.comp2042;

import javafx.scene.effect.ColorAdjust;

/**
 * Colour presets for the snake sprites.
 *
 * <p>The snake is drawn from pixel-art PNG images, so the colour cannot be
 * changed by setting a {@code Color} field. Each sprite gets a
 * {@link ColorAdjust} effect instead, and the snapshot taken for drawing then
 * carries the shifted colour. All four sprites (head, body, turn, tail) must
 * use the same values, otherwise the joins between segments show a seam.</p>
 *
 * <p>{@code hue} is expressed in the {@link ColorAdjust} range of -1..1, where
 * 1 represents +180 degrees. The presets below assume the base sprites are
 * green (about 120 degrees), so a positive shift moves towards blue/purple and
 * a negative shift moves towards orange/red.</p>
 */
public enum SnakeSkin {

    GREEN("Green", 0.00, 0.00, 0.00),
    BLUE("Blue", 0.60, 0.00, 0.00),
    PURPLE("Purple", 0.80, 0.00, 0.00),
    AMBER("Amber", -0.50, 0.10, 0.00),
    MONO("Mono", 0.00, -1.00, 0.10);

    private final String displayName;
    private final double hue;
    private final double saturation;
    private final double brightness;

    SnakeSkin(String displayName, double hue, double saturation, double brightness) {
        this.displayName = displayName;
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    /**
     * Creates a fresh effect instance. Effects must not be shared between
     * nodes, so every sprite asks for its own.
     *
     * @return a {@link ColorAdjust} configured for this skin
     */
    public ColorAdjust createEffect() {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setHue(hue);
        adjust.setSaturation(saturation);
        adjust.setBrightness(brightness);
        return adjust;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves a stored name back to a skin, falling back to {@link #GREEN}.
     *
     * @param name value previously returned by {@link #name()}
     * @return the matching skin, or {@code GREEN} when the name is unknown
     */
    public static SnakeSkin fromName(String name) {
        if (name != null) {
            for (SnakeSkin skin : values()) {
                if (skin.name().equalsIgnoreCase(name.trim())) {
                    return skin;
                }
            }
        }
        return GREEN;
    }
}
