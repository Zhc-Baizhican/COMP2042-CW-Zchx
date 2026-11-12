package com.comp2042;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
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
            "Slower, the walls simply stop the snake instead of killing it, coins appear close to the snake, one automatic respawn.",
            "Steady pace with deadly walls.",
            "Faster, deadly walls, and silver coins that litter the board with obstacles."
    };
    private static final int DEFAULT_INDEX = 1;
    /** Cells around the head that a coin may appear in. */
    private static final int COIN_SPAWN_RADIUS = 6;

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
        installEscape();
    }

    /** Escape returns to the main menu, like the Back button. */
    private void installEscape() {
        javafx.application.Platform.runLater(() -> {
            if (difficultyRow.getScene() != null) {
                difficultyRow.getScene().setOnKeyPressed(event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        handleBack();
                    }
                });
            }
        });
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

    @FXML
    private void handleHacks() {
        viewManager.showHackMenu();
    }

    private Scene launchSnake(Stage stage, char difficulty) {
        int boardColumns = MainMenuController.getxValue();
        int boardRows = MainMenuController.getyValue();

        // One cell size for the whole board, derived from the space the window
        // gives us, so the board fits without changing the window size.
        double cellSize = (double) viewManager.boardSize() / boardColumns;

        boolean ghostBoard = Hacks.get().isEnabled(Hack.OFF_BOARD);

        int columns = boardColumns;
        int rows = boardRows;
        int sceneWidth = viewManager.windowSize();
        int sceneHeight = viewManager.windowSize();

        if (ghostBoard) {
            // The playfield becomes the whole screen while the painted board
            // keeps its size, so the extra cells live on a transparent
            // background the player can see through.
            Rectangle2D screen = Screen.getPrimary().getVisualBounds();
            sceneWidth = (int) screen.getWidth();
            sceneHeight = (int) screen.getHeight();
            columns = (int) Math.ceil(sceneWidth / cellSize);
            rows = (int) Math.ceil(sceneHeight / cellSize);
        }

        Game game = new Game(columns, rows, true, difficulty, cellSize);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        // Coins spawn near the head. On the ghost board that keeps them inside
        // the painted board until the snake leaves it, and outside it afterwards,
        // without needing a separate rule.
        int spawnRadius = (ghostBoard || game.isEasyMode()) ? COIN_SPAWN_RADIUS : 0;
        Coin.Area spawnArea = ghostBoard ? Coin.Area.BOARD : Coin.Area.ANY;
        Coin coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle, spawnRadius, spawnArea);

        GameView gameView = new GameView(game);
        GameController controller = new GameController(snek, coin, game, gameView, obstacle, stage);

        StackPane gamePane = new StackPane();
        gamePane.getStyleClass().add("gameScene");
        Scene gameScene = new Scene(gamePane, sceneWidth, sceneHeight);
        // No theme class here on purpose: the game window is transparent around
        // the board, and the dark theme rule would paint an opaque background
        // over the whole scene.
        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(gameScene, snek, coin);
        gamePane.getChildren().addAll(gameView.gridCanvas, gameView.statusOverlay, gameView.startOverlay);
        return gameScene;
    }
}
