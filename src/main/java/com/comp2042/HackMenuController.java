package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

/**
 * Controller for the hack list.
 *
 * <p>Every {@link Hack} becomes a check box, and the state is written to disk as
 * soon as it changes, so a chosen combination is still there next launch.</p>
 */
public class HackMenuController {

    @FXML
    private VBox hackList;

    private final ViewManager viewManager = Main.getViewManager();

    @FXML
    private void initialize() {
        for (Hack hack : Hack.values()) {
            CheckBox box = new CheckBox(hack.getDisplayName() + "  —  " + hack.getDescription());
            box.setWrapText(true);
            box.setMaxWidth(Double.MAX_VALUE);
            box.getStyleClass().add("hackCheck");
            box.setSelected(Hacks.get().isEnabled(hack));
            box.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
                Hacks.get().setEnabled(hack, isSelected);
                SettingsStore.save(Settings.get());
            });
            hackList.getChildren().add(box);
        }
        installEscape();
    }

    /** Escape returns to the difficulty page, like the Back button. */
    private void installEscape() {
        Platform.runLater(() -> {
            if (hackList.getScene() != null) {
                hackList.getScene().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        handleBack();
                    }
                });
            }
        });
    }

    @FXML
    private void handleBack() {
        viewManager.showPlayMenu();
    }
}
