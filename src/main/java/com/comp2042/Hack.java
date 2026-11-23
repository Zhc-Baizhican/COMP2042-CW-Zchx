package com.comp2042;

/**
 * Hack entries the player can switch on independently.
 *
 * <p>Each entry changes one rule of the game. They are meant to be combined
 * freely, so every effect is applied where the rule lives rather than in one
 * central switch.</p>
 */
public enum Hack {

    OFF_BOARD("Ghost board",
            "The snake can leave the board and coins may spawn in the transparent area around it."),

    WIN("Win",
            "The run is won as soon as it starts: press any key and the game is over in your favour."),

    MIDAS("Midas",
            "Every coin is a gold coin, worth fifteen points instead of five."),

    NO_GROWTH("No growth",
            "Eating never makes the snake longer, so the board can never fill."),

    MIRROR("Mirror keys",
            "The left and right keys are swapped. Good luck."),

    TURBO("Turbo",
            "The snake moves at more than double speed."),

    SLOW_MOTION("Slow motion",
            "Everything crawls along at less than half speed."),

    BLIND("Blind",
            "The board is swallowed by darkness; only the head and the coin stay lit.");

    private final String displayName;
    private final String description;

    Hack(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @param hack    the entry to test
     * @return {@code true} when that entry is switched on
     */
    public static boolean isOn(Hack hack) {
        return Hacks.get().isEnabled(hack);
    }
}
