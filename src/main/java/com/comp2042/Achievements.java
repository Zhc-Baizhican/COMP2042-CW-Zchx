package com.comp2042;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Which {@link Achievement}s have been unlocked.
 *
 * <p>Stored as one enum name per line next to the settings, so adding a new
 * achievement never invalidates an existing save: unknown names are ignored and
 * new ones simply start locked.</p>
 */
public final class Achievements {

    private static final String FILE_NAME = "achievements.txt";

    private static final Achievements INSTANCE =
            new Achievements(Paths.get(System.getProperty("user.home"), ".comp2042"));

    private final Path file;
    private final Set<Achievement> unlocked = EnumSet.noneOf(Achievement.class);

    private Achievements(Path directory) {
        this.file = directory.resolve(FILE_NAME);
        load();
    }

    /**
     * Builds a store backed by a specific folder, for tests.
     *
     * @param directory folder holding the achievements file
     * @return a store bound to that folder
     */
    public static Achievements forDirectory(Path directory) {
        return new Achievements(directory);
    }

    public static Achievements get() {
        return INSTANCE;
    }

    /**
     * @param achievement the badge to test
     * @return {@code true} when it is already unlocked
     */
    public boolean isUnlocked(Achievement achievement) {
        return unlocked.contains(achievement);
    }

    /**
     * @return how many badges are unlocked
     */
    public int unlockedCount() {
        return unlocked.size();
    }

    /**
     * @return every badge: unlocked first, then the visible locked ones, then the
     *         still-hidden secret ones
     */
    public List<Achievement> getAll() {
        List<Achievement> result = new ArrayList<>();
        for (Achievement achievement : Achievement.values()) {
            if (isUnlocked(achievement)) {
                result.add(achievement);
            }
        }
        for (Achievement achievement : Achievement.values()) {
            if (!isUnlocked(achievement) && !achievement.isSecret()) {
                result.add(achievement);
            }
        }
        for (Achievement achievement : Achievement.values()) {
            if (!isUnlocked(achievement) && achievement.isSecret()) {
                result.add(achievement);
            }
        }
        return result;
    }

    /**
     * Checks every badge against a finished run and unlocks the ones that match.
     *
     * @param summary the finished run
     * @return the badges unlocked by this run, empty when none are new
     */
    public List<Achievement> evaluate(RunSummary summary) {
        List<Achievement> fresh = new ArrayList<>();
        for (Achievement achievement : Achievement.values()) {
            if (!isUnlocked(achievement) && achievement.isMetBy(summary)) {
                unlocked.add(achievement);
                fresh.add(achievement);
            }
        }
        if (!fresh.isEmpty()) {
            save();
        }
        return fresh;
    }

    /** Forgets every unlocked badge. */
    public void clear() {
        unlocked.clear();
        save();
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String name = line.trim();
                if (name.isEmpty()) {
                    continue;
                }
                try {
                    unlocked.add(Achievement.valueOf(name));
                } catch (IllegalArgumentException ignored) {
                    // A badge removed in a later version; keep the rest.
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read the achievements: " + e.getMessage());
        }
    }

    private void save() {
        try {
            Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            for (Achievement achievement : unlocked) {
                lines.add(achievement.name());
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Could not write the achievements: " + e.getMessage());
        }
    }
}
