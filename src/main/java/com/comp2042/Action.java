package com.comp2042;

import javafx.scene.input.KeyCode;

/**
 * Everything the player can do with the keyboard, together with its default
 * key.
 *
 * <p>The game used to compare {@link KeyCode} values directly in several
 * places, which made rebinding impossible. Input is now resolved to an
 * {@code Action} first, and the key that triggers it lives in
 * {@link KeyBindings}.</p>
 */
public enum Action {

    MOVE_UP("Move up", KeyCode.UP, Direction.UP),
    MOVE_DOWN("Move down", KeyCode.DOWN, Direction.DOWN),
    MOVE_LEFT("Move left", KeyCode.LEFT, Direction.LEFT),
    MOVE_RIGHT("Move right", KeyCode.RIGHT, Direction.RIGHT),

    PAUSE("Pause", KeyCode.P, null),
    RESTART("Restart", KeyCode.TAB, null),
    FULLSCREEN_ON("Full screen", KeyCode.PLUS, null),
    FULLSCREEN_OFF("Windowed", KeyCode.MINUS, null),
    BACK_TO_MENU("Back to menu", KeyCode.ESCAPE, null);

    private final String displayName;
    private final KeyCode defaultKey;
    private final Direction direction;

    Action(String displayName, KeyCode defaultKey, Direction direction) {
        this.displayName = displayName;
        this.defaultKey = defaultKey;
        this.direction = direction;
    }

    public String getDisplayName() {
        return displayName;
    }

    public KeyCode getDefaultKey() {
        return defaultKey;
    }

    public boolean isMovement() {
        return direction != null;
    }

    /**
     * @return the direction this action steers towards, or {@code null} for a
     *         non-movement action
     */
    public Direction getDirection() {
        return direction;
    }
}
