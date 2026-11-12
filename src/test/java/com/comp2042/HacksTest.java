package com.comp2042;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Hacks}: switching entries on and off and counting them.
 */
class HacksTest {

    @BeforeEach
    void reset() {
        Hacks.get().disableAll();
    }

    @Test
    @DisplayName("every hack starts switched off")
    void allStartDisabled() {
        for (Hack hack : Hack.values()) {
            assertFalse(Hacks.get().isEnabled(hack), hack + " should start off");
        }
        assertEquals(0, Hacks.get().enabledCount());
    }

    @Test
    @DisplayName("a hack can be switched on and off again")
    void togglingWorks() {
        Hacks.get().setEnabled(Hack.OFF_BOARD, true);
        assertTrue(Hacks.get().isEnabled(Hack.OFF_BOARD));
        assertEquals(1, Hacks.get().enabledCount());

        Hacks.get().setEnabled(Hack.OFF_BOARD, false);
        assertFalse(Hacks.get().isEnabled(Hack.OFF_BOARD));
        assertEquals(0, Hacks.get().enabledCount());
    }

    @Test
    @DisplayName("a null hack is ignored instead of throwing")
    void nullIsIgnored() {
        assertFalse(Hacks.get().isEnabled(null));
        Hacks.get().setEnabled(null, true);
        assertEquals(0, Hacks.get().enabledCount());
    }

    @Test
    @DisplayName("hacks combine freely")
    void hacksCombine() {
        for (Hack hack : Hack.values()) {
            Hacks.get().setEnabled(hack, true);
        }

        assertEquals(Hack.values().length, Hacks.get().enabledCount());
    }
}
