package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class GameController {

    private static final int RESPAWN_PENALTY = 30;
    private static final int EASY_COIN_RADIUS = 6;
    private static final int BOARD_CELLS = 20;

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
    private boolean started = false;
    private int respawnsLeft;
    private boolean headLeftBoard = false;
    private boolean spawnOnBoardNext = true;

    public GameController(Snake snek, Coin coin, Game game, GameView gameView, Obstacle obstacle, Stage stage) {
        this.snek = snek;
        this.coin = coin;
        this.gameView = gameView;
        this.game = game;
        this.stage = stage;

        this.obstacle = obstacle;
        this.respawnsLeft = initialRespawns();
        this.gameView.setPauseActions(this::resumeFromPause, this::restart, this::backToMenu);
        animation.setCycleCount(Animation.INDEFINITE);
        animation.setRate(game.getSpeed());
    }

    /**
     * Easy mode gives one automatic respawn; the other difficulties end the run
     * the first time the snake dies.
     *
     * @return number of second chances for this run
     */
    private int initialRespawns() {
        return game.isEasyMode() ? 1 : 0;
    }

    /**
     * Coins spawn within this many cells of the head on easy mode, and on the
     * ghost board so they follow the snake wherever it wanders.
     */
    private int spawnRadius() {
        return (game.isEasyMode() || Hacks.get().isEnabled(Hack.OFF_BOARD)) ? EASY_COIN_RADIUS : 0;
    }

    private final Timeline animation = new Timeline(new KeyFrame(Duration.seconds(1), event -> step()));

    /**
     * Advances the game by one cell.
     *
     * <p>This is the body of the animation tick. Easy mode calls it directly
     * from a key press instead, which is what makes the snake wait for input
     * rather than move on its own.</p>
     */
    private void step() {
        if (snek.getDead()) {
            return;
        }

        snek.move(currentDirection.toMoveChar());

        if (!headLeftBoard && isHeadOffBoard()) {
            headLeftBoard = true;
        }

        if (snek.getDead() && tryRespawn()) {
            return;
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
    }

    public void handle(KeyEvent event) {
        Action action = KeyBindings.get().actionFor(event.getCode());
        if (action == null) {
            return;
        }

        // The run does not begin until the player presses something: the first
        // key press only dismisses the prompt.
        if (!started) {
            started = true;
            gameView.hideStartPrompt();
            if (Hacks.get().isEnabled(Hack.WIN)) {
                game.winNow();
                gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
                gameView.updateStatus(snek, paused);
                return;
            }
            animation.play();
            return;
        }

        if (action.isMovement()) {
            handleMovement(action.getDirection());
            return;
        }

        switch (action) {
            case PAUSE_MENU -> openPauseMenu();
            case RESTART -> restart();
            case FULLSCREEN_ON -> gameView.scaleToFullscreen(true, stage, obstacle, snek, coin);
            case FULLSCREEN_OFF -> gameView.scaleToFullscreen(false, stage, obstacle, snek, coin);
            default -> {
                // Movement actions are handled above.
            }
        }
    }

    /** Resumes the run when the pause menu asks for it. */
    private void resumeFromPause() {
        if (paused) {
            togglePause();
        }
    }

    /**
     * Pause menu on a live run; straight back to the menu once the run ended, so
     * a finished game can never trap the player.
     */
    private void openPauseMenu() {
        if (snek.getDead() || game.gameWon()) {
            backToMenu();
        } else {
            togglePause();
        }
    }

    /**
     * Starts the game loop and turns the snake. The loop is not restarted while
     * paused: the overlay would stay up with the snake moving underneath it.
     */
    private void handleMovement(Direction direction) {
        if (paused || snek.getDead()) {
            return;
        }
        animation.play();
        turnTowards(direction);
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
    }

    private void backToMenu() {
        // The window is reused, so the timer has to be stopped here: nothing
        // else would stop the snake of an abandoned run.
        animation.stop();
        Main.getViewManager().showMenu();
    }

    /** Full restart from the restart key: score and second chance are reset. */
    private void reset() {
        game.resetScore();
        respawnsLeft = initialRespawns();
        resetBoard();
    }

    /**
     * Rebuilds the snake and the coin without touching the score. Used by both
     * the restart key and the easy-mode respawn, so the two paths cannot drift
     * apart.
     */
    private void resetBoard() {
        animation.jumpTo(Duration.ZERO);
        animation.stop();

        // Clear the obstacles first so the new coin cannot be placed on a cell
        // that is about to become free again.
        obstacle.reset();

        snek = new Snake(game.getWidth(), game.getHeight(), game.isNoBorder(), obstacle);
        headLeftBoard = false;
        spawnOnBoardNext = true;
        Coin.Area area = Hacks.get().isEnabled(Hack.OFF_BOARD) ? Coin.Area.BOARD : Coin.Area.ANY;
        coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle, spawnRadius(), area);

        animation.setRate(game.getSpeed());
        currentDirection = Direction.LEFT;
        lastDirection = Direction.LEFT;
        keyPressed = false;
        paused = false;

        gameView.updateStatus(snek, paused);
        gameView.drawGrid(coin, snek, obstacle, gameView.gc, false, 0);
    }

    /**
     * Spends the second chance if the difficulty grants one.
     *
     * @return {@code true} when the snake was respawned and the run continues
     */
    private boolean tryRespawn() {
        if (respawnsLeft <= 0) {
            return false;
        }
        respawnsLeft--;
        game.addPoints(-RESPAWN_PENALTY);
        resetBoard();
        // resetBoard stops the timer for a manual restart; a respawn has to keep
        // the snake moving, otherwise the run appears to freeze.
        animation.play();
        return true;
    }

    /**
     * Applies the effect of the coin that was just eaten and awards its points.
     *
     * <p>Each coin carries its original effect: the rainbow coin speeds the game
     * up, the silver coin litters the board with obstacles, and the blue coin
     * lets the snake ignore the borders for a while. The very first coin of a
     * run resets both modifiers, so no effect leaks between coins.</p>
     */
    private void powerCoin() {
        animation.setRate(game.getSpeed());
        snek.ignoreBorders(false);

        if (coin instanceof RainbowCoin rainbow) {
            animation.setRate(game.getSpeed() + rainbow.getSpeedBonus());
        } else if (coin instanceof SilverCoin) {
            int before = obstacle.getObstacles().size();
            obstacle.newObstacles(snek, coin, random.nextInt(game.getWidth() * game.getHeight() / 20) + 3);
            // Only the cells actually added shrink the board; passing the running
            // total would subtract the same obstacles again on every silver coin.
            game.decMaxScore(obstacle.getObstacles().size() - before);
        } else if (coin instanceof BlueCoin) {
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
        int radius = spawnRadius();
        Coin.Area area = spawnArea();
        if (!game.isPowerUp()) {
            coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle, radius, area);
            return;
        }

        int ran = random.nextInt(12);
        switch (ran) {
            case 1 -> coin = new GoldCoin(game.getWidth(), game.getHeight(), snek, obstacle, radius, area);
            case 2 -> coin = new RainbowCoin(game.getWidth(), game.getHeight(), snek, obstacle, radius, area);
            case 3 -> coin = new BlueCoin(game.getWidth(), game.getHeight(), snek, obstacle, radius, area);
            // The silver coin litters the board with obstacles, which is a hard
            // mode mechanic only.
            case 4 -> coin = game.isHardMode()
                    ? new SilverCoin(game.getWidth(), game.getHeight(), snek, obstacle, radius, area)
                    : new Coin(game.getWidth(), game.getHeight(), snek, obstacle, radius, area);
            default -> coin = new Coin(game.getWidth(), game.getHeight(), snek, obstacle, radius, area);
        }
    }

    /**
     * Chooses the region the next coin may appear in.
     *
     * <p>On the ghost board the coins stay on the painted board until the snake
     * first leaves it. From then on they alternate: one on the board, one in the
     * transparent area around it.</p>
     *
     * @return the region for the next coin
     */
    private Coin.Area spawnArea() {
        if (!Hacks.get().isEnabled(Hack.OFF_BOARD)) {
            return Coin.Area.ANY;
        }
        if (!headLeftBoard) {
            return Coin.Area.BOARD;
        }
        spawnOnBoardNext = !spawnOnBoardNext;
        return spawnOnBoardNext ? Coin.Area.BOARD : Coin.Area.OUTSIDE;
    }

    /**
     * @return {@code true} once the snake's head is outside the painted board
     */
    private boolean isHeadOffBoard() {
        int columns = game.getWidth();
        int rows = game.getHeight();
        int boardColumns = Math.min(columns, BOARD_CELLS);
        int boardRows = Math.min(rows, BOARD_CELLS);
        int left = (columns - boardColumns) / 2;
        int top = (rows - boardRows) / 2;
        java.awt.Point head = snek.getHead();
        return head.x < left || head.x >= left + boardColumns
                || head.y < top || head.y >= top + boardRows;
    }

    /** Plays the pickup sound, subject to the sound settings. */
    private void playCoinSound() {
        audio.playCoin();
    }
}
