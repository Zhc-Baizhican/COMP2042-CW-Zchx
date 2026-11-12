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
            "The run is won as soon as it starts: press any key and the game is over in your favour.");

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
}
