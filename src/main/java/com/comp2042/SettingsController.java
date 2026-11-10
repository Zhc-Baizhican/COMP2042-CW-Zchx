package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Controller for the settings screen.
 *
 * <p>Changes are applied to {@link Settings} immediately, so leaving the screen
 * through the window close button still persists them. The volume slider is
 * only written to disk when dragging ends, to avoid a file write per pixel.</p>
 */
public class SettingsController {

    private static final String SELECTED_STYLE = "selected";
    private static final String[] WINDOW_SIZE_NAMES = {"Small", "Medium", "Large"};

    @FXML
    private CheckBox soundCheck;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label volumeValue;

    @FXML
    private HBox skinRow;

    @FXML
    private Label skinName;

    @FXML
    private CheckBox darkThemeCheck;

    @FXML
    private HBox sizeRow;

    /** Used to give audible feedback while adjusting the sound settings. */
    private final AudioService preview = new AudioService();

    @FXML
    private void initialize() {
        Settings settings = Settings.get();

        soundCheck.setSelected(settings.isSoundEnabled());
        soundCheck.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            settings.setSoundEnabled(isSelected);
            SettingsStore.save(settings);
        });

        volumeSlider.setValue(settings.getVolume());
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            settings.setVolume(newValue.doubleValue());
            updateVolumeValue();
        });
        volumeSlider.valueChangingProperty().addListener((observable, wasChanging, isChanging) -> {
            if (!isChanging) {
                SettingsStore.save(settings);
            }
        });
        volumeSlider.setOnMouseReleased(event -> SettingsStore.save(settings));

        updateVolumeValue();
        buildSkinSwatches(settings);

        darkThemeCheck.setSelected(settings.isDarkTheme());
        darkThemeCheck.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            settings.setDarkTheme(isSelected);
            SettingsStore.save(settings);
            applyThemeEverywhere();
        });
        buildWindowSizeButtons(settings);

        saveWhenWindowCloses();
    }

    private void buildWindowSizeButtons(Settings settings) {
        for (int i = 0; i < WINDOW_SIZE_NAMES.length; i++) {
            final int index = i;
            Button button = new Button(WINDOW_SIZE_NAMES[i]);
            button.getStyleClass().add("optionButton");
            button.setUserData(index);
            button.setPrefWidth(104);
            button.setPrefHeight(34);
            button.setOnAction(event -> {
                settings.setWindowSizeIndex(index);
                SettingsStore.save(settings);
                highlightSelectedSize(index);
            });
            sizeRow.getChildren().add(button);
        }
        highlightSelectedSize(settings.getWindowSizeIndex());
    }

    private void highlightSelectedSize(int selected) {
        for (Node node : sizeRow.getChildren()) {
            node.getStyleClass().remove(SELECTED_STYLE);
            if (node.getUserData() instanceof Integer index && index == selected) {
                node.getStyleClass().add(SELECTED_STYLE);
            }
        }
    }

    /** Repaints this window and the menu behind it with the current theme. */
    private void applyThemeEverywhere() {
        if (skinRow.getScene() == null) {
            return;
        }
        ThemeManager.apply(skinRow.getScene().getRoot());
        if (skinRow.getScene().getWindow() instanceof Stage stage) {
            ThemeManager.applyTo(stage.getOwner());
        }
    }

    private void buildSkinSwatches(Settings settings) {
        for (SnakeSkin skin : SnakeSkin.values()) {
            Button swatch = new Button();
            swatch.getStyleClass().addAll("skinSwatch", "skin-" + skin.name().toLowerCase());
            swatch.setTooltip(new Tooltip(skin.getDisplayName()));
            swatch.setUserData(skin);
            swatch.setOnAction(event -> {
                settings.setSnakeSkin(skin);
                SettingsStore.save(settings);
                highlightSelectedSkin(settings.getSnakeSkin());
                preview.playCoin();
            });
            skinRow.getChildren().add(swatch);
        }
        highlightSelectedSkin(settings.getSnakeSkin());
    }

    private void highlightSelectedSkin(SnakeSkin selected) {
        for (Node node : skinRow.getChildren()) {
            node.getStyleClass().remove(SELECTED_STYLE);
            if (node.getUserData() == selected) {
                node.getStyleClass().add(SELECTED_STYLE);
            }
        }
        if (skinName != null) {
            skinName.setText("Current: " + selected.getDisplayName());
        }
    }

    private void updateVolumeValue() {
        volumeValue.setText(Math.round(volumeSlider.getValue() * 100) + "%");
    }

    private void saveWhenWindowCloses() {
        // The scene is attached after initialisation, so defer the lookup.
        Platform.runLater(() -> {
            Window window = skinRow.getScene() == null ? null : skinRow.getScene().getWindow();
            if (window != null) {
                window.setOnHidden(event -> SettingsStore.save(Settings.get()));
            }
        });
    }

    @FXML
    private void handleBack() {
        SettingsStore.save(Settings.get());
        preview.stop();
        Window window = skinRow.getScene() == null ? null : skinRow.getScene().getWindow();
        if (window instanceof Stage stage) {
            stage.close();
        }
    }
}
