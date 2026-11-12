package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Controller for the main menu.
 *
 * <p>The menu is deliberately short: it only routes to the difficulty page, the
 * settings screen or the exit. Difficulty selection and starting a game live in
 * {@link PlayMenuController}.</p>
 */
public class MainMenuController {

    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;

    @FXML
    private Label hint;

    private ViewManager viewManager = Main.getViewManager();

    @FXML
    private void initialize() {
        refreshHints();
    }

    /** Opens the difficulty page, where the game is actually started. */
    public void handleStart() {
        viewManager.showPlayMenu();
    }

    /** Opens the settings screen in the same window. */
    public void handleSettings() {
        viewManager.showSettings();
    }

    /** Opens the list of finished runs. */
    public void handleHistory() {
        viewManager.showHistory();
    }

    /** Opens the badge list. */
    public void handleBadges() {
        viewManager.showAchievements();
    }

    public void handleQuit() {
        Platform.exit();
    }

    /**
     * Rewrites the controls hint from the current key bindings, so it stays
     * correct after the player rebinds anything.
     */
    private void refreshHints() {
        if (hint == null) {
            return;
        }
        hint.setText(String.format("%s %s %s %s move      %s pause      %s restart",
                keyLabel(Action.MOVE_UP),
                keyLabel(Action.MOVE_LEFT),
                keyLabel(Action.MOVE_DOWN),
                keyLabel(Action.MOVE_RIGHT),
                keyLabel(Action.PAUSE_MENU),
                keyLabel(Action.RESTART)));
    }

    private String keyLabel(Action action) {
        return KeyBindings.get().labelFor(action);
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public static int getxValue() {
        return X_VALUE;
    }

    public static int getyValue() {
        return Y_VALUE;
    }
}
