package com.comp2042;

/**
 * What a finished run looked like, used to decide which achievements unlock.
 *
 * <p>A plain value object: the achievements only read from it, which keeps them
 * testable without starting a game.</p>
 */
public class RunSummary {

    private final boolean won;
    private final String mode;
    private final int score;
    private final int length;
    private final long seconds;
    private final String hacks;
    private final long idleSeconds;

    /**
     * @param won     whether the run was won
     * @param mode    "Easy", "Normal", "Hard" or "Endless"
     * @param score   points collected
     * @param length  snake length in cells
     * @param seconds run time, not counting pauses
     * @param hacks   enabled hacks, comma separated; empty when none
     */
    public RunSummary(boolean won, String mode, int score, int length, long seconds, String hacks) {
        this(won, mode, score, length, seconds, hacks, 0);
    }

    /**
     * @param idleSeconds seconds spent on the start screen before pressing a key
     */
    public RunSummary(boolean won, String mode, int score, int length,
                      long seconds, String hacks, long idleSeconds) {
        this.won = won;
        this.mode = mode;
        this.score = score;
        this.length = length;
        this.seconds = seconds;
        this.hacks = hacks == null ? "" : hacks;
        this.idleSeconds = idleSeconds;
    }

    public boolean isWon() {
        return won;
    }

    public String getMode() {
        return mode;
    }

    public int getScore() {
        return score;
    }

    public int getLength() {
        return length;
    }

    public long getSeconds() {
        return seconds;
    }

    public String getHacks() {
        return hacks;
    }

    /**
     * @return seconds spent on the start screen before the first key press
     */
    public long getIdleSeconds() {
        return idleSeconds;
    }

    /**
     * @return {@code true} when at least one hack was on
     */
    public boolean hadHacks() {
        return !hacks.isBlank();
    }

    /**
     * @param name a hack display name
     * @return {@code true} when that hack was on for this run
     */
    public boolean usedHack(String name) {
        return hacks.contains(name);
    }

    /**
     * @param modeName a mode name
     * @return {@code true} when the run was played on that mode
     */
    public boolean isMode(String modeName) {
        return modeName.equalsIgnoreCase(mode);
    }
}
