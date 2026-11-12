package com.comp2042;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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

    private Stage gameStage;

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
     * @return pixels the game window gives the board. The board fills the
     *         window: a margin only made sense while the window was transparent,
     *         and it showed up as a dead border on machines where the
     *         transparent stage is not supported.
     */
    public int boardSize() {
        return windowSize();
    }

    /** Resizes the window to the stored setting without changing the scene. */
    public void applyWindowSize() {
        stage.setWidth(windowSize());
        stage.setHeight(windowSize());
    }

    /** Shows the main menu, leaving fullscreen so the window fits the menu. */
    public void showMenu() {
        closeGameWindow();
        stage.setFullScreen(false);
        show("/resources/views/MainMenu.fxml", "Snake Game");
        stage.show();
    }

    /** Shows the difficulty page, where the game is started. */
    public void showPlayMenu() {
        show("/resources/views/PlayMenu.fxml", "Snake Game");
    }

    /** Shows the settings screen. */
    public void showSettings() {
        show("/resources/views/Settings.fxml", "Settings");
    }

    /** Shows the list of hack entries. */
    public void showHackMenu() {
        show("/resources/views/HackMenu.fxml", "Hacks");
    }

    /**
     * Shows a game scene in its own transparent window, so the area around the
     * board shows the desktop instead of an opaque background.
     *
     * @param gameScene the scene created by the difficulty page
     */
    public void showGame(Scene gameScene) {
        if (gameStage == null) {
            gameStage = new Stage(StageStyle.TRANSPARENT);
            gameStage.getIcons().add(snakeLogo);
            gameStage.setTitle("Snake Game");
        }
        gameScene.setFill(Color.TRANSPARENT);
        gameStage.setScene(gameScene);
        gameStage.sizeToScene();
        if (gameScene.getWidth() > windowSize()) {
            // A ghost-board run uses a screen-sized scene, so the window has to
            // sit at the top-left of the screen.
            gameStage.setX(0);
            gameStage.setY(0);
        } else {
            gameStage.centerOnScreen();
        }
        gameStage.show();
        stage.hide();
    }

    /** Returns from the settings screen to the menu. */
    public void closeSettings() {
        showMenu();
    }

    /** Hides the game window, if one is open. */
    private void closeGameWindow() {
        if (gameStage != null) {
            gameStage.hide();
        }
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
