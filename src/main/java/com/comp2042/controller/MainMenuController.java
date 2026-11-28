package com.comp2042.controller;

import com.comp2042.run.RunHistory;
import com.comp2042.run.RunRecord;
import com.comp2042.settings.Action;
import com.comp2042.settings.KeyBindings;
import com.comp2042.view.Main;
import com.comp2042.view.ViewManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

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

    @FXML
    private Label bestLabel;

    private ViewManager viewManager = Main.getViewManager();

    @FXML
    private void initialize() {
        refreshHints();
        refreshBestScores();
    }

    /**
     * Shows the best score per mode, read straight from the run history so no
     * extra file is needed for it.
     */
    private void refreshBestScores() {
        if (bestLabel == null) {
            return;
        }
        List<RunRecord> records = RunHistory.get().getRecords();
        if (records.isEmpty()) {
            bestLabel.setText("No runs yet");
            return;
        }

        StringBuilder text = new StringBuilder("Best");
        boolean any = false;
        for (String mode : new String[]{"Easy", "Normal", "Hard", "Endless"}) {
            int top = -1;
            for (RunRecord record : records) {
                if (record.getMode().equalsIgnoreCase(mode)) {
                    top = Math.max(top, record.getScore());
                }
            }
            if (top > 0) {
                text.append("      ").append(mode).append(' ').append(top);
                any = true;
            }
        }
        bestLabel.setText(any ? text.toString() : "No runs yet");
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
