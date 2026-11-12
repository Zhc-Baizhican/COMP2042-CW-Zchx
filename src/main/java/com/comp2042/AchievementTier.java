package com.comp2042;

/**
 * How rare a badge is. The tier decides its colour on the badge screen and in
 * the pop-up shown when it is earned: the higher the tier, the brighter.
 */
public enum AchievementTier {

    COMMON("Common"),
    RARE("Rare"),
    EPIC("Epic"),
    LEGENDARY("Legendary");

    private final String label;

    AchievementTier(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * @return the style class that colours this tier
     */
    public String getStyleClass() {
        return "tier" + name().charAt(0) + name().substring(1).toLowerCase();
    }
}
