package com.comp2042;

import com.comp2042.model.BoardGeometry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link BoardGeometry}: the painted board is centred in a larger
 * playfield and never grows past its twenty cells.
 */
class BoardGeometryTest {

    @Test
    @DisplayName("an exactly sized playfield has no offset")
    void exactPlayfield() {
        assertEquals(0, BoardGeometry.left(20));
        assertEquals(0, BoardGeometry.top(20));
        assertEquals(20, BoardGeometry.columns(20));
        assertEquals(20, BoardGeometry.rows(20));
    }

    @Test
    @DisplayName("a screen sized playfield centres a twenty cell board")
    void largerPlayfieldCentresTheBoard() {
        assertEquals(24, BoardGeometry.left(68));
        assertEquals(11, BoardGeometry.top(43));
        assertEquals(20, BoardGeometry.columns(68));
        assertEquals(20, BoardGeometry.rows(43));
    }

    @Test
    @DisplayName("the board test follows the centred rectangle")
    void onBoardFollowsTheRectangle() {
        assertTrue(BoardGeometry.onBoard(24, 11, 68, 43));
        assertTrue(BoardGeometry.onBoard(43, 30, 68, 43));
        assertFalse(BoardGeometry.onBoard(23, 11, 68, 43));
        assertFalse(BoardGeometry.onBoard(44, 30, 68, 43));
        assertFalse(BoardGeometry.onBoard(24, 10, 68, 43));
        assertFalse(BoardGeometry.onBoard(24, 31, 68, 43));
    }

    @Test
    @DisplayName("a small playfield is entirely board")
    void smallPlayfieldIsAllBoard() {
        assertTrue(BoardGeometry.onBoard(0, 0, 12, 12));
        assertTrue(BoardGeometry.onBoard(11, 11, 12, 12));
        assertFalse(BoardGeometry.onBoard(12, 0, 12, 12));
    }
}
