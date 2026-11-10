package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class GameController {

    private Snake snek;
    private Coin coin;
    private GameView gameView;
    private Game game;
    private Stage stage;
    private Obstacle obstacle;
    private final AudioService audio = new AudioService();
    private final Random random = new Random();

    private Direction currentDirection = Direction.LEFT;
    private Direction lastDirection = Direction.LEFT;
    private boolean keyPressed = false;
    private boolean paused = false;

    public GameController(Snake snek, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.snek = snek;
        this.coin = coin;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;

        this.obstacle = obstacle;
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        if (snek.getDead()) {
            return;
        } else {

            snek.move(currentDirection.toMoveChar());

            if (!snek.getDead() && snek.ateCoin(coin)) {
                playCoinSound();
                powerCoin();
                game.incScore();
                if (game.gameWon()) {
                    gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
                    return;
                }
                newCoin();
            }

            keyPressed = false;

            if (!snek.getDead()) {
                gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
            }

            gameView.updateStatus(snek, paused);
            gameView.updateScoreLabel();
        }
    }));

    public void handle(KeyEvent event) {
        Action action = KeyBindings.get().actionFor(event.getCode());
        if (action == null) {
            return;
        }

        if (action.isMovement()) {
            startMoving();
            turnTowards(action.getDirection());
            return;
        }

        switch (action) {
            case PAUSE -> togglePause();
            case RESTART -> restart();
            case FULLSCREEN_ON -> gameView.scaleToFullscreen(true, stage, obstacle, snek, coin);
            case FULLSCREEN_OFF -> gameView.scaleToFullscreen(false, stage, obstacle, snek, coin);
            case BACK_TO_MENU -> backToMenu();
            default -> {
                // Movement actions are handled above.
            }
        }
    }

    /**
     * Starts the game loop, but not while paused: the overlay would stay up
     * with the snake still moving underneath it.
     */
    private void startMoving() {
        if (!paused) {
            animation.play();
        }
    }

    private void turnTowards(Direction direction) {
        if (paused || keyPressed || direction.isOpposite(currentDirection)) {
            return;
        }
        lastDirection = currentDirection;
        currentDirection = direction;
        keyPressed = true;
        gameView.turnDir(snek, lastDirection, currentDirection);
    }

    private void togglePause() {
        paused = !paused;
        if (paused) {
            animation.stop();
        } else {
            animation.play();
        }
        gameView.updateStatus(snek, paused);
    }

    private void restart() {
        reset();
        gameView.scoreLabel.setText("Score: " + game.getScorePoints());
    }

    private void backToMenu() {
        MainMenuController mainMenuController = new MainMenuController();
        mainMenuController.getViewManager().getPrimaryStage().show();
        mainMenuController.getViewManager().getGameStage().close();
    }

    private void reset() {

        animation.jumpTo(Duration.ZERO);
        animation.stop();

        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        animation.setRate(game.getSpeed());
        currentDirection = Direction.LEFT;
        lastDirection = Direction.LEFT;
        keyPressed = false;
        game.resetScore();
        obstacle.reset();

        paused = false;
        gameView.updateStatus(snek, paused);
        gameView.updateScoreLabel();

        gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
    }

    private void powerCoin() {
        animation.setRate(game.getSpeed());
        snek.ignoreBorders(false);
        if (coin.getClass().equals(RainbowCoin.class)) {
            RainbowCoin tempCoin = (RainbowCoin) coin;
            animation.setRate(game.getSpeed() + tempCoin.getSpeedBonus());
        } else if (coin.getClass().equals(SilverCoin.class)){
            int before = obstacle.getObstacles().size();
            obstacle.newObstacles(snek, coin, random.nextInt(game.getWidth() * game.getHeight()/ 20) +3);
            // Only the cells actually added shrink the board; passing the running
            // total would subtract the same obstacles again on every silver coin.
            game.decMaxScore(obstacle.getObstacles().size() - before);
        } else if (coin.getClass().equals(BlueCoin.class)) {
            snek.ignoreBorders(true);
        }
        game.addPoints(coin.getPoints());
    }

    /**
     * Chooses the next coin to appear.
     *
     * <p>The previous version tested {@code ran == 1} twice, so the rainbow coin
     * branch was unreachable and the speed power-up never spawned. The silver
     * coin branch also required the board to have no obstacles at all, which is
     * mutually exclusive with the situation in which it is meant to appear.
     * Each branch now has its own value.</p>
     */
    private void newCoin() {
        if (!game.isPowerUp()) {
            coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);
            return;
        }

        int ran = random.nextInt(12);
        switch (ran) {
            case 1 -> coin = new GoldCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case 2 -> coin = new RainbowCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case 3 -> coin = new BlueCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            case 4 -> coin = new SilverCoin(game.getWidth(), game.getHeight(), snek, obstacle);
            default -> coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);
        }
    }

    /** Plays the pickup sound, subject to the sound settings. */
    private void playCoinSound() {
        audio.playCoin();
    }
}
