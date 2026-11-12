package com.comp2042;

import java.util.EnumSet;
import java.util.Set;

/**
 * Which {@link Hack} entries are switched on.
 *
 * <p>Kept separate from {@link Settings} because hacks are a gameplay mode
 * rather than a preference, but persisted in the same file so a chosen set
 * survives a restart.</p>
 */
public final class Hacks {

    private static final Hacks INSTANCE = new Hacks();

    private final Set<Hack> enabled = EnumSet.noneOf(Hack.class);

    private Hacks() {
    }

    public static Hacks get() {
        return INSTANCE;
    }

    /**
     * @param hack the entry to test
     * @return {@code true} when the hack is switched on
     */
    public boolean isEnabled(Hack hack) {
        return hack != null && enabled.contains(hack);
    }

    /**
     * @param hack the entry to change
     * @param on   the new state
     */
    public void setEnabled(Hack hack, boolean on) {
        if (hack == null) {
            return;
        }
        if (on) {
            enabled.add(hack);
        } else {
            enabled.remove(hack);
        }
    }

    /** Switches every hack off. */
    public void disableAll() {
        enabled.clear();
    }

    /**
     * @return how many hacks are currently on
     */
    public int enabledCount() {
        return enabled.size();
    }
}
