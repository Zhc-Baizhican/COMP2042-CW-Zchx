package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller for the badge list.
 *
 * <p>Unlocked badges come first and are marked with a star, so the screen shows
 * progress without hiding what is still missing.</p>
 */
public class AchievementController {

    @FXML
    private VBox badgeList;

    @FXML
    private Label countLabel;

    @FXML
    private Button resetButton;

    private final ViewManager viewManager = Main.getViewManager();

    @FXML
    private void initialize() {
        refresh();
        installEscape();
    }

    private void refresh() {
        badgeList.getChildren().clear();
        int unlocked = Achievements.get().unlockedCount();
        countLabel.setText(unlocked + " of " + Achievement.values().length + " unlocked");

        // The reset is a testing tool, so it only shows with developer mode on.
        boolean developer = Settings.get().isDeveloperMode();
        resetButton.setVisible(developer);
        resetButton.setManaged(developer);

        for (Achievement achievement : Achievements.get().getAll()) {
            badgeList.getChildren().add(buildRow(achievement));
        }
    }

    /** Developer tool: locks every badge again so the unlock flow can be retested. */
    @FXML
    private void handleReset() {
        Achievements.get().clear();
        refresh();
    }

    private HBox buildRow(Achievement achievement) {
        boolean done = Achievements.get().isUnlocked(achievement);
        AchievementTier tier = achievement.getTier();

        HBox row = new HBox(10);
        row.getStyleClass().addAll("badgeRow", done ? "unlocked" : "locked", tier.getStyleClass());
        row.setMaxWidth(Double.MAX_VALUE);

        Label mark = new Label(done ? "★" : "☆");
        mark.getStyleClass().addAll("badgeStar", done ? "starOn" : "starOff", tier.getStyleClass());

        VBox text = new VBox(1);
        Label title = new Label(achievement.getDisplayTitle(done));
        title.getStyleClass().add(done ? "badgeTitle" : "badgeTitleLocked");
        Label description = new Label(achievement.getDisplayDescription(done)
                + "      " + tier.getLabel());
        description.getStyleClass().add("badgeDescription");
        description.setWrapText(true);
        text.getChildren().addAll(title, description);

        row.getChildren().addAll(mark, text);
        return row;
    }

    @FXML
    private void handleBack() {
        viewManager.showMenu();
    }

    /** Escape leaves the badge list, like the Back button. */
    private void installEscape() {
        Platform.runLater(() -> {
            if (badgeList.getScene() != null) {
                badgeList.getScene().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        handleBack();
                    }
                });
            }
        });
    }
}
