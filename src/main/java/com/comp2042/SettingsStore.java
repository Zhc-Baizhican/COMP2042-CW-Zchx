package com.comp2042;

import javafx.scene.input.KeyCode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Reads and writes {@link Settings} as a small properties file.
 *
 * <p>The file lives under the user's home directory rather than inside the
 * project, so a packaged build never tries to write next to the jar. Any
 * failure (missing file, unreadable file, malformed value) falls back to the
 * defaults instead of stopping the game from starting.</p>
 */
public final class SettingsStore {

    private static final String DIRECTORY = ".comp2042";
    private static final String FILE_NAME = "settings.properties";

    private static final String KEY_SOUND = "sound.enabled";
    private static final String KEY_VOLUME = "sound.volume";
    private static final String KEY_SKIN = "snake.skin";
    private static final String KEY_THEME = "theme.dark";
    private static final String KEY_WINDOW_SIZE = "display.windowSizeIndex";
    private static final String KEY_PREFIX = "key.";

    private SettingsStore() {
        // Utility class.
    }

    /**
     * @return the file the settings are stored in
     */
    public static Path location() {
        return Paths.get(System.getProperty("user.home", "."), DIRECTORY, FILE_NAME);
    }

    /**
     * Fills {@code settings} from disk, leaving fields at their defaults when
     * the file or a value is missing.
     *
     * @param settings the instance to update
     */
    public static void load(Settings settings) {
        Path file = location();
        if (!Files.isRegularFile(file)) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            properties.load(in);
        } catch (IOException e) {
            System.err.println("Could not read settings: " + e.getMessage());
            return;
        }

        settings.setSoundEnabled(readBoolean(properties, KEY_SOUND, settings.isSoundEnabled()));
        settings.setVolume(readDouble(properties, KEY_VOLUME, settings.getVolume()));
        settings.setSnakeSkin(SnakeSkin.fromName(properties.getProperty(KEY_SKIN)));
        settings.setDarkTheme(readBoolean(properties, KEY_THEME, settings.isDarkTheme()));
        settings.setWindowSizeIndex((int) readDouble(properties, KEY_WINDOW_SIZE, settings.getWindowSizeIndex()));
        loadKeyBindings(properties);
    }

    /**
     * Writes {@code settings} to disk, creating the directory when needed.
     *
     * @param settings the instance to persist
     */
    public static void save(Settings settings) {
        Properties properties = new Properties();
        properties.setProperty(KEY_SOUND, Boolean.toString(settings.isSoundEnabled()));
        properties.setProperty(KEY_VOLUME, Double.toString(settings.getVolume()));
        properties.setProperty(KEY_SKIN, settings.getSnakeSkin().name());
        properties.setProperty(KEY_THEME, Boolean.toString(settings.isDarkTheme()));
        properties.setProperty(KEY_WINDOW_SIZE, Integer.toString(settings.getWindowSizeIndex()));
        saveKeyBindings(properties);

        Path file = location();
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                properties.store(out, "COMP2042 Snake settings");
            }
        } catch (IOException e) {
            System.err.println("Could not save settings: " + e.getMessage());
        }
    }

    private static void loadKeyBindings(Properties properties) {
        KeyBindings bindings = KeyBindings.get();
        bindings.resetToDefaults();
        for (Action action : Action.values()) {
            String raw = properties.getProperty(KEY_PREFIX + action.name());
            if (raw == null) {
                continue;
            }
            try {
                bindings.bind(action, KeyCode.valueOf(raw.trim()));
            } catch (IllegalArgumentException e) {
                System.err.println("Unknown key for " + action + ": " + raw);
            }
        }
    }

    private static void saveKeyBindings(Properties properties) {
        KeyBindings bindings = KeyBindings.get();
        for (Action action : Action.values()) {
            properties.setProperty(KEY_PREFIX + action.name(), bindings.keyFor(action).name());
        }
    }

    private static boolean readBoolean(Properties properties, String key, boolean fallback) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        String value = raw.trim();
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return fallback;
    }

    private static double readDouble(Properties properties, String key, double fallback) {
        String raw = properties.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
