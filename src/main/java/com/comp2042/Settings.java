package com.comp2042;

/**
 * The player-visible settings, shared by the menu, the settings screen and the
 * game.
 *
 * <p>Fields for features that arrive in later iterations (theme, window size
 * and key bindings) are already declared here so that {@link SettingsStore}
 * keeps working when those screens are added; the first iteration only edits
 * sound and the snake skin.</p>
 */
public final class Settings {

    private static final Settings INSTANCE = new Settings();

    private boolean soundEnabled = true;
    private double volume = 0.7;
    private SnakeSkin snakeSkin = SnakeSkin.GREEN;

    // Reserved for the theme and display iteration.
    private boolean darkTheme = false;
    private int windowSizeIndex = 1;

    private Settings() {
        // Single shared instance; use Settings.get().
    }

    /**
     * @return the process-wide settings instance
     */
    public static Settings get() {
        return INSTANCE;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public double getVolume() {
        return volume;
    }

    /**
     * @param volume playback volume, clamped to 0..1
     */
    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
    }

    public SnakeSkin getSnakeSkin() {
        return snakeSkin;
    }

    public void setSnakeSkin(SnakeSkin snakeSkin) {
        this.snakeSkin = snakeSkin == null ? SnakeSkin.GREEN : snakeSkin;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }

    public void setDarkTheme(boolean darkTheme) {
        this.darkTheme = darkTheme;
    }

    public int getWindowSizeIndex() {
        return windowSizeIndex;
    }

    public void setWindowSizeIndex(int windowSizeIndex) {
        this.windowSizeIndex = windowSizeIndex;
    }
}
