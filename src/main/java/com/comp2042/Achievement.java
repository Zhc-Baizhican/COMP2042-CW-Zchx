package com.comp2042;

import java.util.function.Predicate;

/**
 * Every badge the player can earn.
 *
 * <p>Each entry carries its own rule, so adding one is a single enum constant
 * rather than another branch somewhere in the game loop. All rules read from a
 * {@link RunSummary}, which is why they can be tested without JavaFX.</p>
 */
public enum Achievement {

    FIRST_RUN("First Steps", "Finish your first run", AchievementTier.COMMON, run -> true),
    FIRST_WIN("Winner", "Win a run", AchievementTier.COMMON, RunSummary::isWon),
    EASY_WIN("Easy Rider", "Win on Easy", AchievementTier.RARE,
            run -> run.isWon() && run.isMode("Easy")),
    NORMAL_WIN("Perfectly Normal", "Win on Normal", AchievementTier.RARE,
            run -> run.isWon() && run.isMode("Normal")),
    HARD_WIN("Hardcore", "Win on Hard", AchievementTier.EPIC,
            run -> run.isWon() && run.isMode("Hard")),
    ENDLESS_WIN("Board Filler", "Win on Endless by filling the board", AchievementTier.EPIC,
            run -> run.isWon() && run.isMode("Endless")),
    SPEEDRUN("Speedrun", "Win in under a minute", AchievementTier.EPIC,
            run -> run.isWon() && run.getSeconds() < 60),
    MARATHON("Marathon", "Survive three minutes", AchievementTier.RARE,
            run -> run.getSeconds() >= 180),
    LONG_SNAKE("Long Boi", "Grow to 30 cells", AchievementTier.RARE, run -> run.getLength() >= 30),
    CENTURY("Century", "Score 100 in one run", AchievementTier.COMMON, run -> run.getScore() >= 100),
    QUICK_DEATH("Ouch", "Game over within five seconds", AchievementTier.COMMON,
            run -> !run.isWon() && run.getSeconds() <= 5),
    GHOST_WIN("Ghost", "Win with the ghost board on", AchievementTier.EPIC,
            run -> run.isWon() && run.usedHack("Ghost board")),
    HACK_WIN("With a Little Help", "Win with any hack on", AchievementTier.RARE,
            run -> run.isWon() && run.hadHacks()),
    CLEAN_WIN("Clean Run", "Win with no hacks on", AchievementTier.EPIC,
            run -> run.isWon() && !run.hadHacks()),

    /**
     * Hidden until earned: its name and condition are masked on the badge
     * screen, so the player has to stumble onto it. It is also the only
     * legendary badge, so it gets the brightest card.
     *
     * <p>An enum constant cannot read a static field declared below it, so the
     * fifteen second threshold is written into the rule itself.</p>
     */
    ARE_YOU_STILL_THERE("Are You Still There?",
            "Win the waiting game: leave the start screen alone for fifteen seconds with the Win hack on.",
            AchievementTier.LEGENDARY,
            true,
            run -> run.isWon() && run.usedHack("Win") && run.getIdleSeconds() >= 15);

    private static final String MASKED_TITLE = "???";
    private static final String MASKED_DESCRIPTION = "A hidden badge. Earn it to find out what it is.";

    private final String title;
    private final String description;
    private final AchievementTier tier;
    private final boolean secret;
    private final Predicate<RunSummary> rule;

    Achievement(String title, String description, AchievementTier tier, Predicate<RunSummary> rule) {
        this(title, description, tier, false, rule);
    }

    Achievement(String title, String description, AchievementTier tier, boolean secret,
                Predicate<RunSummary> rule) {
        this.title = title;
        this.description = description;
        this.tier = tier;
        this.secret = secret;
        this.rule = rule;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return how rare this badge is, which decides its colour
     */
    public AchievementTier getTier() {
        return tier;
    }

    /**
     * @return {@code true} when the badge stays hidden until it is earned
     */
    public boolean isSecret() {
        return secret;
    }

    /**
     * @param unlocked whether the player already earned this badge
     * @return the title, or a mask while a secret badge is still locked
     */
    public String getDisplayTitle(boolean unlocked) {
        return secret && !unlocked ? MASKED_TITLE : title;
    }

    /**
     * @param unlocked whether the player already earned this badge
     * @return the description, or a mask while a secret badge is still locked
     */
    public String getDisplayDescription(boolean unlocked) {
        return secret && !unlocked ? MASKED_DESCRIPTION : description;
    }

    /**
     * @param summary the finished run
     * @return {@code true} when this run satisfies the rule
     */
    public boolean isMetBy(RunSummary summary) {
        return summary != null && rule.test(summary);
    }
}
