package com.comp2042;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link RunHistory}: records survive a reload, newest first, and the
 * clear button really empties the list.
 */
class RunHistoryTest {

    @TempDir
    Path directory;

    @Test
    @DisplayName("a record survives writing and reading the file back")
    void recordsPersist() {
        RunHistory history = RunHistory.forDirectory(directory);
        history.add(new RunRecord("2026-01-01 10:00", "Normal", false, 42, 7,
                "Ghost board", "", "01:23"));

        RunHistory reloaded = RunHistory.forDirectory(directory);
        assertEquals(1, reloaded.getRecords().size());

        RunRecord record = reloaded.getRecords().get(0);
        assertEquals("2026-01-01 10:00", record.getTimestamp());
        assertEquals("Normal", record.getMode());
        assertFalse(record.isWon());
        assertEquals(42, record.getScore());
        assertEquals(7, record.getLength());
        assertEquals("Ghost board", record.getHacks());
        assertEquals("01:23", record.getDuration());
        assertTrue(record.hadHacks());
    }

    @Test
    @DisplayName("the newest run is listed first")
    void newestFirst() {
        RunHistory history = RunHistory.forDirectory(directory);
        history.add(new RunRecord("old", "Easy", false, 1, 3, "", "", "00:10"));
        history.add(new RunRecord("new", "Hard", true, 250, 20, "Win", "", "02:00"));

        assertEquals("new", history.getRecords().get(0).getTimestamp());
        assertEquals("old", history.getRecords().get(1).getTimestamp());
    }

    @Test
    @DisplayName("a run without hacks reports none")
    void noHacksIsReported() {
        RunRecord record = new RunRecord("2026-01-01 10:00", "Endless", true, 300, 40, "", "", "03:45");

        assertFalse(record.hadHacks());
        assertEquals("Won", record.getResult());
    }

    @Test
    @DisplayName("clear removes every record")
    void clearEmptiesTheList() {
        RunHistory history = RunHistory.forDirectory(directory);
        history.add(new RunRecord("a", "Normal", false, 1, 2, "", "", "00:05"));
        history.add(new RunRecord("b", "Normal", false, 2, 3, "", "", "00:06"));

        history.clear();

        assertEquals(0, history.getRecords().size());
        assertEquals(0, RunHistory.forDirectory(directory).getRecords().size());
    }
}
