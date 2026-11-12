package com.comp2042;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

public class GameView {

    public Canvas gridCanvas;
    public GraphicsContext gc;

    /** Prompt shown before the first key press. */
    public StackPane startOverlay;
    public StackPane statusOverlay;
    private Label statusTitle;
    private Label statusDetail;
    private VBox pauseButtons;
    private VBox statusBox;
    private VBox badgeCards;
    private Button resumeButton;
    private Button restartButton;
    private Button menuButton;
    private Label startTitle;

    private Runnable onResume;
    private Runnable onRestart;
    private Runnable onMainMenu;

    private Game game;
    private double initX;
    private double initY;
    private double fullScale = 1;
    private boolean fullsize = false;
    /** Cells spanned by the painted board, regardless of how big the playfield is. */
    private static final int BOARD_CELLS = 20;

    /** Run time shown in the board corner, formatted as mm:ss. */
    private String timerText = "00:00";

    /** Badges unlocked by the run that just ended, shown one card at a time. */
    private List<Achievement> earnedBadges = List.of();

    /** Timing of the end-of-run intro: panel first, then the text on top. */
    private static final Duration STATUS_FADE = Duration.millis(220);
    private static final Duration STATUS_TEXT_DELAY = Duration.millis(170);
    private static final Duration STATUS_TEXT_IN = Duration.millis(260);
    private static final Duration BADGE_IN = Duration.millis(200);

    private Animation fadeIn;
    private Animation textFadeIn;
    private Animation textPopIn;

    /** Font used for the score painted on the canvas. */
    private static final Font SCORE_FONT = loadScoreFont();

    /** Colour of the "turn left or right" arrows shown at a wall. */
    private static final Color WALL_HINT_COLOR = Color.web("#ffd166");

    private int[][] turnArray;

    public GameView(Game game) {
        this.game = game;
        this.turnArray = new int[game.getWidth()][game.getHeight()];
    }

    public void makeScene(Scene scene, Snake snek, Coin coin) {
        Scene gameScene = scene;
        gridCanvas = new Canvas(gameScene.getWidth(), gameScene.getHeight());
        initX = scene.getWidth();
        initY = scene.getHeight();
        gc = gridCanvas.getGraphicsContext2D();
        drawGrid(coin, snek, gc);

        addStylesheet(scene);

        statusTitle = new Label();
        statusTitle.getStyleClass().add("statusTitle");
        statusDetail = new Label();
        statusDetail.getStyleClass().add("statusDetail");
        statusDetail.setWrapText(true);
        statusDetail.setTextAlignment(TextAlignment.CENTER);

        pauseButtons = buildPauseButtons();
        badgeCards = new VBox(5);
        badgeCards.setAlignment(Pos.CENTER);
        badgeCards.setVisible(false);

        statusBox = new VBox(statusTitle, statusDetail, badgeCards, pauseButtons);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setSpacing(10);

        statusOverlay = new StackPane(statusBox);
        statusOverlay.getStyleClass().add("statusOverlay");
        statusOverlay.setVisible(false);

        startTitle = new Label("Press any key to start");
        startTitle.getStyleClass().add("startTitle");
        startOverlay = new StackPane(startTitle);
        startOverlay.getStyleClass().add("startOverlay");

        updateOverlayLayout();
    }

    /** Buttons shown only while the game is paused. */
    private VBox buildPauseButtons() {
        Button resume = new Button("Resume");
        Button restart = new Button("Restart");
        Button menu = new Button("Main menu");
        for (Button button : new Button[]{resume, restart, menu}) {
            button.getStyleClass().add("menuButton");
            button.setPrefWidth(130);
            button.setPrefHeight(36);
            button.setFocusTraversable(false);
        }
        resume.setOnAction(event -> run(onResume));
        restart.setOnAction(event -> run(onRestart));
        menu.setOnAction(event -> run(onMainMenu));

        resumeButton = resume;
        restartButton = restart;
        menuButton = menu;

        VBox box = new VBox(resume, restart, menu);
        box.setAlignment(Pos.CENTER);
        box.setSpacing(8);
        box.setVisible(false);
        return box;
    }

