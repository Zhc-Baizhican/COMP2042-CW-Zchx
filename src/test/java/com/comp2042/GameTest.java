package com.comp2042;

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

    @Nested
    @DisplayName("board configuration")
    class BoardConfiguration {

        @Test
        @DisplayName("width and height are stored")
        void storesDimensions() {
            Game game = new Game(20, 15, false, 'N');

            assertEquals(20, game.getWidth());
            assertEquals(15, game.getHeight());
        }

        @Test
        @DisplayName("short boards use a multiplier of 20")
        void multiplierForShortBoard() {
            Game game = new Game(20, 20, false, 'N');

            assertEquals(20.0, game.getMultiplier(), EPSILON);
        }

        @Test
        @DisplayName("tall boards use a multiplier of 10")
        void multiplierForTallBoard() {
            Game game = new Game(20, 60, false, 'N');

            assertEquals(10.0, game.getMultiplier(), EPSILON);
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
            assertEquals(8.5, game.getSpeed(), EPSILON);
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
