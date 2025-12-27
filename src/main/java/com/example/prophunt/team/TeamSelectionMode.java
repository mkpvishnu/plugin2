package com.example.prophunt.team;

/**
 * Defines how teams are assigned in PropHunt games.
 */
public enum TeamSelectionMode {

    /**
     * Players choose their own team in the lobby.
     * No team limits - good for small/private servers.
     */
    CHOICE,

    /**
     * Teams are randomly assigned based on prop percentage ratio.
     * Good for large public servers to ensure balance.
     */
    RANDOM,

    /**
     * Players can choose, but if teams are unbalanced,
     * some players are auto-assigned to balance.
     */
    HYBRID;

    /**
     * Gets the mode from a string (case-insensitive).
     *
     * @param name the mode name
     * @return the mode, or RANDOM if invalid
     */
    public static TeamSelectionMode fromString(String name) {
        if (name == null) return RANDOM;
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RANDOM;
        }
    }
}
