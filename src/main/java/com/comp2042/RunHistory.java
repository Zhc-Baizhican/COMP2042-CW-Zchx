package com.comp2042;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * The list of finished runs, persisted next to the settings.
 *
 * <p>One tab-separated line per run plus one PNG per run in a {@code shots}
 * folder. The list is capped, because an unbounded history would keep growing
 * the folder forever.</p>
 */
public final class RunHistory {

    /** Runs kept on disk; older ones are dropped together with their picture. */
    private static final int MAX_ENTRIES = 50;

    private static final String FILE_NAME = "history.tsv";
    private static final String SHOTS_FOLDER = "shots";

    private static final RunHistory INSTANCE = new RunHistory(defaultDirectory());

    private final Path directory;
    private final Path indexFile;
    private final Path shotsDirectory;
    private final List<RunRecord> records = new ArrayList<>();

    private RunHistory(Path directory) {
        this.directory = directory;
        this.indexFile = directory.resolve(FILE_NAME);
        this.shotsDirectory = directory.resolve(SHOTS_FOLDER);
        load();
    }

    /**
     * Builds a history backed by a specific folder, for tests.
     *
     * @param directory folder holding the index file and the screenshots
     * @return a history bound to that folder
     */
    public static RunHistory forDirectory(Path directory) {
        return new RunHistory(directory);
    }

    public static RunHistory get() {
        return INSTANCE;
    }

    private static Path defaultDirectory() {
        return Paths.get(System.getProperty("user.home"), ".comp2042", "history");
    }

    /**
     * @return the folder the screenshots are written to, created on demand
     */
    public Path getShotsDirectory() {
        try {
            Files.createDirectories(shotsDirectory);
        } catch (IOException e) {
            System.err.println("Could not create the screenshot folder: " + e.getMessage());
        }
        return shotsDirectory;
    }

    /**
     * @return the stored runs, newest first
     */
    public List<RunRecord> getRecords() {
        return List.copyOf(records);
    }

    /**
     * Adds a run at the front of the list and persists it.
     *
     * @param record the finished run
     */
    public void add(RunRecord record) {
        records.add(0, record);
        trim();
        save();
    }

    /** Removes every record and its screenshot. */
    public void clear() {
        records.clear();
        save();
        try (Stream<Path> files = Files.list(shotsDirectory)) {
            files.forEach(file -> {
                try {
                    Files.deleteIfExists(file);
                } catch (IOException ignored) {
                    // A locked screenshot is not worth failing the clear over.
                }
            });
        } catch (IOException e) {
            System.err.println("Could not clear the screenshot folder: " + e.getMessage());
        }
    }

    private void trim() {
        while (records.size() > MAX_ENTRIES) {
            RunRecord dropped = records.remove(records.size() - 1);
            deleteShot(dropped);
        }
    }

    private void deleteShot(RunRecord record) {
        if (record.getImageName() == null || record.getImageName().isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(shotsDirectory.resolve(record.getImageName()));
        } catch (IOException ignored) {
            // The index is what matters; a leftover picture is harmless.
        }
    }

    private void load() {
        if (!Files.exists(indexFile)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(indexFile, StandardCharsets.UTF_8)) {
                RunRecord record = parse(line);
                if (record != null) {
                    records.add(record);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read the run history: " + e.getMessage());
        }
    }

    private void save() {
        try {
            Files.createDirectories(directory);
            List<String> lines = new ArrayList<>();
            for (RunRecord record : records) {
                lines.add(serialise(record));
            }
            Files.write(indexFile, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Could not write the run history: " + e.getMessage());
        }
    }

    private static String serialise(RunRecord record) {
        return String.join("\t",
                record.getTimestamp(),
                record.getMode(),
                Boolean.toString(record.isWon()),
                Integer.toString(record.getScore()),
                Integer.toString(record.getLength()),
                record.getHacks() == null ? "" : record.getHacks(),
                record.getImageName() == null ? "" : record.getImageName(),
                record.getDuration());
    }

    private static RunRecord parse(String line) {
        String[] parts = line.split("\t", -1);
        if (parts.length < 8) {
            return null;
        }
        try {
            return new RunRecord(parts[0], parts[1], Boolean.parseBoolean(parts[2]),
                    Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[5], parts[6], parts[7]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
