package com.example.prophunt.player;

import com.example.prophunt.disguise.PropType;
import com.example.prophunt.game.Game;
import com.example.prophunt.team.Team;
import org.bukkit.entity.Player;

/**
 * Represents a player on the Props team.
 */
public class PropPlayer extends GamePlayer {

    // Disguise state
    private PropType disguise;
    private boolean locked;
    private float rotation;

    // Game state
    private boolean revealed;
    private long revealedTime;
    private int propChangesRemaining;
    private long lastPropChange;
    private long lastTaunt;
    private long lastForcedTaunt;

    // Stats
    private int timesFound;
    private int timesEscaped;
    private int voluntaryTaunts;

    /**
     * Creates a new PropPlayer.
     *
     * @param player the Bukkit player
     * @param game the game
     */
    public PropPlayer(Player player, Game game) {
        super(player, game);
        this.team = Team.PROPS;
        this.locked = false;
        this.rotation = 0;
        this.revealed = false;
        this.propChangesRemaining = 3; // Configurable
        this.timesFound = 0;
        this.timesEscaped = 0;
        this.voluntaryTaunts = 0;
    }

    /**
     * Creates a PropPlayer from an existing GamePlayer.
     *
     * @param gamePlayer the base GamePlayer
     * @return new PropPlayer
     */
    public static PropPlayer fromGamePlayer(GamePlayer gamePlayer) {
        PropPlayer prop = new PropPlayer(gamePlayer.getPlayer(), gamePlayer.getGame());
        prop.points = gamePlayer.points;
        prop.gameJoinTime = gamePlayer.gameJoinTime;
        return prop;
    }

    // ===== Disguise Methods =====

    /**
     * Gets the current disguise.
     *
     * @return the prop type, or null if not disguised
     */
    public PropType getDisguise() {
        return disguise;
    }

    /**
     * Sets the disguise.
     *
     * @param disguise the prop type
     */
    public void setDisguise(PropType disguise) {
        this.disguise = disguise;
        this.lastPropChange = System.currentTimeMillis();
    }

    /**
     * Checks if the player is disguised.
     *
     * @return true if disguised
     */
    public boolean isDisguised() {
        return disguise != null;
    }

    /**
     * Checks if the player can change their prop.
     *
     * @param cooldownSeconds the cooldown in seconds
     * @return true if can change
     */
    public boolean canChangeProp(int cooldownSeconds) {
        if (propChangesRemaining <= 0) return false;
        if (lastPropChange == 0) return true;

        long elapsed = System.currentTimeMillis() - lastPropChange;
        return elapsed >= cooldownSeconds * 1000L;
    }

    /**
     * Gets the remaining cooldown for prop change.
     *
     * @param cooldownSeconds the cooldown in seconds
     * @return remaining seconds, or 0 if ready
     */
    public int getPropChangeCooldown(int cooldownSeconds) {
        if (lastPropChange == 0) return 0;

        long elapsed = System.currentTimeMillis() - lastPropChange;
        int remaining = cooldownSeconds - (int) (elapsed / 1000);
        return Math.max(0, remaining);
    }

    /**
     * Uses a prop change.
     */
    public void usePropChange() {
        propChangesRemaining--;
    }

    // ===== Lock State =====

    /**
     * Checks if the prop is locked in place.
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Sets the locked state.
     *
     * @param locked true to lock
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Toggles the locked state.
     *
     * @return the new locked state
     */
    public boolean toggleLock() {
        this.locked = !this.locked;
        return this.locked;
    }

    /**
     * Gets the prop rotation.
     *
     * @return rotation in degrees
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Sets the prop rotation.
     *
     * @param rotation rotation in degrees
     */
    public void setRotation(float rotation) {
        this.rotation = rotation % 360;
    }

    // ===== Reveal State =====

    /**
     * Checks if the prop is revealed (found by hunter).
     *
     * @return true if revealed
     */
    public boolean isRevealed() {
        return revealed;
    }

    /**
     * Reveals the prop (after being hit).
     */
    public void reveal() {
        this.revealed = true;
        this.revealedTime = System.currentTimeMillis();
        this.locked = false;
        this.timesFound++;
    }

    /**
     * Hides the prop again (escaped successfully).
     */
    public void hide() {
        this.revealed = false;
        this.timesEscaped++;
    }

    /**
     * Gets time since being revealed.
     *
     * @return milliseconds since reveal, or -1 if not revealed
     */
    public long getTimeSinceReveal() {
        if (!revealed) return -1;
        return System.currentTimeMillis() - revealedTime;
    }

    /**
     * Checks if the prop has been revealed for long enough to hide again.
     *
     * @param escapeTimeSeconds time needed to escape
     * @return true if can hide
     */
    public boolean canHideAgain(int escapeTimeSeconds) {
        if (!revealed) return false;
        return getTimeSinceReveal() >= escapeTimeSeconds * 1000L;
    }

    // ===== Taunt Methods =====

    /**
     * Checks if the player can voluntary taunt.
     *
     * @param cooldownSeconds the cooldown
     * @return true if can taunt
     */
    public boolean canVoluntaryTaunt(int cooldownSeconds) {
        if (lastTaunt == 0) return true;
        long elapsed = System.currentTimeMillis() - lastTaunt;
        return elapsed >= cooldownSeconds * 1000L;
    }

    /**
     * Records a voluntary taunt.
     */
    public void recordVoluntaryTaunt() {
        this.lastTaunt = System.currentTimeMillis();
        this.voluntaryTaunts++;
    }

    /**
     * Checks if forced taunt is due.
     *
     * @param intervalSeconds the interval
     * @return true if should taunt
     */
    public boolean isForcedTauntDue(int intervalSeconds) {
        if (lastForcedTaunt == 0) {
            // First taunt after initial delay
            return getTimeInGame() >= intervalSeconds * 1000L;
        }
        long elapsed = System.currentTimeMillis() - lastForcedTaunt;
        return elapsed >= intervalSeconds * 1000L;
    }

    /**
     * Records a forced taunt.
     */
    public void recordForcedTaunt() {
        this.lastForcedTaunt = System.currentTimeMillis();
    }

    // ===== Stats =====

    public int getPropChangesRemaining() {
        return propChangesRemaining;
    }

    public int getTimesFound() {
        return timesFound;
    }

    public int getTimesEscaped() {
        return timesEscaped;
    }

    public int getVoluntaryTaunts() {
        return voluntaryTaunts;
    }

    @Override
    public String toString() {
        return "PropPlayer{" +
                "name=" + getName() +
                ", disguise=" + (disguise != null ? disguise.getMaterial() : "none") +
                ", locked=" + locked +
                ", revealed=" + revealed +
                ", points=" + points +
                '}';
    }
}
