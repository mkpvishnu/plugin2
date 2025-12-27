package com.example.prophunt.config;

/**
 * Holds settings for a PropHunt game.
 * Can be customized per-arena or use defaults.
 */
public class GameSettings {

    // Player limits
    private int minPlayers = 6;
    private int maxPlayers = 20;

    // Timers (in seconds)
    private int lobbyCountdown = 30;
    private int hideTime = 30;
    private int seekTime = 300;

    // Team balance
    private double propPercentage = 0.6;

    // Prop settings
    private int propHealth = 20;
    private boolean canLock = true;

    // Hunter settings
    private int hunterHealth = 20;
    private double missPenalty = 2.0;
    private int attackCooldown = 20; // ticks

    // Taunt settings
    private int forcedTauntInterval = 45;
    private int voluntaryTauntCooldown = 30;
    private int voluntaryTauntPoints = 50;

    // Scoring
    private int propSurvivalPointsPerMinute = 10;
    private int propWinBonus = 100;
    private int hunterKillPoints = 50;
    private int hunterWinBonus = 100;

    /**
     * Creates a copy of these settings.
     *
     * @return a new GameSettings with the same values
     */
    public GameSettings copy() {
        GameSettings copy = new GameSettings();
        copy.minPlayers = this.minPlayers;
        copy.maxPlayers = this.maxPlayers;
        copy.lobbyCountdown = this.lobbyCountdown;
        copy.hideTime = this.hideTime;
        copy.seekTime = this.seekTime;
        copy.propPercentage = this.propPercentage;
        copy.propHealth = this.propHealth;
        copy.canLock = this.canLock;
        copy.hunterHealth = this.hunterHealth;
        copy.missPenalty = this.missPenalty;
        copy.attackCooldown = this.attackCooldown;
        copy.forcedTauntInterval = this.forcedTauntInterval;
        copy.voluntaryTauntCooldown = this.voluntaryTauntCooldown;
        copy.voluntaryTauntPoints = this.voluntaryTauntPoints;
        copy.propSurvivalPointsPerMinute = this.propSurvivalPointsPerMinute;
        copy.propWinBonus = this.propWinBonus;
        copy.hunterKillPoints = this.hunterKillPoints;
        copy.hunterWinBonus = this.hunterWinBonus;
        return copy;
    }

    /**
     * Calculates the number of props for a given player count.
     *
     * @param playerCount total players
     * @return number of props
     */
    public int calculatePropCount(int playerCount) {
        return (int) Math.round(playerCount * propPercentage);
    }

    /**
     * Calculates the number of hunters for a given player count.
     *
     * @param playerCount total players
     * @return number of hunters
     */
    public int calculateHunterCount(int playerCount) {
        return playerCount - calculatePropCount(playerCount);
    }

    // ===== Getters and Setters =====

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = Math.max(2, minPlayers);
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = Math.max(this.minPlayers, maxPlayers);
    }

    public int getLobbyCountdown() {
        return lobbyCountdown;
    }

    public void setLobbyCountdown(int lobbyCountdown) {
        this.lobbyCountdown = Math.max(5, lobbyCountdown);
    }

    public int getHideTime() {
        return hideTime;
    }

    public void setHideTime(int hideTime) {
        this.hideTime = Math.max(10, hideTime);
    }

    public int getSeekTime() {
        return seekTime;
    }

    public void setSeekTime(int seekTime) {
        this.seekTime = Math.max(60, seekTime);
    }

    public double getPropPercentage() {
        return propPercentage;
    }

    public void setPropPercentage(double propPercentage) {
        this.propPercentage = Math.max(0.3, Math.min(0.8, propPercentage));
    }

    public int getPropHealth() {
        return propHealth;
    }

    public void setPropHealth(int propHealth) {
        this.propHealth = Math.max(1, propHealth);
    }

    public boolean isCanLock() {
        return canLock;
    }

    public void setCanLock(boolean canLock) {
        this.canLock = canLock;
    }

    public int getHunterHealth() {
        return hunterHealth;
    }

    public void setHunterHealth(int hunterHealth) {
        this.hunterHealth = Math.max(1, hunterHealth);
    }

    public double getMissPenalty() {
        return missPenalty;
    }

    public void setMissPenalty(double missPenalty) {
        this.missPenalty = Math.max(0, missPenalty);
    }

    public int getAttackCooldown() {
        return attackCooldown;
    }

    public void setAttackCooldown(int attackCooldown) {
        this.attackCooldown = Math.max(0, attackCooldown);
    }

    public int getForcedTauntInterval() {
        return forcedTauntInterval;
    }

    public void setForcedTauntInterval(int forcedTauntInterval) {
        this.forcedTauntInterval = Math.max(10, forcedTauntInterval);
    }

    public int getVoluntaryTauntCooldown() {
        return voluntaryTauntCooldown;
    }

    public void setVoluntaryTauntCooldown(int voluntaryTauntCooldown) {
        this.voluntaryTauntCooldown = Math.max(5, voluntaryTauntCooldown);
    }

    public int getVoluntaryTauntPoints() {
        return voluntaryTauntPoints;
    }

    public void setVoluntaryTauntPoints(int voluntaryTauntPoints) {
        this.voluntaryTauntPoints = Math.max(0, voluntaryTauntPoints);
    }

    public int getPropSurvivalPointsPerMinute() {
        return propSurvivalPointsPerMinute;
    }

    public void setPropSurvivalPointsPerMinute(int points) {
        this.propSurvivalPointsPerMinute = Math.max(0, points);
    }

    public int getPropWinBonus() {
        return propWinBonus;
    }

    public void setPropWinBonus(int propWinBonus) {
        this.propWinBonus = Math.max(0, propWinBonus);
    }

    public int getHunterKillPoints() {
        return hunterKillPoints;
    }

    public void setHunterKillPoints(int hunterKillPoints) {
        this.hunterKillPoints = Math.max(0, hunterKillPoints);
    }

    public int getHunterWinBonus() {
        return hunterWinBonus;
    }

    public void setHunterWinBonus(int hunterWinBonus) {
        this.hunterWinBonus = Math.max(0, hunterWinBonus);
    }
}
