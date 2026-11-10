package com.comp2042;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class ViewManager {

    private Stage primaryStage = new Stage();
    private AnchorPane root;

    private Stage gameStage;
    private Stage settingsStage;

    private Image snakeLogo = new Image("resources/views/res/SnakeLogoWhite.png");

    public ViewManager() {
    }

    public void initalizeMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/resources/views/MainMenu.fxml"));
            root = loader.load();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        Scene mainScene = new Scene(root);
        ThemeManager.apply(root);

        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(snakeLogo);
        primaryStage.setTitle("Snake Game");
        primaryStage.show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void initGameStage() {
        gameStage = new Stage();
        gameStage.setTitle("Snake Game");
        gameStage.getIcons().add(snakeLogo);
        gameStage.setResizable(false);
    }

    public Stage getGameStage() {
        return gameStage;
    }

    /**
     * Opens the settings window, creating it the first time it is requested.
     * The window is modal on the main menu so settings cannot be changed while
     * a game is running.
     */
    public void showSettings() {
        if (settingsStage == null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("/resources/views/Settings.fxml"));
            Scene scene;
            try {
                scene = new Scene(loader.load());
            } catch (IOException exception) {
                exception.printStackTrace();
                return;
            }
            ThemeManager.apply(scene.getRoot());

            settingsStage = new Stage();
            settingsStage.initOwner(primaryStage);
            settingsStage.initModality(Modality.WINDOW_MODAL);
            settingsStage.setTitle("Settings");
            settingsStage.getIcons().add(snakeLogo);
            settingsStage.setResizable(false);
            settingsStage.setScene(scene);
        }
        settingsStage.show();
        settingsStage.toFront();
    }
}
