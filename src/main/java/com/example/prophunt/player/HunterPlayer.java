package com.example.prophunt.player;

import com.example.prophunt.game.Game;
import com.example.prophunt.team.Team;
import org.bukkit.entity.Player;

/**
 * Represents a player on the Hunters team.
 */
public class HunterPlayer extends GamePlayer {

    // Combat state
    private long lastAttackTime;
    private int attackCooldownTicks;

    // Stats
    private int propsFound;
    private int propsKilled;
    private int wrongHits;
    private boolean hasFirstBlood;

    /**
     * Creates a new HunterPlayer.
     *
     * @param player the Bukkit player
     * @param game the game
     */
    public HunterPlayer(Player player, Game game) {
        super(player, game);
        this.team = Team.HUNTERS;
        this.lastAttackTime = 0;
        this.attackCooldownTicks = 20; // 1 second default
        this.propsFound = 0;
        this.propsKilled = 0;
        this.wrongHits = 0;
        this.hasFirstBlood = false;
    }

    /**
     * Creates a HunterPlayer from an existing GamePlayer.
     *
     * @param gamePlayer the base GamePlayer
     * @return new HunterPlayer
     */
    public static HunterPlayer fromGamePlayer(GamePlayer gamePlayer) {
        HunterPlayer hunter = new HunterPlayer(gamePlayer.getPlayer(), gamePlayer.getGame());
        hunter.points = gamePlayer.points;
        hunter.gameJoinTime = gamePlayer.gameJoinTime;
        return hunter;
    }

    // ===== Combat Methods =====

    /**
     * Checks if the hunter can attack (cooldown finished).
     *
     * @return true if can attack
     */
    public boolean canAttack() {
        if (lastAttackTime == 0) return true;
        long elapsed = System.currentTimeMillis() - lastAttackTime;
        return elapsed >= attackCooldownTicks * 50L; // Ticks to ms
    }

    /**
     * Gets remaining attack cooldown in ticks.
     *
     * @return remaining ticks
     */
    public int getAttackCooldownRemaining() {
        if (lastAttackTime == 0) return 0;
        long elapsed = System.currentTimeMillis() - lastAttackTime;
        int remainingMs = attackCooldownTicks * 50 - (int) elapsed;
        return Math.max(0, remainingMs / 50);
    }

    /**
     * Records an attack (starts cooldown).
     */
    public void recordAttack() {
        this.lastAttackTime = System.currentTimeMillis();
    }

    /**
     * Sets the attack cooldown.
     *
     * @param ticks cooldown in ticks
     */
    public void setAttackCooldown(int ticks) {
        this.attackCooldownTicks = ticks;
    }

    // ===== Hit Recording =====

    /**
     * Records finding a prop (first hit on a prop).
     */
    public void recordPropFound() {
        this.propsFound++;
    }

    /**
     * Records killing a prop.
     */
    public void recordPropKill() {
        this.propsKilled++;
    }

    /**
     * Records a wrong hit (hitting a real block).
     */
    public void recordWrongHit() {
        this.wrongHits++;
    }

    /**
     * Sets first blood status.
     */
    public void setFirstBlood() {
        this.hasFirstBlood = true;
    }

    // ===== Stats =====

    /**
     * Gets the number of props found.
     *
     * @return props found count
     */
    public int getPropsFound() {
        return propsFound;
    }

    /**
     * Gets the number of props killed.
     *
     * @return props killed count
     */
    public int getPropsKilled() {
        return propsKilled;
    }

    /**
     * Gets the number of wrong hits.
     *
     * @return wrong hits count
     */
    public int getWrongHits() {
        return wrongHits;
    }

    /**
     * Checks if this hunter got first blood.
     *
     * @return true if first blood
     */
    public boolean hasFirstBlood() {
        return hasFirstBlood;
    }

    /**
     * Calculates hit accuracy.
     *
     * @return accuracy percentage (0-100)
     */
    public double getAccuracy() {
        int totalHits = propsFound + wrongHits;
        if (totalHits == 0) return 0;
        return (propsFound * 100.0) / totalHits;
    }

    @Override
    public String toString() {
        return "HunterPlayer{" +
                "name=" + getName() +
                ", propsFound=" + propsFound +
                ", propsKilled=" + propsKilled +
                ", wrongHits=" + wrongHits +
                ", points=" + points +
                '}';
    }
}
