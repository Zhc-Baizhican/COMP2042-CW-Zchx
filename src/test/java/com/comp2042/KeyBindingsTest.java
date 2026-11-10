package com.comp2042;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link KeyBindings}: defaults, rebinding and conflict lookup.
 */
class KeyBindingsTest {

    private KeyBindings bindings;

    @BeforeEach
    void freshBindings() {
        bindings = KeyBindings.get();
        bindings.resetToDefaults();
    }

    @Test
    @DisplayName("every action starts on its default key")
    void defaultsAreApplied() {
        for (Action action : Action.values()) {
            assertEquals(action.getDefaultKey(), bindings.keyFor(action), action + " default");
        }
    }

    @Test
    @DisplayName("no two actions share a default key")
    void defaultsAreUnique() {
        EnumSet<KeyCode> seen = EnumSet.noneOf(KeyCode.class);
        for (Action action : Action.values()) {
            assertTrue(seen.add(action.getDefaultKey()),
                    "duplicate default key " + action.getDefaultKey() + " for " + action);
        }
    }

    @Test
    @DisplayName("a key resolves back to the action that owns it")
    void actionForFindsOwner() {
        assertEquals(Action.MOVE_UP, bindings.actionFor(KeyCode.UP));
        assertEquals(Action.PAUSE, bindings.actionFor(KeyCode.P));
    }

    @Test
    @DisplayName("an unbound key resolves to nothing")
    void actionForReturnsNullForUnboundKey() {
        assertNull(bindings.actionFor(KeyCode.F7));
    }

    @Test
    @DisplayName("rebinding moves the key and frees the old one")
    void rebindingMovesTheKey() {
        bindings.bind(Action.MOVE_UP, KeyCode.W);

        assertEquals(KeyCode.W, bindings.keyFor(Action.MOVE_UP));
        assertEquals(Action.MOVE_UP, bindings.actionFor(KeyCode.W));
        assertNull(bindings.actionFor(KeyCode.UP));
    }

    @Test
    @DisplayName("resetToDefaults undoes any rebinding")
    void resetRestoresDefaults() {
        bindings.bind(Action.PAUSE, KeyCode.SPACE);

        bindings.resetToDefaults();

        assertEquals(KeyCode.P, bindings.keyFor(Action.PAUSE));
    }

    @Test
    @DisplayName("only the four move actions carry a direction")
    void onlyMovesCarryDirections() {
        int moves = 0;
        for (Action action : Action.values()) {
            if (action.isMovement()) {
                moves++;
                assertTrue(action.getDirection() != null);
            }
        }
        assertEquals(4, moves);
    }
}
