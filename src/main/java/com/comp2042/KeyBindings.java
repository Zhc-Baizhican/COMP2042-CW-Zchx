package com.comp2042;

import javafx.scene.input.KeyCode;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * The current key for every {@link Action}.
 *
 * <p>One action owns a key at a time: binding a key that is already taken is
 * refused by the settings screen, so a single key press can never trigger two
 * different things.</p>
 */
public final class KeyBindings {

    private static final KeyBindings INSTANCE = new KeyBindings();

    private final EnumMap<Action, KeyCode> keys = new EnumMap<>(Action.class);

    private KeyBindings() {
        resetToDefaults();
    }

    public static KeyBindings get() {
        return INSTANCE;
    }

    /** Restores every action to the key it shipped with. */
    public void resetToDefaults() {
        for (Action action : Action.values()) {
            keys.put(action, action.getDefaultKey());
        }
    }

    /**
     * @param action the action to look up
     * @return the key currently bound, never {@code null}
     */
    public KeyCode keyFor(Action action) {
        return keys.get(action);
    }

    /**
     * Human-readable name of the key bound to an action, for use in on-screen
     * hints so that they follow a rebind.
     *
     * @param action the action to look up
     * @return the key name, for example {@code "Tab"}
     */
    public String labelFor(Action action) {
        return keyFor(action).getName();
    }

    /**
     * Binds a key to an action without any conflict check. Callers that face a
     * user must go through {@link #actionFor(KeyCode)} first.
     *
     * @param action the action to bind
     * @param key    the new key, ignored when {@code null}
     */
    public void bind(Action action, KeyCode key) {
        if (action != null && key != null) {
            keys.put(action, key);
        }
    }

    /**
     * @param key the key to look up
     * @return the action using that key, or {@code null} when it is unbound
     */
    public Action actionFor(KeyCode key) {
        if (key == null) {
            return null;
        }
        for (Map.Entry<Action, KeyCode> entry : keys.entrySet()) {
            if (entry.getValue() == key) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * @return a read-only view of the current bindings
     */
    public Map<Action, KeyCode> asMap() {
        return Collections.unmodifiableMap(keys);
    }
}
