package com.example.prophunt;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for PropHunt - a hide-and-seek minigame
 * where props disguise as blocks using BlockDisplay entities.
 */
public class PropHuntPlugin extends JavaPlugin {

    private static PropHuntPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        // Save default configuration files
        saveDefaultConfig();
        saveResource("messages.yml", false);

        getLogger().info("PropHunt has been enabled!");
        getLogger().info("Version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("PropHunt has been disabled!");
        instance = null;
    }

    /**
     * Gets the plugin instance.
     *
     * @return the plugin instance
     */
    public static PropHuntPlugin getInstance() {
        return instance;
    }

    /**
     * Logs a debug message if debug mode is enabled.
     *
     * @param message the message to log
     */
    public void debug(String message) {
        if (getConfig().getBoolean("general.debug", false)) {
            getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }
}
