package com.example.prophunt.config;

import com.example.prophunt.PropHuntPlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Manages plugin configuration loading and access.
 */
public class ConfigManager {

    private final PropHuntPlugin plugin;
    private GameSettings defaultGameSettings;

    // General settings
    private boolean debug;
    private String language;

    public ConfigManager(PropHuntPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /**
     * Reloads all configuration values.
     */
    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // General
        debug = config.getBoolean("general.debug", false);
        language = config.getString("general.language", "en");

        // Load default game settings
        defaultGameSettings = loadGameSettings(config);

        plugin.debug("Configuration loaded successfully");
    }

    /**
     * Loads game settings from configuration.
     *
     * @param config the configuration
     * @return game settings
     */
    private GameSettings loadGameSettings(FileConfiguration config) {
        GameSettings settings = new GameSettings();

        // Game settings
        settings.setMinPlayers(config.getInt("game.min-players", 6));
        settings.setMaxPlayers(config.getInt("game.max-players", 20));
        settings.setLobbyCountdown(config.getInt("game.lobby-countdown", 30));
        settings.setHideTime(config.getInt("game.hide-time", 30));
        settings.setSeekTime(config.getInt("game.seek-time", 300));
        settings.setPropPercentage(config.getDouble("game.prop-percentage", 0.6));

        // Team settings - Props
        settings.setPropHealth(config.getInt("teams.props.health", 20));
        settings.setCanLock(config.getBoolean("teams.props.can-lock", true));

        // Team settings - Hunters
        settings.setHunterHealth(config.getInt("teams.hunters.health", 20));
        settings.setMissPenalty(config.getDouble("teams.hunters.miss-penalty", 2.0));
        settings.setAttackCooldown(config.getInt("teams.hunters.attack-cooldown", 20));

        // Taunt settings
        settings.setForcedTauntInterval(config.getInt("taunts.forced-interval", 45));
        settings.setVoluntaryTauntCooldown(config.getInt("taunts.voluntary-cooldown", 30));
        settings.setVoluntaryTauntPoints(config.getInt("taunts.voluntary-points", 50));

        // Scoring
        settings.setPropSurvivalPointsPerMinute(config.getInt("scoring.prop-survival-per-minute", 10));
        settings.setPropWinBonus(config.getInt("scoring.prop-win-bonus", 100));
        settings.setHunterKillPoints(config.getInt("scoring.hunter-kill", 50));
        settings.setHunterWinBonus(config.getInt("scoring.hunter-win-bonus", 100));

        return settings;
    }

    /**
     * Gets the default game settings.
     *
     * @return default settings
     */
    public GameSettings getDefaultGameSettings() {
        return defaultGameSettings.copy();
    }

    /**
     * Checks if debug mode is enabled.
     *
     * @return true if debug enabled
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Gets the language setting.
     *
     * @return language code
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Gets the storage type.
     *
     * @return storage type string
     */
    public String getStorageType() {
        return plugin.getConfig().getString("storage.type", "yaml");
    }

    /**
     * Gets a configuration value.
     *
     * @param path the config path
     * @param defaultValue the default value
     * @return the value
     */
    public int getInt(String path, int defaultValue) {
        return plugin.getConfig().getInt(path, defaultValue);
    }

    /**
     * Gets a configuration value.
     *
     * @param path the config path
     * @param defaultValue the default value
     * @return the value
     */
    public double getDouble(String path, double defaultValue) {
        return plugin.getConfig().getDouble(path, defaultValue);
    }

    /**
     * Gets a configuration value.
     *
     * @param path the config path
     * @param defaultValue the default value
     * @return the value
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return plugin.getConfig().getBoolean(path, defaultValue);
    }

    /**
     * Gets a configuration value.
     *
     * @param path the config path
     * @param defaultValue the default value
     * @return the value
     */
    public String getString(String path, String defaultValue) {
        return plugin.getConfig().getString(path, defaultValue);
    }
}