    private void run(Runnable action) {
        if (action != null) {
            action.run();
        }
    }

    /**
     * Supplies what the pause-menu buttons should do.
     *
     * @param resume    resume the run
     * @param restart   start a fresh run
     * @param mainMenu  leave the game
     */
    public void setPauseActions(Runnable resume, Runnable restart, Runnable mainMenu) {
        this.onResume = resume;
        this.onRestart = restart;
        this.onMainMenu = mainMenu;
    }

    /** Hides the "press any key" prompt once the run starts. */
    public void hideStartPrompt() {
        if (startOverlay != null) {
            startOverlay.setVisible(false);
        }
    }

    /**
     * Pins the score bar and the status overlay to the painted board, so both
     * stay over the board instead of stretching across a screen-sized window.
     */
    private void updateOverlayLayout() {
        if (statusOverlay == null || startOverlay == null) {
            return;
        }
        double cell = game.getMultiplier() * fullScale;
        double boardSize = boardCells() * cell;
        double boardLeft = gridOriginX() + (game.getWidth() - boardCells()) / 2.0 * cell;
        double boardTop = gridOriginY() + (game.getHeight() - boardCells()) / 2.0 * cell;

        // The overlay is clipped to the board: covering the transparent margin
        // would paint a grey frame around the board.
        StackPane.setAlignment(statusOverlay, Pos.TOP_LEFT);
        statusOverlay.setPrefSize(boardSize, boardSize);
        statusOverlay.setMaxSize(boardSize, boardSize);
        statusOverlay.setTranslateX(boardLeft);
        statusOverlay.setTranslateY(boardTop);

        StackPane.setAlignment(startOverlay, Pos.TOP_LEFT);
        startOverlay.setPrefSize(boardSize, boardSize);
        startOverlay.setMaxSize(boardSize, boardSize);
        startOverlay.setTranslateX(boardLeft);
        startOverlay.setTranslateY(boardTop);
    }

    /** Attaches the stylesheet once, at the scene level, instead of per label. */
    private void addStylesheet(Scene scene) {
        URL css = getClass().getResource("/resources/views/styling.css");
        if (css == null) {
            System.err.println("Stylesheet not found: /resources/views/styling.css");
            return;
        }
        scene.getStylesheets().add(css.toExternalForm());
    }

    public void scaleToFullscreen(boolean fullScreen, Stage primaryStage, Obstacle obstacle, Snake snek, Coin coin) {
        primaryStage.setFullScreen(fullScreen);

        // Scale against the matching axis: width against width, height against
        // height. The original code paired width with height, so the board could
        // overflow the screen on wide monitors.
        double widthScale = Screen.getPrimary().getVisualBounds().getWidth() / initX;
        double heightScale = Screen.getPrimary().getVisualBounds().getHeight() / initY;

        if (fullScreen && !fullsize) {
            fullScale = Math.min(widthScale, heightScale);
            gridCanvas.setWidth(gridCanvas.getWidth() * fullScale);
            gridCanvas.setHeight(gridCanvas.getHeight() * fullScale);
            fullsize = true;
        } else if (!fullScreen) {
            fullScale = 1;
            gridCanvas.setWidth(initX);
            gridCanvas.setHeight(initY);
            fullsize = false;
        }

        updateOverlayLayout();
        drawGrid(coin, snek, obstacle, gc, false, 0);
    }

