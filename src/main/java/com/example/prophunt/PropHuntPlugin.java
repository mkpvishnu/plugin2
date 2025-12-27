package com.example.prophunt;

import com.example.prophunt.api.PropHuntAPI;
import com.example.prophunt.arena.ArenaManager;
import com.example.prophunt.commands.PropHuntCommand;
import com.example.prophunt.config.ConfigManager;
import com.example.prophunt.disguise.DisguiseManager;
import com.example.prophunt.game.GameManager;
import com.example.prophunt.gui.PropSelectorGUI;
import com.example.prophunt.listeners.*;
import com.example.prophunt.mechanics.LateGameManager;
import com.example.prophunt.mechanics.TauntManager;
import com.example.prophunt.player.PlayerManager;
import com.example.prophunt.stats.LeaderboardManager;
import com.example.prophunt.stats.StatsManager;
import com.example.prophunt.util.MessageUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/**
 * Main plugin class for PropHunt - a hide-and-seek minigame
 * where props disguise as blocks using BlockDisplay entities.
 */
public class PropHuntPlugin extends JavaPlugin {

    private static PropHuntPlugin instance;

    // Core Managers
    private ConfigManager configManager;
    private MessageUtil messageUtil;
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private PlayerManager playerManager;
    private DisguiseManager disguiseManager;

    // Stats & Scoring
    private StatsManager statsManager;
    private LeaderboardManager leaderboardManager;

    // Mechanics
    private TauntManager tauntManager;
    private LateGameManager lateGameManager;

    // GUI
    private PropSelectorGUI propSelectorGUI;

    // API
    private PropHuntAPI api;

    @Override
    public void onEnable() {
        instance = this;

        // Create plugin folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Save default configuration files
        saveDefaultConfig();
        saveResourceIfNotExists("messages.yml");

        // Initialize managers
        initializeManagers();

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        getLogger().info("PropHunt has been enabled!");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("Loaded " + arenaManager.getCount() + " arenas");
        debug("Debug mode is enabled");
    }

    @Override
    public void onDisable() {
        // Shutdown mechanics managers first
        if (tauntManager != null) {
            tauntManager.shutdown();
        }
        if (lateGameManager != null) {
            lateGameManager.shutdown();
        }

        // Shutdown disguise manager
        if (disguiseManager != null) {
            disguiseManager.shutdown();
        }

        // End all running games gracefully
        if (gameManager != null) {
            gameManager.endAllGames();
        }

        // Save all data
        if (arenaManager != null) {
            arenaManager.saveAll();
        }

        // Save and close stats database
        if (statsManager != null) {
            statsManager.shutdown();
        }

        getLogger().info("PropHunt has been disabled!");
        instance = null;
    }

    /**
     * Initializes all manager instances.
     */
    private void initializeManagers() {
        // Core managers
        configManager = new ConfigManager(this);
        messageUtil = new MessageUtil(this);
        arenaManager = new ArenaManager(this);
        playerManager = new PlayerManager(this);
        gameManager = new GameManager(this);
        disguiseManager = new DisguiseManager(this);

        // Stats managers
        statsManager = new StatsManager(this);
        statsManager.initialize();
        leaderboardManager = new LeaderboardManager(this, statsManager);

        // Mechanics managers
        tauntManager = new TauntManager(this);
        lateGameManager = new LateGameManager(this);

        // GUI
        propSelectorGUI = new PropSelectorGUI(this);

        // API
        api = new PropHuntAPI(this);

        debug("All managers initialized");
    }

    /**
     * Registers commands.
     */
    private void registerCommands() {
        PropHuntCommand mainCommand = new PropHuntCommand(this);

        PluginCommand cmd = getCommand("prophunt");
        if (cmd != null) {
            cmd.setExecutor(mainCommand);
            cmd.setTabCompleter(mainCommand);
        }

        debug("Commands registered");
    }

    /**
     * Registers event listeners.
     */
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new MovementListener(this), this);
        getServer().getPluginManager().registerEvents(new GameProtectionListener(this), this);

        debug("Event listeners registered");
    }

    /**
     * Saves a resource if it doesn't already exist.
     *
     * @param resourceName the resource name
     */
    private void saveResourceIfNotExists(String resourceName) {
        File file = new File(getDataFolder(), resourceName);
        if (!file.exists()) {
            saveResource(resourceName, false);
        }
    }

    /**
     * Reloads the plugin configuration.
     */
    public void reload() {
        configManager.reload();
        messageUtil.loadMessages();
        arenaManager.reload();
        getLogger().info("Configuration reloaded");
    }

    // ===== Getters =====

    public static PropHuntPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public DisguiseManager getDisguiseManager() {
        return disguiseManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }

    public TauntManager getTauntManager() {
        return tauntManager;
    }

    public LateGameManager getLateGameManager() {
        return lateGameManager;
    }

    public PropSelectorGUI getPropSelectorGUI() {
        return propSelectorGUI;
    }

    public PropHuntAPI getAPI() {
        return api;
    }

    /**
     * Logs a debug message if debug mode is enabled.
     */
    public void debug(String message) {
        if (configManager != null && configManager.isDebug()) {
            getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }

    /**
     * Logs a debug message with formatting if debug mode is enabled.
     */
    public void debug(String message, Object... args) {
        if (configManager != null && configManager.isDebug()) {
            getLogger().log(Level.INFO, "[DEBUG] " + String.format(message, args));
        }
    }
}
