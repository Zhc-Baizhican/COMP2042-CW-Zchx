package com.comp2042;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Direction}, which replaced the raw character codes the snake
 * used before the key bindings were made configurable.
 */
class DirectionTest {

    @Test
    @DisplayName("opposite directions are detected")
    void oppositePairs() {
        assertTrue(Direction.LEFT.isOpposite(Direction.RIGHT));
        assertTrue(Direction.RIGHT.isOpposite(Direction.LEFT));
        assertTrue(Direction.UP.isOpposite(Direction.DOWN));
        assertTrue(Direction.DOWN.isOpposite(Direction.UP));
    }

    @Test
    @DisplayName("the same or a perpendicular direction is not opposite")
    void nonOppositeDirections() {
        assertFalse(Direction.UP.isOpposite(Direction.UP));
        assertFalse(Direction.UP.isOpposite(Direction.LEFT));
        assertFalse(Direction.LEFT.isOpposite(Direction.DOWN));
    }

    @Test
    @DisplayName("a null direction is never opposite")
    void nullIsNotOpposite() {
        assertFalse(Direction.UP.isOpposite(null));
    }

    @Test
    @DisplayName("each direction maps to the move character the snake expects")
    void moveCharacters() {
        assertEquals('U', Direction.UP.toMoveChar());
        assertEquals('D', Direction.DOWN.toMoveChar());
        assertEquals('L', Direction.LEFT.toMoveChar());
        assertEquals('R', Direction.RIGHT.toMoveChar());
    }
}
