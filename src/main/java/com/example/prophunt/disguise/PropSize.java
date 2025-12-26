package com.example.prophunt.disguise;

/**
 * Represents the size categories for props.
 * Size affects health and visibility.
 */
public enum PropSize {

    /**
     * Small props - low health but hard to spot.
     * Examples: buttons, flowers, torches, candles
     */
    SMALL("Small", 8, 4),

    /**
     * Medium props - balanced health and visibility.
     * Examples: flower pots, lanterns, skulls, cakes
     */
    MEDIUM("Medium", 14, 7),

    /**
     * Large props - high health but easy to spot.
     * Examples: barrels, chests, furnaces, cauldrons
     */
    LARGE("Large", 20, 10);

    private final String displayName;
    private final int health;
    private final int hearts;

    PropSize(String displayName, int health, int hearts) {
        this.displayName = displayName;
        this.health = health;
        this.hearts = hearts;
    }

    /**
     * Gets the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the health points for this size.
     *
     * @return health in half-hearts
     */
    public int getHealth() {
        return health;
    }

    /**
     * Gets the heart count for this size.
     *
     * @return number of hearts
     */
    public int getHearts() {
        return hearts;
    }

    /**
     * Gets the health description.
     *
     * @return formatted health string
     */
    public String getHealthDescription() {
        return hearts + " hearts";
    }
}
