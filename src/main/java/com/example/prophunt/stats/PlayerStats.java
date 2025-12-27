package com.example.prophunt.stats;

import java.util.UUID;

/**
 * Stores lifetime statistics for a player.
 */
public class PlayerStats {

    private final UUID uuid;
    private String lastKnownName;

    // Game stats
    private int gamesPlayed;
    private int gamesWon;
    private int gamesLost;

    // Prop stats
    private int timesAsProp;
    private int propSurvives;
    private int propDeaths;
    private long totalTimeAsHiddenProp; // seconds
    private int successfulTaunts;

    // Hunter stats
    private int timesAsHunter;
    private int propsFound;
    private int propsKilled;
    private int wrongHits;
    private int hunterDeaths;

    // Points
    private long totalPoints;
    private int highestGamePoints;

    // Time tracking
    private long totalPlayTime; // seconds
    private long firstPlayed;
    private long lastPlayed;

    public PlayerStats(UUID uuid) {
        this.uuid = uuid;
        this.firstPlayed = System.currentTimeMillis();
        this.lastPlayed = System.currentTimeMillis();
    }

    public PlayerStats(UUID uuid, String lastKnownName) {
        this(uuid);
        this.lastKnownName = lastKnownName;
    }

    // Getters
    public UUID getUuid() {
        return uuid;
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public int getGamesLost() {
        return gamesLost;
    }

    public int getTimesAsProp() {
        return timesAsProp;
    }

    public int getPropSurvives() {
        return propSurvives;
    }

    public int getPropDeaths() {
        return propDeaths;
    }

    public long getTotalTimeAsHiddenProp() {
        return totalTimeAsHiddenProp;
    }

    public int getSuccessfulTaunts() {
        return successfulTaunts;
    }

    public int getTimesAsHunter() {
        return timesAsHunter;
    }

    public int getPropsFound() {
        return propsFound;
    }

    public int getPropsKilled() {
        return propsKilled;
    }

    public int getWrongHits() {
        return wrongHits;
    }

    public int getHunterDeaths() {
        return hunterDeaths;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public int getHighestGamePoints() {
        return highestGamePoints;
    }

    public long getTotalPlayTime() {
        return totalPlayTime;
    }

    public long getFirstPlayed() {
        return firstPlayed;
    }

    public long getLastPlayed() {
        return lastPlayed;
    }

    // Setters for loading from database
    public void setLastKnownName(String name) {
        this.lastKnownName = name;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public void setGamesLost(int gamesLost) {
        this.gamesLost = gamesLost;
    }

    public void setTimesAsProp(int timesAsProp) {
        this.timesAsProp = timesAsProp;
    }

    public void setPropSurvives(int propSurvives) {
        this.propSurvives = propSurvives;
    }

    public void setPropDeaths(int propDeaths) {
        this.propDeaths = propDeaths;
    }

    public void setTotalTimeAsHiddenProp(long totalTimeAsHiddenProp) {
        this.totalTimeAsHiddenProp = totalTimeAsHiddenProp;
    }

    public void setSuccessfulTaunts(int successfulTaunts) {
        this.successfulTaunts = successfulTaunts;
    }

    public void setTimesAsHunter(int timesAsHunter) {
        this.timesAsHunter = timesAsHunter;
    }

    public void setPropsFound(int propsFound) {
        this.propsFound = propsFound;
    }

    public void setPropsKilled(int propsKilled) {
        this.propsKilled = propsKilled;
    }

    public void setWrongHits(int wrongHits) {
        this.wrongHits = wrongHits;
    }

    public void setHunterDeaths(int hunterDeaths) {
        this.hunterDeaths = hunterDeaths;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void setHighestGamePoints(int highestGamePoints) {
        this.highestGamePoints = highestGamePoints;
    }

    public void setTotalPlayTime(long totalPlayTime) {
        this.totalPlayTime = totalPlayTime;
    }

    public void setFirstPlayed(long firstPlayed) {
        this.firstPlayed = firstPlayed;
    }

    public void setLastPlayed(long lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    // Increment methods for tracking
    public void incrementGamesPlayed() {
        this.gamesPlayed++;
        this.lastPlayed = System.currentTimeMillis();
    }

    public void incrementGamesWon() {
        this.gamesWon++;
    }

    public void incrementGamesLost() {
        this.gamesLost++;
    }

    public void incrementTimesAsProp() {
        this.timesAsProp++;
    }

    public void incrementPropSurvives() {
        this.propSurvives++;
    }

    public void incrementPropDeaths() {
        this.propDeaths++;
    }

    public void addTimeAsHiddenProp(long seconds) {
        this.totalTimeAsHiddenProp += seconds;
    }

    public void incrementSuccessfulTaunts() {
        this.successfulTaunts++;
    }

    public void incrementTimesAsHunter() {
        this.timesAsHunter++;
    }

    public void incrementPropsFound() {
        this.propsFound++;
    }

    public void incrementPropsKilled() {
        this.propsKilled++;
    }

    public void incrementWrongHits() {
        this.wrongHits++;
    }

    public void incrementHunterDeaths() {
        this.hunterDeaths++;
    }

    public void addPoints(int points) {
        this.totalPoints += points;
    }

    public void updateHighestGamePoints(int gamePoints) {
        if (gamePoints > this.highestGamePoints) {
            this.highestGamePoints = gamePoints;
        }
    }

    public void addPlayTime(long seconds) {
        this.totalPlayTime += seconds;
    }

    // Calculated stats
    public double getWinRate() {
        if (gamesPlayed == 0) return 0;
        return (double) gamesWon / gamesPlayed * 100;
    }

    public double getPropSurvivalRate() {
        if (timesAsProp == 0) return 0;
        return (double) propSurvives / timesAsProp * 100;
    }

    public double getHunterKillRate() {
        if (propsFound == 0) return 0;
        return (double) propsKilled / propsFound * 100;
    }

    public double getHunterAccuracy() {
        int totalHits = propsFound + wrongHits;
        if (totalHits == 0) return 0;
        return (double) propsFound / totalHits * 100;
    }

    public double getKDR() {
        int deaths = propDeaths + hunterDeaths;
        if (deaths == 0) return propsKilled;
        return (double) propsKilled / deaths;
    }

    public double getAveragePointsPerGame() {
        if (gamesPlayed == 0) return 0;
        return (double) totalPoints / gamesPlayed;
    }
}
