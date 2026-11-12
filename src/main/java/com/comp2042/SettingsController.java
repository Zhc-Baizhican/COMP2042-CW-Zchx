package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

/**
 * Controller for the settings screen.
 *
 * <p>Changes are applied to {@link Settings} and {@link KeyBindings}
 * immediately, so leaving the screen through the window close button still
 * persists them. The volume slider is only written to disk when dragging ends,
 * to avoid a file write per pixel.</p>
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
    private CheckBox devModeCheck;

    @FXML
    private HBox sizeRow;

    @FXML
    private VBox keyRowBox;

    @FXML
    private Label keyHint;

    private final Map<Action, Button> keyButtons = new EnumMap<>(Action.class);

    /** The action currently waiting for a key press, or {@code null}. */
    private Action capturing;

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

        darkThemeCheck.setSelected(settings.isDarkTheme());
        darkThemeCheck.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            settings.setDarkTheme(isSelected);
            SettingsStore.save(settings);
            applyThemeEverywhere();
        });

        devModeCheck.setSelected(settings.isDeveloperMode());
        devModeCheck.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
            settings.setDeveloperMode(isSelected);
            SettingsStore.save(settings);
        });

        updateVolumeValue();
        buildSkinSwatches(settings);
        buildWindowSizeButtons(settings);
        buildKeyRows();
        saveWhenWindowCloses();
        installKeyCapture();
        installEscapeToMenu();
    }

    /** Escape leaves the settings screen, like the Back button. */
    private void installEscapeToMenu() {
        Platform.runLater(() -> {
            if (skinRow.getScene() != null) {
                skinRow.getScene().setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE && capturing == null) {
                        handleBack();
                    }
                });
            }
        });
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
                Main.getViewManager().applyWindowSize();
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

    private void buildKeyRows() {
        for (Action action : Action.values()) {
            Label name = new Label(action.getDisplayName());
            name.getStyleClass().add("settingLabel");
            name.setMinWidth(148.0);
            name.setPrefWidth(148.0);

            Button keyButton = new Button(keyText(action));
            keyButton.getStyleClass().add("keyButton");
            keyButton.setMinWidth(132.0);
            keyButton.setPrefWidth(132.0);
            keyButton.setOnAction(event -> beginCapture(action));

            HBox row = new HBox(8.0, name, keyButton);
            row.setAlignment(Pos.CENTER_LEFT);
            keyRowBox.getChildren().add(row);
            keyButtons.put(action, keyButton);
        }
    }

    private String keyText(Action action) {
        return KeyBindings.get().keyFor(action).getName();
    }

    private void beginCapture(Action action) {
        cancelCapture();
        capturing = action;
        keyButtons.get(action).setText("Press a key...");
        keyHint.setText("Press any key to bind " + action.getDisplayName() + "   (Esc cancels)");
    }

    /**
     * Intercepts key presses while a binding is being captured. The filter sits
     * on the scene so the press never reaches the buttons underneath.
     */
    private void installKeyCapture() {
        Platform.runLater(() -> {
            if (keyRowBox.getScene() != null) {
                keyRowBox.getScene().addEventFilter(KeyEvent.KEY_PRESSED, this::captureKey);
            }
        });
    }

    private void captureKey(KeyEvent event) {
        if (capturing == null) {
            return;
        }
        event.consume();
        KeyCode code = event.getCode();

        if (code == KeyCode.ESCAPE) {
            keyHint.setText("Binding cancelled");
            cancelCapture();
            return;
        }

        Action owner = KeyBindings.get().actionFor(code);
        if (owner != null && owner != capturing) {
            keyHint.setText(code.getName() + " is already used by " + owner.getDisplayName());
            cancelCapture();
            return;
        }

        KeyBindings.get().bind(capturing, code);
        SettingsStore.save(Settings.get());
        keyHint.setText("Bound " + capturing.getDisplayName() + " to " + code.getName());
        cancelCapture();
    }

    private void cancelCapture() {
        if (capturing != null) {
            keyButtons.get(capturing).setText(keyText(capturing));
        }
        capturing = null;
    }

    private void updateVolumeValue() {
        volumeValue.setText(Math.round(volumeSlider.getValue() * 100) + "%");
    }

    /** Repaints the settings screen with the current theme. */
    private void applyThemeEverywhere() {
        if (skinRow.getScene() != null) {
            ThemeManager.apply(skinRow.getScene().getRoot());
        }
    }

    private void saveWhenWindowCloses() {
        // Settings are written on every change and again on Back; there is no
        // separate window to close any more, so nothing extra is needed here.
    }

    @FXML
    private void handleBack() {
        SettingsStore.save(Settings.get());
        preview.stop();
        Main.getViewManager().closeSettings();
    }
}
