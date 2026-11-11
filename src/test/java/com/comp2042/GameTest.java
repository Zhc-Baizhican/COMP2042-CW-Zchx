package com.comp2042;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Game}: board configuration, scoring and the win condition.
 *
 * <p>These are plain JVM tests. {@code Game} deliberately holds no JavaFX
 * nodes, so the tests run without starting the JavaFX toolkit.</p>
 */
class GameTest {

    private static final double EPSILON = 1e-9;

    /**
     * {@link Settings} is a process-wide singleton that can be restored from
     * disk, so every test starts from the smallest window size and the light
     * theme to stay deterministic.
     */
    @BeforeEach
    void resetSettings() {
        Settings.get().setWindowSizeIndex(0);
        Settings.get().setDarkTheme(false);
    }

    @Nested
    @DisplayName("board configuration")
    class BoardConfiguration {

        /** Cell size used by most tests; the window decides it in the app. */
        private static final double CELL = 20.0;

        @Test
        @DisplayName("width and height are stored")
        void storesDimensions() {
            Game game = new Game(20, 15, false, 'N', CELL);

            assertEquals(20, game.getWidth());
            assertEquals(15, game.getHeight());
        }

        @Test
        @DisplayName("the cell size handed in by the view is used as-is")
        void cellSizeIsStored() {
            Game game = new Game(20, 20, false, 'N', 28.5);

            assertEquals(28.5, game.getMultiplier(), EPSILON);
        }

        @Test
        @DisplayName("a tall board does not change the cell size")
        void tallBoardKeepsCellSize() {
            Game game = new Game(20, 60, false, 'N', CELL);

            assertEquals(CELL, game.getMultiplier(), EPSILON);
        }

        @Test
        @DisplayName("the night theme uses a different board colour")
        void nightThemeChangesBoardColour() {
            Game day = new Game(20, 20, false, 'N', CELL);
            Settings.get().setDarkTheme(true);
            Game night = new Game(20, 20, false, 'N', CELL);

            assertFalse(day.getColor().equals(night.getColor()));
        }
    }

    @Nested
    @DisplayName("difficulty")
    class Difficulty {

        @Test
        @DisplayName("easy wraps around the borders")
        void easyWraps() {
            Game game = new Game(20, 20, false, 'E');

            assertTrue(game.isNoBorder());
        }

        @Test
        @DisplayName("normal stops at the borders")
        void normalStops() {
            Game game = new Game(20, 20, false, 'N');

            assertFalse(game.isNoBorder());
            assertEquals(8.5, game.getSpeed(), EPSILON);
        }

        @Test
        @DisplayName("hard is faster and stops at the borders")
        void hardIsFaster() {
            Game game = new Game(20, 20, false, 'H');

            assertFalse(game.isNoBorder());
            assertEquals(13.0, game.getSpeed(), EPSILON);
        }

        @Test
        @DisplayName("an unknown difficulty falls back to normal instead of leaving speed at 0")
        void unknownDifficultyFallsBack() {
            Game game = new Game(20, 20, false, 'X');

            assertEquals(8.5, game.getSpeed(), EPSILON);
            assertFalse(game.isNoBorder());
        }

        @Test
        @DisplayName("only the hard preset counts as hard mode")
        void onlyHardIsHardMode() {
            assertTrue(new Game(20, 20, false, 'H').isHardMode());
            assertFalse(new Game(20, 20, false, 'N').isHardMode());
            assertFalse(new Game(20, 20, false, 'E').isHardMode());
        }

        @Test
        @DisplayName("only easy is easy mode")
        void onlyEasyIsEasyMode() {
            assertTrue(new Game(20, 20, false, 'E').isEasyMode());
            assertFalse(new Game(20, 20, false, 'N').isEasyMode());
            assertFalse(new Game(20, 20, false, 'H').isEasyMode());
        }

        @Test
        @DisplayName("points never drop below zero")
        void pointsClampAtZero() {
            Game game = new Game(20, 20, false, 'E');
            game.addPoints(10);

            game.addPoints(-30);

            assertEquals(0, game.getScorePoints());
        }

        @Test
        @DisplayName("easy is slower than normal, which is slower than hard")
        void speedsIncreaseWithDifficulty() {
            double easy = new Game(20, 20, false, 'E').getSpeed();
            double normal = new Game(20, 20, false, 'N').getSpeed();
            double hard = new Game(20, 20, false, 'H').getSpeed();

            assertTrue(easy < normal, "easy should be slower than normal");
            assertTrue(normal < hard, "normal should be slower than hard");
        }
    }

    @Nested
    @DisplayName("scoring and the win condition")
    class Scoring {

        @Test
        @DisplayName("a new game is not won")
        void notWonAtStart() {
            Game game = new Game(10, 10, false, 'N');

            assertFalse(game.gameWon());
        }

        @Test
        @DisplayName("easy asks for fewer points than normal, and hard for more")
        void targetScoresIncreaseWithDifficulty() {
            int easy = new Game(20, 20, false, 'E').getTargetScore();
            int normal = new Game(20, 20, false, 'N').getTargetScore();
            int hard = new Game(20, 20, false, 'H').getTargetScore();

            assertTrue(easy < normal, "easy target should be lower than normal");
            assertTrue(normal < hard, "hard target should be higher than normal");
        }

        @Test
        @DisplayName("reaching the target score wins the run")
        void reachingTheTargetWins() {
            Game game = new Game(20, 20, false, 'E');

            game.addPoints(game.getTargetScore() - 1);
            assertFalse(game.gameWon());

            game.addPoints(1);
            assertTrue(game.gameWon());
        }

        @Test
        @DisplayName("the board is won once every cell is filled")
        void wonWhenBoardIsFull() {
            Game game = new Game(10, 10, false, 'N'); // capacity: 10 * 10 - 2 = 98

            for (int i = 0; i < 97; i++) {
                game.incScore();
            }
            assertFalse(game.gameWon());

            game.incScore();
            assertTrue(game.gameWon());
        }

        @Test
        @DisplayName("obstacles lower the number of coins needed to win")
        void obstaclesLowerTheThreshold() {
            Game game = new Game(10, 10, false, 'N'); // 98
            game.decMaxScore(8);                      // 90

            for (int i = 0; i < 89; i++) {
                game.incScore();
            }
            assertFalse(game.gameWon());

            game.incScore();
            assertTrue(game.gameWon());
        }

        @Test
        @DisplayName("points accumulate and resetScore clears them")
        void pointsAccumulateAndReset() {
            Game game = new Game(20, 20, false, 'N');

            game.addPoints(5);
            game.addPoints(15);
            assertEquals(20, game.getScorePoints());

            game.incScore();
            game.resetScore();
            assertEquals(0, game.getScorePoints());
            assertFalse(game.gameWon());
        }

        @Test
        @DisplayName("the power-up flag is kept as given")
        void powerUpFlagIsKept() {
            assertTrue(new Game(20, 20, true, 'N').isPowerUp());
            assertFalse(new Game(20, 20, false, 'N').isPowerUp());
        }
    }
}
