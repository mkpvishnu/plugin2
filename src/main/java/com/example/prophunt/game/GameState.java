package com.example.prophunt.game;

/**
 * Represents the different states a PropHunt game can be in.
 */
public enum GameState {

    /**
     * Arena is disabled and not accepting players.
     */
    DISABLED("Disabled", false),

    /**
     * Waiting for minimum players to join.
     */
    WAITING("Waiting", true),

    /**
     * Countdown before game starts, teams being assigned.
     */
    STARTING("Starting", false),

    /**
     * Props are hiding, hunters are blinded/caged.
     */
    HIDING("Hiding", false),

    /**
     * Main gameplay - hunters seeking props.
     */
    HUNTING("Hunting", false),

    /**
     * Game ended, showing results.
     */
    ENDING("Ending", false);

    private final String displayName;
    private final boolean joinable;

    GameState(String displayName, boolean joinable) {
        this.displayName = displayName;
        this.joinable = joinable;
    }

    /**
     * Gets the display name for this state.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Checks if players can join during this state.
     *
     * @return true if joinable
     */
    public boolean isJoinable() {
        return joinable;
    }

    /**
     * Checks if the game is currently in progress.
     *
     * @return true if game is active (HIDING or HUNTING)
     */
    public boolean isInProgress() {
        return this == HIDING || this == HUNTING;
    }

    /**
     * Checks if the game is in a playable state.
     *
     * @return true if not DISABLED
     */
    public boolean isEnabled() {
        return this != DISABLED;
    }
}
