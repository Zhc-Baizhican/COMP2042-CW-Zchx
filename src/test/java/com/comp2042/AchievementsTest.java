package com.comp2042;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link Achievement} rules and the {@link Achievements} store.
 */
class AchievementsTest {

    private static RunSummary run(boolean won, String mode, int score, int length, long seconds, String hacks) {
        return new RunSummary(won, mode, score, length, seconds, hacks);
    }

    @TempDir
    Path directory;

    @Test
    @DisplayName("finishing a run unlocks First Steps")
    void firstRunUnlocks() {
        assertTrue(Achievement.FIRST_RUN.isMetBy(run(false, "Normal", 3, 5, 12, "")));
    }

    @Test
    @DisplayName("a win unlocks the winner badge and the mode badge")
    void winUnlocksModeBadge() {
        RunSummary easyWin = run(true, "Easy", 100, 20, 40, "");

        assertTrue(Achievement.FIRST_WIN.isMetBy(easyWin));
        assertTrue(Achievement.EASY_WIN.isMetBy(easyWin));
        assertFalse(Achievement.HARD_WIN.isMetBy(easyWin));
    }

    @Test
    @DisplayName("the endless badge needs a win on the endless mode")
    void endlessBadgeNeedsEndlessWin() {
        assertTrue(Achievement.ENDLESS_WIN.isMetBy(run(true, "Endless", 400, 400, 900, "")));
        assertFalse(Achievement.ENDLESS_WIN.isMetBy(run(true, "Normal", 200, 40, 120, "")));
    }

    @Test
    @DisplayName("speedrun and marathon depend on the clock")
    void timeBasedBadges() {
        assertTrue(Achievement.SPEEDRUN.isMetBy(run(true, "Hard", 250, 30, 59, "")));
        assertFalse(Achievement.SPEEDRUN.isMetBy(run(true, "Hard", 250, 30, 61, "")));
        assertTrue(Achievement.MARATHON.isMetBy(run(false, "Normal", 50, 20, 180, "")));
    }

    @Test
    @DisplayName("the hack badges tell clean runs from assisted ones")
    void hackBadges() {
        RunSummary clean = run(true, "Normal", 200, 30, 100, "");
        RunSummary assisted = run(true, "Normal", 200, 30, 100, "Ghost board");

        assertTrue(Achievement.CLEAN_WIN.isMetBy(clean));
        assertFalse(Achievement.HACK_WIN.isMetBy(clean));
        assertTrue(Achievement.HACK_WIN.isMetBy(assisted));
        assertTrue(Achievement.GHOST_WIN.isMetBy(assisted));
        assertFalse(Achievement.CLEAN_WIN.isMetBy(assisted));
    }

    @Test
    @DisplayName("an early death unlocks Ouch")
    void quickDeath() {
        assertTrue(Achievement.QUICK_DEATH.isMetBy(run(false, "Hard", 0, 3, 4, "")));
        assertFalse(Achievement.QUICK_DEATH.isMetBy(run(true, "Hard", 250, 30, 4, "")));
    }

    @Test
    @DisplayName("evaluating a run unlocks each badge only once")
    void unlockedOnlyOnce() {
        Achievements store = Achievements.forDirectory(directory);
        RunSummary summary = run(true, "Normal", 200, 30, 100, "");

        List<Achievement> first = store.evaluate(summary);
        List<Achievement> second = store.evaluate(summary);

        assertTrue(first.contains(Achievement.FIRST_RUN));
        assertTrue(first.contains(Achievement.FIRST_WIN));
        assertTrue(second.isEmpty(), "nothing should unlock twice");
    }

    @Test
    @DisplayName("unlocked badges survive a reload")
    void badgesPersist() {
        Achievements store = Achievements.forDirectory(directory);
        store.evaluate(run(true, "Hard", 250, 40, 90, "Ghost board"));

        Achievements reloaded = Achievements.forDirectory(directory);

        assertTrue(reloaded.isUnlocked(Achievement.HARD_WIN));
        assertTrue(reloaded.isUnlocked(Achievement.GHOST_WIN));
        assertEquals(store.unlockedCount(), reloaded.unlockedCount());
    }

    @Test
    @DisplayName("clear locks everything again")
    void clearLocksEverything() {
        Achievements store = Achievements.forDirectory(directory);
        store.evaluate(run(true, "Easy", 100, 20, 40, ""));

        store.clear();

        assertEquals(0, store.unlockedCount());
        assertFalse(store.isUnlocked(Achievement.FIRST_RUN));
    }

    @Test
    @DisplayName("the secret badge needs the Win hack and fifteen idle seconds")
    void secretBadgeNeedsBoth() {
        RunSummary quick = new RunSummary(true, "Normal", 200, 30, 1, "Win", 3);
        RunSummary slow = new RunSummary(true, "Normal", 200, 30, 1, "Win", 15);
        RunSummary noHack = new RunSummary(true, "Normal", 200, 30, 1, "", 20);

        assertFalse(Achievement.ARE_YOU_STILL_THERE.isMetBy(quick));
        assertTrue(Achievement.ARE_YOU_STILL_THERE.isMetBy(slow));
        assertFalse(Achievement.ARE_YOU_STILL_THERE.isMetBy(noHack));
    }

    @Test
    @DisplayName("a locked secret badge hides its name and condition")
    void secretBadgeIsMaskedWhileLocked() {
        Achievement secret = Achievement.ARE_YOU_STILL_THERE;

        assertTrue(secret.isSecret());
        assertEquals("???", secret.getDisplayTitle(false));
        assertEquals("Are You Still There?", secret.getDisplayTitle(true));
        assertFalse(secret.getDisplayDescription(false).contains("fifteen"));
        assertTrue(secret.getDisplayDescription(true).contains("fifteen"));
    }

    @Test
    @DisplayName("a normal badge is never masked")
    void normalBadgeIsAlwaysShown() {
        assertFalse(Achievement.FIRST_WIN.isSecret());
        assertEquals("Winner", Achievement.FIRST_WIN.getDisplayTitle(false));
    }

    @Test
    @DisplayName("locked secret badges are listed last")
    void secretBadgesComeLast() {
        Achievements store = Achievements.forDirectory(directory);
        List<Achievement> all = store.getAll();

        assertEquals(Achievement.ARE_YOU_STILL_THERE, all.get(all.size() - 1));
    }

    @Test
    @DisplayName("the secret badge is the only legendary one")
    void secretBadgeIsLegendary() {
        assertEquals(AchievementTier.LEGENDARY, Achievement.ARE_YOU_STILL_THERE.getTier());

        for (Achievement achievement : Achievement.values()) {
            if (achievement != Achievement.ARE_YOU_STILL_THERE) {
                assertFalse(achievement.getTier() == AchievementTier.LEGENDARY,
                        achievement + " should not be legendary");
            }
        }
    }

    @Test
    @DisplayName("every tier has its own style class")
    void everyTierHasAStyleClass() {
        assertEquals("tierCommon", AchievementTier.COMMON.getStyleClass());
        assertEquals("tierRare", AchievementTier.RARE.getStyleClass());
        assertEquals("tierEpic", AchievementTier.EPIC.getStyleClass());
        assertEquals("tierLegendary", AchievementTier.LEGENDARY.getStyleClass());

        for (Achievement achievement : Achievement.values()) {
            assertTrue(achievement.getTier() != null, achievement + " needs a tier");
        }
    }
}