    public void drawGrid(Coin coin, Snake snek, Obstacle obstacle, GraphicsContext gc, boolean a, int b) {
        drawBoardBackground();

        gc.save();
        gc.translate(gridOriginX(), gridOriginY());

        gc.drawImage(coin.getImage(), (coin.getPos().x * game.getMultiplier()) * fullScale, (coin.getPos().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale - 3 * fullScale, game.getMultiplier() * fullScale - 3 * fullScale);

        int bodyIndex = 0;
        for (Point point : snek.getBody()) {
            drawBody(snek, point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);

        drawWallHint(gc, snek);

        gc.setFill(obstacle.getColor());
        for (Point point : obstacle.getObstacles()) {
            gc.fillRect(point.x * game.getMultiplier() * fullScale, point.y * game.getMultiplier() * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
        }

        gc.restore();

        drawScore();
    }

    private void drawGrid(Coin coin, Snake snek, GraphicsContext gc) {
        drawBoardBackground();

        gc.save();
        gc.translate(gridOriginX(), gridOriginY());

        gc.drawImage(coin.getImage(), (coin.getPos().x * game.getMultiplier()) * fullScale, (coin.getPos().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale - 3 * fullScale, game.getMultiplier() * fullScale - 3 * fullScale);

        int bodyIndex = 0;
        for (Point point : snek.getBody()) {
            drawBody(snek, point, bodyIndex);
            bodyIndex++;
        }

        drawHead(snek);

        gc.restore();
    }

    /**
     * Clears the canvas to transparent and paints the board in the middle. The
     * surrounding margin is left transparent on purpose: the game window is a
     * transparent stage, so the desktop shows through there.
     */
    private void drawBoardBackground() {
        gc.clearRect(0, 0, gridCanvas.getWidth(), gridCanvas.getHeight());
        gc.setFill(game.getColor());
        gc.fillRect(boardOriginX(), boardOriginY(), boardPixels(), boardPixels());
    }

    /**
     * Pixels covered by the painted board. The ghost-board hack enlarges the
     * playfield to the whole screen but keeps the board itself the same size, so
     * this is capped at the original cell count.
     */
    private double boardPixels() {
        return Math.min(game.getWidth(), BOARD_CELLS) * game.getMultiplier() * fullScale;
    }

    /** Left edge of the painted board inside the canvas. */
    private double boardOriginX() {
        return gridOriginX() + (game.getWidth() - boardCells()) / 2.0 * game.getMultiplier() * fullScale;
    }

    /** Top edge of the painted board inside the canvas. */
    private double boardOriginY() {
        return gridOriginY() + (game.getHeight() - boardCells()) / 2.0 * game.getMultiplier() * fullScale;
    }

    /** Number of cells the painted board spans. */
    private double boardCells() {
        return Math.min(Math.min(game.getWidth(), game.getHeight()), BOARD_CELLS);
    }

    /**
     * Draws the score straight onto the canvas.
     *
     * <p>The canvas shares the board's coordinate system, so the text is always
     * pinned to the painted board. A label in the layout was drifting away from
     * the board whenever the window size and the playfield size differed.</p>
     */
    private void drawScore() {
        if (gc == null) {
            return;
        }
        String text = "Score: " + game.getScorePoints() + " / " + game.getWinTarget();
        Text measure = new Text(text);
        measure.setFont(SCORE_FONT);
        double padding = 10;
        double width = measure.getLayoutBounds().getWidth() + padding * 2;
        double height = 20;
        double x = boardOriginX() + (boardPixels() - width) / 2;
        double y = boardOriginY() + 6;

        gc.setFill(Color.rgb(0, 0, 0, 0.45));
        gc.fillRoundRect(x, y, width, height, 10, 10);
        gc.setFill(Color.WHITE);
        gc.setFont(SCORE_FONT);
        gc.fillText(text, x + padding, y + height - 6);

        drawTimer();
    }

    /**
     * Draws the run time in the top-right corner of the board.
     */
    private void drawTimer() {
        Text measure = new Text(timerText);
        measure.setFont(SCORE_FONT);
        double padding = 8;
        double width = measure.getLayoutBounds().getWidth() + padding * 2;
        double height = 20;
        double x = boardOriginX() + boardPixels() - width - 6;
        double y = boardOriginY() + 6;

        gc.setFill(Color.rgb(0, 0, 0, 0.45));
        gc.fillRoundRect(x, y, width, height, 10, 10);
        gc.setFill(Color.WHITE);
        gc.setFont(SCORE_FONT);
        gc.fillText(timerText, x + padding, y + height - 6);
    }

    /**
     * @param text the formatted run time, for example {@code 01:23}
     * @return {@code true} when the text actually changed
     */
    public boolean setTimerText(String text) {
        if (text == null || text.equals(timerText)) {
            return false;
        }
        timerText = text;
        return true;
    }

    /** Left edge of the whole playfield inside the canvas. */
    private double gridOriginX() {
        return (gridCanvas.getWidth() - game.getWidth() * game.getMultiplier() * fullScale) / 2;
    }

    /** Top edge of the whole playfield inside the canvas. */
    private double gridOriginY() {
        return (gridCanvas.getHeight() - game.getHeight() * game.getMultiplier() * fullScale) / 2;
    }

    private void drawHead(Snake snek) {
        snek.getImageViewHead().setRotate(snek.getSnakeDir());
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image rotatedImage = snek.getImageViewHead().snapshot(params, null);
        gc.drawImage(rotatedImage, (snek.getHead().x * game.getMultiplier()) * fullScale, (snek.getHead().y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
    }

    private void drawBody(Snake snek, Point point, int index) {
        Image rotatedImage;
        if (snek.getBody().size() <= 1) {
            snek.getImageViewTail().setRotate(snek.getSnakeDir());
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewTail().snapshot(params, null);
        } else if (index == 0) {
            int rotateTail;
            if (snek.getBody().get(1).getX() == snek.getBody().get(0).getX() && snek.getBody().get(1).getY() == snek.getBody().get(0).getY() - 1) {
                rotateTail = 0;
            } else if (snek.getBody().get(1).getX() == snek.getBody().get(0).getX() && snek.getBody().get(1).getY() == snek.getBody().get(0).getY() + 1) {
                rotateTail = 180;
            } else if (snek.getBody().get(1).getX() == snek.getBody().get(0).getX() - 1 && snek.getBody().get(1).getY() == snek.getBody().get(0).getY()) {
                rotateTail = 270;
            } else {
                rotateTail = 90;
            }
            snek.getImageViewTail().setRotate(rotateTail);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewTail().snapshot(params, null);
        } else if (index > 0 && turnValueAt(snek.getBody().get(index)) != 0) {
            snek.getImageViewTurn().setRotate(90 * turnValueAt(snek.getBody().get(index)));
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewTurn().snapshot(params, null);
        } else {
            snek.getImageViewBody().setRotate(snek.getBodyDir(index));
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            rotatedImage = snek.getImageViewBody().snapshot(params, null);
        }

        setTurnValue(snek.getBody().get(0), 0);

        gc.drawImage(rotatedImage, (point.x * game.getMultiplier()) * fullScale, (point.y * game.getMultiplier()) * fullScale, game.getMultiplier() * fullScale, game.getMultiplier() * fullScale);
    }

    /**
     * @return {@code true} when the cell is inside the board
     */
    private boolean insideBoard(int x, int y) {
        return x >= 0 && y >= 0 && x < game.getWidth() && y < game.getHeight();
    }

    /**
     * Reads the recorded turn for a cell. Cells outside the board have no
     * sprite bookkeeping, which matters once a hack lets the snake leave it.
     */
    private int turnValueAt(Point point) {
        int x = (int) point.getX();
        int y = (int) point.getY();
        return insideBoard(x, y) ? turnArray[x][y] : 0;
    }

    /** Records a turn for a cell, ignoring cells outside the board. */
    private void setTurnValue(Point point, int value) {
        int x = (int) point.getX();
        int y = (int) point.getY();
        if (insideBoard(x, y)) {
            turnArray[x][y] = value;
        }
    }

    /**
     * Records how the snake turned at its current head cell, so the correct
     * corner sprite is drawn for that segment.
     *
     * @param snek the snake
     * @param from the direction it was travelling before the turn
     * @param to   the direction it now travels
     */
    public void turnDir(Snake snek, Direction from, Direction to) {
        int value = 0;
        if ((from == Direction.LEFT && to == Direction.UP) || (from == Direction.DOWN && to == Direction.RIGHT)) {
            value = 2;
        } else if ((from == Direction.LEFT && to == Direction.DOWN) || (from == Direction.UP && to == Direction.RIGHT)) {
            value = 3;
        } else if ((from == Direction.RIGHT && to == Direction.UP) || (from == Direction.DOWN && to == Direction.LEFT)) {
            value = 1;
        } else if ((from == Direction.UP && to == Direction.LEFT) || (from == Direction.RIGHT && to == Direction.DOWN)) {
            value = 4;
        }
        setTurnValue(snek.getHead(), value);
    }

    /**
     * Shows the single status overlay for whichever state currently applies.
     * Win and loss take priority over pause, because the snake is not moving in
     * either of those states anyway.
     *
     * @param snek   the snake, used to detect a lost game
     * @param paused whether the player has paused the game
     */
    public void updateStatus(Snake snek, boolean paused) {
        boolean won = game.gameWon();
        boolean dead = snek.getDead();

        if (won) {
            showStatus("YOU WON!", "Score: " + game.getScorePoints());
        } else if (dead) {
            showStatus("GAME OVER", "Score: " + game.getScorePoints());
        } else if (paused) {
            showStatus("PAUSED", "Score: " + game.getScorePoints() + " / " + game.getWinTarget());
        } else {
            hideStatus();
            showMenuButtons(false, false, false);
            return;
        }

        // A finished run keeps the menu buttons so the player can still leave;
        // only a live pause offers Resume.
        showMenuButtons(true, paused && !dead && !won, true);
    }

    /**
     * Shows or hides the pause-menu buttons individually.
     *
     * @param visible       whether the button block is shown at all
     * @param showResume    whether Resume applies to the current state
     * @param showExit      whether Restart and Main menu apply
     */
    private void showMenuButtons(boolean visible, boolean showResume, boolean showExit) {
        if (pauseButtons == null) {
            return;
        }
        pauseButtons.setVisible(visible);
        resumeButton.setVisible(showResume);
        restartButton.setVisible(showExit);
        menuButton.setVisible(showExit);
    }

    /**
     * Draws small arrows beside the head when a wall stopped the snake, pointing
     * left and right so the player knows which way is still open.
     */
    private void drawWallHint(GraphicsContext gc, Snake snek) {
        if (!snek.isWallBlocked()) {
            return;
        }
        double cell = game.getMultiplier() * fullScale;
        Point head = snek.getHead();
        double centreY = head.y * cell + cell / 2;
        double size = cell * 0.26;

        gc.setFill(WALL_HINT_COLOR);
        if (head.x > 0) {
            // Arrow in the cell to the left, pointing outwards.
            double cx = (head.x - 0.5) * cell;
            gc.fillPolygon(
                    new double[]{cx - size, cx + size, cx + size},
                    new double[]{centreY, centreY - size, centreY + size}, 3);
        }
        if (head.x < game.getWidth() - 1) {
            // Arrow in the cell to the right, pointing outwards.
            double cx = (head.x + 1.5) * cell;
            gc.fillPolygon(
                    new double[]{cx + size, cx - size, cx - size},
                    new double[]{centreY, centreY - size, centreY + size}, 3);
        }
    }

    /**
     * Saves a picture of the painted board, for the run history.
     *
     * <p>Only the board is captured: in ghost-board mode the canvas is as large
     * as the screen, and a screenshot of the whole desktop would be useless.</p>
     *
     * @param file where the PNG should be written
     * @return {@code true} when the file was written
     */
    public boolean captureBoard(java.io.File file) {
        if (gridCanvas == null) {
            return false;
        }
        int size = (int) Math.round(boardPixels()) + 1;
        if (size <= 1) {
            return false;
        }
        SnapshotParameters params = new SnapshotParameters();
        params.setViewport(new Rectangle2D(boardOriginX(), boardOriginY(), size, size));
        WritableImage image = new WritableImage(size, size);
        gridCanvas.snapshot(params, image);

        BufferedImage buffered = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        PixelReader reader = image.getPixelReader();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                buffered.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        try {
            return ImageIO.write(buffered, "png", file);
        } catch (Exception e) {
            System.err.println("Could not save the run picture: " + e.getMessage());
            return false;
        }
    }

    /** Key name for the on-screen hints, so they follow a rebind. */
    private String keyLabel(Action action) {
        return KeyBindings.get().labelFor(action);
    }

    private void showStatus(String title, String detail) {
        statusTitle.setText(title);
        statusDetail.setText(detail);

        boolean wasHidden = !statusOverlay.isVisible();
        statusOverlay.setVisible(true);
        if (wasHidden) {
            playStatusIntro();
            playBadgeIntro();
        }
    }

    /**
     * Drops the badges earned by this run into the panel one after another, each
     * card arriving after the previous one.
     */
    private void playBadgeIntro() {
        if (badgeCards == null) {
            return;
        }
        badgeCards.getChildren().clear();
        if (earnedBadges.isEmpty()) {
            badgeCards.setVisible(false);
            return;
        }
        badgeCards.setVisible(true);

        long delayMs = 430;
        for (Achievement badge : earnedBadges) {
            Label card = new Label("★ " + badge.getTitle()
                    + "      " + badge.getTier().getLabel());
            card.getStyleClass().addAll("badgeCard", badge.getTier().getStyleClass());
            card.setOpacity(0);
            card.setScaleX(0.8);
            card.setScaleY(0.8);
            badgeCards.getChildren().add(card);

            FadeTransition fade = new FadeTransition(BADGE_IN, card);
            fade.setDelay(Duration.millis(delayMs));
            fade.setFromValue(0);
            fade.setToValue(1);

            ScaleTransition pop = new ScaleTransition(BADGE_IN, card);
            pop.setDelay(Duration.millis(delayMs));
            pop.setFromX(0.8);
            pop.setFromY(0.8);
            pop.setToX(1);
            pop.setToY(1);
            pop.setInterpolator(Interpolator.EASE_OUT);

            fade.play();
            pop.play();
            delayMs += 170;
        }
    }

    /**
     * Fades the grey panel in first, then pops the text and buttons on top of
     * it, so an ended run lands with a little weight instead of appearing in a
     * single frame.
     */
    private void playStatusIntro() {
        if (statusBox == null) {
            return;
        }
        stopStatusIntro();

        statusOverlay.setOpacity(0);
        statusBox.setOpacity(0);
        statusBox.setScaleX(0.82);
        statusBox.setScaleY(0.82);

        FadeTransition panelIn = new FadeTransition(STATUS_FADE, statusOverlay);
        panelIn.setFromValue(0);
        panelIn.setToValue(1);
        fadeIn = panelIn;

        FadeTransition textIn = new FadeTransition(STATUS_TEXT_IN, statusBox);
        textIn.setDelay(STATUS_TEXT_DELAY);
        textIn.setFromValue(0);
        textIn.setToValue(1);
        textFadeIn = textIn;

        ScaleTransition pop = new ScaleTransition(STATUS_TEXT_IN, statusBox);
        pop.setDelay(STATUS_TEXT_DELAY);
        pop.setFromX(0.82);
        pop.setFromY(0.82);
        pop.setToX(1);
        pop.setToY(1);
        pop.setInterpolator(Interpolator.EASE_OUT);
        textPopIn = pop;

        panelIn.play();
        textIn.play();
        pop.play();
    }

    /** Cancels a running intro so a new state starts from the beginning. */
    private void stopStatusIntro() {
        if (fadeIn != null) {
            fadeIn.stop();
        }
        if (textFadeIn != null) {
            textFadeIn.stop();
        }
        if (textPopIn != null) {
            textPopIn.stop();
        }
        statusOverlay.setOpacity(1);
        if (statusBox != null) {
            statusBox.setOpacity(1);
            statusBox.setScaleX(1);
            statusBox.setScaleY(1);
        }
    }

    /**
     * @param badges the badges unlocked by the run that just ended
     */
    public void setEarnedBadges(List<Achievement> badges) {
        this.earnedBadges = badges == null ? List.of() : List.copyOf(badges);
    }

    private void hideStatus() {
        stopStatusIntro();
        statusOverlay.setVisible(false);
    }

    /** Loads the pixel font for the score, falling back to a system font. */
    private static Font loadScoreFont() {
        try (java.io.InputStream stream = GameView.class.getResourceAsStream("/resources/views/res/SFPixelate.ttf")) {
            if (stream != null) {
                Font loaded = Font.loadFont(stream, 13);
                if (loaded != null) {
                    return loaded;
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load the score font: " + e.getMessage());
        }
        return Font.font("Consolas", 13);
    }

}
