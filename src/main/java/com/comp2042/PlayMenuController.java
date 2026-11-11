package com.comp2042;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Controller for the difficulty page, reached from the main menu.
 *
 * <p>The main menu no longer cycles through difficulties with a single button:
 * that hid two of the three modes behind a click, and gave no room to explain
 * what each one does. Here every difficulty is a visible choice with a one-line
 * description, and the game only starts once the player presses Play.</p>
 */
public class PlayMenuController {

    private static final String SELECTED_STYLE = "selected";

    private static final char[] DIFFICULTIES = {'E', 'N', 'H'};
    private static final String[] NAMES = {"Easy", "Normal", "Hard"};
    private static final String[] DESCRIPTIONS = {
            "Slower, walls wrap around, coins appear close to the snake, one automatic respawn.",
            "Steady pace with deadly walls.",
            "Faster, deadly walls, and silver coins that litter the board with obstacles."
    };
    private static final int DEFAULT_INDEX = 1;

    @FXML
    private HBox difficultyRow;

    @FXML
    private Label description;

    private final ViewManager viewManager = Main.getViewManager();

    private int selectedIndex = DEFAULT_INDEX;

    @FXML
    private void initialize() {
        for (int i = 0; i < NAMES.length; i++) {
            final int index = i;
            Button button = new Button(NAMES[i]);
            button.getStyleClass().add("optionButton");
            button.setUserData(index);
            button.setPrefWidth(140.0);
            button.setPrefHeight(40.0);
            button.setOnAction(event -> select(index));
            difficultyRow.getChildren().add(button);
        }
        select(DEFAULT_INDEX);
    }

    private void select(int index) {
        selectedIndex = index;
        for (Node node : difficultyRow.getChildren()) {
            node.getStyleClass().remove(SELECTED_STYLE);
            if (node.getUserData() instanceof Integer value && value == index) {
                node.getStyleClass().add(SELECTED_STYLE);
            }
        }
        description.setText(DESCRIPTIONS[index]);
    }

    @FXML
    private void handlePlay() {
        Scene scene = launchSnake(viewManager.getStage(), DIFFICULTIES[selectedIndex]);
        viewManager.showGame(scene);
    }

    @FXML
    private void handleBack() {
        viewManager.showMenu();
    }

    private Scene launchSnake(Stage stage, char difficulty) {
        int columns = MainMenuController.getxValue();
        int rows = MainMenuController.getyValue();

        // One cell size for the whole board, derived from the space the window
        // gives us, so the board fits without changing the window size.
        double cellSize = Math.min(
                (double) (viewManager.boardSize()) / columns,
                (double) (viewManager.boardSize()) / rows);

        Game game = new Game(columns, rows, true, difficulty, cellSize);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        Coin coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle, game.isEasyMode() ? 6 : 0);

        GameView gameView = new GameView(game);
        GameController controller = new GameController(snek, coin, game, gameView, obstacle, stage);

        StackPane gamePane = new StackPane();
        int window = viewManager.windowSize();
        Scene gameScene = new Scene(gamePane, window, window);
        ThemeManager.apply(gamePane);
        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(gameScene, snek, coin);
        gamePane.getChildren().addAll(gameView.gridCanvas, gameView.statusOverlay, gameView.scoreLabel);
        gamePane.setPadding(new Insets(ViewManager.BOARD_PADDING));
        gamePane.setAlignment(gameView.scoreLabel, Pos.TOP_CENTER);
        StackPane.setMargin(gameView.scoreLabel, new Insets(4, 0, 0, 0));
        return gameScene;
    }
}
