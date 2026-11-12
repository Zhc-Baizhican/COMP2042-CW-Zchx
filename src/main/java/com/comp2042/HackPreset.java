package com.comp2042;

/**
 * Ready-made combinations of {@link Hack} entries.
 *
 * <p>Applying a preset always starts from a clean slate: every hack is switched
 * off before the preset's own entries are switched on, so a combination can
 * never leak into the next one.</p>
 */
public enum HackPreset {

    CLEAR("Clear"),
    GHOST_BOARD("Ghost", Hack.OFF_BOARD),
    INSTANT_WIN("Win", Hack.WIN),
    EVERYTHING("All", Hack.values());

    private final String label;
    private final Hack[] hacks;

    HackPreset(String label, Hack... hacks) {
        this.label = label;
        this.hacks = hacks;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Switches every hack off, then switches this preset's entries on.
     *
     * @param target the store to update
     */
    public void applyTo(Hacks target) {
        target.disableAll();
        for (Hack hack : hacks) {
            target.setEnabled(hack, true);
        }
    }
}
