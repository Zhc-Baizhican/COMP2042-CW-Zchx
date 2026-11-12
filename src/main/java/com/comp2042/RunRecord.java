package com.comp2042;

/**
 * One finished run, as shown on the history screen.
 *
 * <p>Records are immutable and written to a tab-separated line, so the history
 * survives a restart without needing a database or a JSON library.</p>
 */
public class RunRecord {

    private final String timestamp;
    private final String mode;
    private final boolean won;
    private final int score;
    private final int length;
    private final String hacks;
    private final String imageName;
    private final String duration;

    /**
     * @param timestamp when the run ended, already formatted for display
     * @param mode      difficulty name, or "Endless"
     * @param won       {@code true} when the run was won
     * @param score     points collected
     * @param length    snake length in cells
     * @param hacks     enabled hacks, comma separated; empty when none
     * @param imageName file name of the captured board, or empty
     * @param duration  run time as {@code mm:ss}
     */
    public RunRecord(String timestamp, String mode, boolean won, int score,
                     int length, String hacks, String imageName, String duration) {
        this.timestamp = timestamp;
        this.mode = mode;
        this.won = won;
        this.score = score;
        this.length = length;
        this.hacks = hacks;
        this.imageName = imageName;
        this.duration = duration;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMode() {
        return mode;
    }

    public boolean isWon() {
        return won;
    }

    public int getScore() {
        return score;
    }

    public int getLength() {
        return length;
    }

    public String getHacks() {
        return hacks;
    }

    public String getImageName() {
        return imageName;
    }

    /**
     * @return the run time as {@code mm:ss}
     */
    public String getDuration() {
        return duration == null || duration.isBlank() ? "00:00" : duration;
    }

    /**
     * @return the one-line summary shown above the details
     */
    public String getResult() {
        return won ? "Won" : "Game over";
    }

    /**
     * @return true when at least one hack was on for this run
     */
    public boolean hadHacks() {
        return hacks != null && !hacks.isBlank();
    }
}
