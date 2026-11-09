package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Random;

public class GameController {
    private static final String COIN_SOUND_RESOURCE = "/resources/views/res/coinSound.wav";

    private Snake snek;
    private Coin coin;
    private GameView gameView;
    private Game game;
    private Stage stage;
    private Obstacle obstacle;
    private MediaPlayer coinSound;
    private final Random random = new Random();

    public static KeyCode key = KeyCode.LEFT;
    public static KeyCode lastKey = KeyCode.LEFT;
    private boolean keyPressed = false;
    private boolean paused = false;

    public GameController(Snake snek, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.snek = snek;
        this.coin = coin;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;

        this.obstacle = obstacle;
        this.coinSound = loadSound(COIN_SOUND_RESOURCE);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        if (snek.getDead()) {
            return;
        } else {

            if (key.equals(KeyCode.LEFT)) {
                snek.move('L');
            } else if (key.equals(KeyCode.RIGHT)) {
                snek.move('R');
            } else if (key.equals(KeyCode.UP)) {
                snek.move('U');
            } else if (key.equals(KeyCode.DOWN)) {
                snek.move('D');
            }

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
        // Arrow keys start the game loop, but must not restart it while paused:
        // the overlay would stay up with the snake still moving underneath it.
        if (event.getCode().isArrowKey() && !paused) {
            animation.play();
        }

        if (valKey(event.getCode()) && !keyPressed) {
            lastKey = key;
            key = event.getCode();
            keyPressed = true;
            gameView.turnDir(snek);
        }

        if (event.getCode().equals(KeyCode.P)) {
            paused = !paused;
            if (paused) {
                animation.stop();
            } else {
                animation.play();
            }
            gameView.updateStatus(snek, paused);
        }

        if (event.getCode().equals(KeyCode.TAB)) {
            reset();
            gameView.scoreLabel.setText("Score: " + game.getScorePoints());
        }

        if (event.getCode().equals(KeyCode.PLUS)) {
            gameView.scaleToFullscreen(true, stage, obstacle, snek, coin);
        }

        if (event.getCode().equals(KeyCode.MINUS)) {
            gameView.scaleToFullscreen(false, stage, obstacle, snek, coin);
        }

        if (event.getCode().equals(KeyCode.ESCAPE)) {
            MainMenuController mainMenuController = new MainMenuController();
            mainMenuController.getViewManager().getPrimaryStage().show();
            mainMenuController.getViewManager().getGameStage().close();
        }
    }

    private boolean valKey(KeyCode key) {
        if (!key.isArrowKey()) return false;
        else {
            if (key.equals(KeyCode.LEFT)) if (!this.key.equals(KeyCode.RIGHT)) return true;
            if (key.equals(KeyCode.RIGHT)) if (!this.key.equals(KeyCode.LEFT)) return true;
            if (key.equals(KeyCode.DOWN)) if (!this.key.equals(KeyCode.DOWN)) return true;
            if (key.equals(KeyCode.UP)) return !this.key.equals(KeyCode.DOWN);
            return false;
        }
    }

    private void reset() {

        animation.jumpTo(Duration.ZERO);
        animation.stop();

        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle);

        animation.setRate(game.getSpeed());
        key = KeyCode.LEFT;
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

    /**
     * Loads a sound clip from the classpath.
     *
     * @param resourcePath absolute classpath location, e.g. {@code /views/res/hit.wav}
     * @return a ready player, or {@code null} when the clip cannot be loaded, so
     *         that a missing audio file never stops the game from running
     */
    private MediaPlayer loadSound(String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                System.err.println("Sound resource not found: " + resourcePath);
                return null;
            }
            return new MediaPlayer(new Media(url.toExternalForm()));
        } catch (Exception e) {
            System.err.println("Sound unavailable: " + resourcePath + " (" + e.getMessage() + ")");
            return null;
        }
    }

    private void playCoinSound() {
        if (coinSound == null) {
            return;
        }
        coinSound.stop();
        coinSound.play();
    }
}
