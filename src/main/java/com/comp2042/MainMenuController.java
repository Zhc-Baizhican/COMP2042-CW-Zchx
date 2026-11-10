package com.comp2042;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class MainMenuController {

    private static final int X_VALUE = 20;
    private static final int Y_VALUE = 20;
    private static final boolean POWER_UP = true;

    /** Difficulties in the order the button cycles through them. */
    private static final char[] DIFFICULTY_CYCLE = {'N', 'H', 'E'};
    private static final String[] DIFFICULTY_NAMES = {"Normal", "Hard", "Easy"};

    private static final String[] DIFFICULTY_DESCRIPTIONS = {
            "Normal: walls are deadly",
            "Hard: faster, walls are deadly",
            "Easy: walls wrap around"
    };

    @FXML
    private Button btn_difficulty;

    @FXML
    private Label hint;

    private int difficultyIndex = 0;

    private Scene gameScene;

    private ViewManager viewManager = Main.getViewManager();

    public void handleStart() {
        viewManager.initGameStage();

        Stage gameStage = viewManager.getGameStage();
        launchSnake(gameStage);
        gameStage.setScene(gameScene);
        viewManager.getPrimaryStage().close();
        gameStage.show();
    }

    /**
     * Rewrites the controls hint from the current key bindings, so it stays
     * correct after the player rebinds anything.
     */
    private void refreshHints() {
        if (hint == null) {
            return;
        }
        hint.setText(String.format("%s %s %s %s move      %s pause      %s restart",
                keyLabel(Action.MOVE_UP),
                keyLabel(Action.MOVE_LEFT),
                keyLabel(Action.MOVE_DOWN),
                keyLabel(Action.MOVE_RIGHT),
                keyLabel(Action.PAUSE),
                keyLabel(Action.RESTART)));
    }

    private String keyLabel(Action action) {
        return KeyBindings.get().labelFor(action);
    }

    /** Cycles Normal -> Hard -> Easy and reflects the choice on the button. */
    public void handleDifficulty() {
        difficultyIndex = (difficultyIndex + 1) % DIFFICULTY_CYCLE.length;
        updateDifficultyButton();
    }

    @FXML
    private void initialize() {
        updateDifficultyButton();
        if (viewManager != null) {
            viewManager.setOnSettingsClosed(this::refreshHints);
        }
        refreshHints();
    }

    private void updateDifficultyButton() {
        if (btn_difficulty == null) {
            return;
        }
        btn_difficulty.setText(DIFFICULTY_NAMES[difficultyIndex]);
        btn_difficulty.setTooltip(new Tooltip(DIFFICULTY_DESCRIPTIONS[difficultyIndex]));
    }

    public void handleQuit() {
        Platform.exit();
    }

    /** Opens the settings window on top of the main menu. */
    public void handleSettings() {
        viewManager.showSettings();
    }

    private void launchSnake(Stage gameStage) {
        Game game = new Game(X_VALUE, Y_VALUE, POWER_UP, DIFFICULTY_CYCLE[difficultyIndex]);
        Obstacle obstacle = new Obstacle(game.getWidth(), game.getHeight());
        Snake snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        Coin coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        GameView gameView = new GameView(game);
        GameController controller = new GameController(snek, coin, game, gameView, obstacle, gameStage);

        StackPane gamePane = new StackPane();
        gameScene = new Scene(gamePane, game.getWidth() * game.getMultiplier(), game.getHeight() * game.getMultiplier());
        ThemeManager.apply(gamePane);

        gameScene.setOnKeyPressed(controller::handle);

        gameView.makeScene(gameScene, snek, coin);
        gamePane.getChildren().addAll(gameView.gridCanvas, gameView.statusOverlay, gameView.scoreLabel);
        gamePane.setPadding(new Insets(20));
        gamePane.setAlignment(gameView.scoreLabel, Pos.TOP_CENTER);
        StackPane.setMargin(gameView.scoreLabel, new Insets(4, 0, 0, 0));
    }

    public ViewManager getViewManager() {
        return viewManager;
    }

    public static int getxValue() {
        return X_VALUE;
    }

    public static int getyValue() {
        return Y_VALUE;
    }
}
