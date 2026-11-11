package com.comp2042;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Owns the single application window.
 *
 * <p>Every screen is a scene shown in the same {@link Stage}, built at the square
 * size picked in the settings screen. Scenes are rebuilt on each navigation
 * rather than cached, so a change to the window size takes effect on the next
 * screen instead of leaving a stale scene behind at the old size.</p>
 */
public class ViewManager {

    /** Space kept around the game board inside the window. */
    public static final int BOARD_PADDING = 20;

    /**
     * Square window sizes for the small, medium and large options. The settings
     * screen scrolls, so these can stay comfortably small.
     */
    private static final int[] WINDOW_PIXELS = {480, 600, 760};

    private final Stage stage = new Stage();
    private final Image snakeLogo = new Image("resources/views/res/SnakeLogoWhite.png");

    public ViewManager() {
    }

    public void initalizeMainMenu() {
        stage.setResizable(false);
        stage.getIcons().add(snakeLogo);
        showMenu();
        stage.show();
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * @return the square window size selected in the settings screen
     */
    public int windowSize() {
        int index = Math.max(0, Math.min(WINDOW_PIXELS.length - 1, Settings.get().getWindowSizeIndex()));
        return WINDOW_PIXELS[index];
    }

    /**
     * @return pixels available for the board, after the padding on both sides
     */
    public int boardSize() {
        return windowSize() - 2 * BOARD_PADDING;
    }

    /** Resizes the window to the stored setting without changing the scene. */
    public void applyWindowSize() {
        stage.setWidth(windowSize());
        stage.setHeight(windowSize());
    }

    /** Shows the main menu, leaving fullscreen so the window fits the menu. */
    public void showMenu() {
        stage.setFullScreen(false);
        show("/resources/views/MainMenu.fxml", "Snake Game");
    }

    /** Shows the difficulty page, where the game is started. */
    public void showPlayMenu() {
        show("/resources/views/PlayMenu.fxml", "Snake Game");
    }

    /** Shows the settings screen. */
    public void showSettings() {
        show("/resources/views/Settings.fxml", "Settings");
    }

    /**
     * Shows a freshly built game scene.
     *
     * @param gameScene the scene created by the difficulty page
     */
    public void showGame(Scene gameScene) {
        applyWindowSize();
        stage.setScene(gameScene);
        stage.setTitle("Snake Game");
    }

    /** Returns from the settings screen to the menu. */
    public void closeSettings() {
        showMenu();
    }

    private void show(String resource, String title) {
        Scene scene = loadScene(resource);
        if (scene == null) {
            return;
        }
        applyWindowSize();
        stage.setScene(scene);
        stage.setTitle(title);
    }

    /**
     * Loads an FXML document into a themed scene.
     *
     * @param resource classpath location of the document
     * @return the scene, or {@code null} when the document could not be loaded
     */
    private Scene loadScene(String resource) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(Main.class.getResource(resource));
        try {
            Parent root = loader.load();
            ThemeManager.apply(root);
            return new Scene(root);
        } catch (IOException exception) {
            System.err.println("Could not load " + resource + ": " + exception.getMessage());
            return null;
        }
    }
}
