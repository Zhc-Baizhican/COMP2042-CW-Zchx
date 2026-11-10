package com.comp2042;

import javafx.scene.Parent;
import javafx.stage.Window;

/**
 * Applies the day / night theme to a scene by adding or removing the
 * {@code dark} style class on its root node.
 *
 * <p>Keeping the switch in one place means the menu, the settings screen and
 * the game window always agree, and the colours themselves stay in
 * {@code styling.css} rather than being scattered through the Java code.</p>
 */
public final class ThemeManager {

    private static final String DARK_CLASS = "dark";

    private ThemeManager() {
        // Utility class.
    }

    /**
     * Applies the current theme to a scene root.
     *
     * @param root the scene root, or {@code null} to do nothing
     */
    public static void apply(Parent root) {
        if (root == null) {
            return;
        }
        root.getStyleClass().remove(DARK_CLASS);
        if (Settings.get().isDarkTheme()) {
            root.getStyleClass().add(DARK_CLASS);
        }
    }

    /**
     * Applies the current theme to the scene inside a window.
     *
     * @param window the window whose scene should be updated, may be {@code null}
     */
    public static void applyTo(Window window) {
        if (window != null && window.getScene() != null) {
            apply(window.getScene().getRoot());
        }
    }
}
