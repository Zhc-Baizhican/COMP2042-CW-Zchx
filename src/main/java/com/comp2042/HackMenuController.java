package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

/**
 * Controller for the hack list.
 *
 * <p>Every {@link Hack} becomes a check box, and the state is written to disk as
 * soon as it changes, so a chosen combination is still there next launch. The
 * preset row on top applies a ready-made combination and clears whatever was
 * ticked before it.</p>
 */
public class HackMenuController {

    @FXML
    private VBox hackList;

    @FXML
    private HBox presetRow;

    private final Map<Hack, CheckBox> boxes = new EnumMap<>(Hack.class);
    private final ViewManager viewManager = Main.getViewManager();

    /** Set while a preset rewrites the boxes, so their listeners stay quiet. */
    private boolean refreshing;

    @FXML
    private void initialize() {
        buildPresets();
        buildHackList();
        installEscape();
    }

    private void buildPresets() {
        for (HackPreset preset : HackPreset.values()) {
            Button button = new Button(preset.getLabel());
            button.getStyleClass().add("presetButton");
            button.setFocusTraversable(false);
            button.setOnAction(event -> applyPreset(preset));
            presetRow.getChildren().add(button);
        }
    }

    private void applyPreset(HackPreset preset) {
        preset.applyTo(Hacks.get());

        refreshing = true;
        for (Map.Entry<Hack, CheckBox> entry : boxes.entrySet()) {
            entry.getValue().setSelected(Hacks.get().isEnabled(entry.getKey()));
        }
        refreshing = false;

        SettingsStore.save(Settings.get());
    }

    private void buildHackList() {
        for (Hack hack : Hack.values()) {
            CheckBox box = new CheckBox(hack.getDisplayName() + "  —  " + hack.getDescription());
            box.setWrapText(true);
            box.setMaxWidth(Double.MAX_VALUE);
            box.getStyleClass().add("hackCheck");
            box.setSelected(Hacks.get().isEnabled(hack));
            box.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
                if (refreshing) {
                    return;
                }
                Hacks.get().setEnabled(hack, isSelected);
                SettingsStore.save(Settings.get());
            });
            boxes.put(hack, box);
            hackList.getChildren().add(box);
        }
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
