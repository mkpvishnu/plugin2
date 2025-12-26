package com.example.prophunt.stats;

import com.example.prophunt.PropHuntPlugin;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player statistics with SQLite persistence.
 */
public class StatsManager {

    private final PropHuntPlugin plugin;
    private final Map<UUID, PlayerStats> cache;
    private Connection connection;

    private static final String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS player_stats (
                uuid TEXT PRIMARY KEY,
                last_known_name TEXT,
                games_played INTEGER DEFAULT 0,
                games_won INTEGER DEFAULT 0,
                games_lost INTEGER DEFAULT 0,
                times_as_prop INTEGER DEFAULT 0,
                prop_survives INTEGER DEFAULT 0,
                prop_deaths INTEGER DEFAULT 0,
                total_time_hidden INTEGER DEFAULT 0,
                successful_taunts INTEGER DEFAULT 0,
                times_as_hunter INTEGER DEFAULT 0,
                props_found INTEGER DEFAULT 0,
                props_killed INTEGER DEFAULT 0,
                wrong_hits INTEGER DEFAULT 0,
                hunter_deaths INTEGER DEFAULT 0,
                total_points INTEGER DEFAULT 0,
                highest_game_points INTEGER DEFAULT 0,
                total_play_time INTEGER DEFAULT 0,
                first_played INTEGER,
                last_played INTEGER
            )
            """;

    public StatsManager(PropHuntPlugin plugin) {
        this.plugin = plugin;
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Initializes the database connection.
     */
    public void initialize() {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            File dbFile = new File(dataFolder, "stats.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

            connection = DriverManager.getConnection(url);
            plugin.debug("Connected to SQLite database");

            // Create table
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(CREATE_TABLE);
            }

            plugin.debug("Stats database initialized");

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize stats database: " + e.getMessage());
        }
    }

    /**
     * Closes the database connection.
     */
    public void shutdown() {
        // Save all cached stats
        for (PlayerStats stats : cache.values()) {
            saveStatsSync(stats);
        }
        cache.clear();

        // Close connection
        if (connection != null) {
            try {
                connection.close();
                plugin.debug("Stats database connection closed");
            } catch (SQLException e) {
                plugin.getLogger().warning("Error closing database: " + e.getMessage());
            }
        }
    }

    /**
     * Gets stats for a player, loading from database if needed.
     */
    public PlayerStats getStats(Player player) {
        return getStats(player.getUniqueId(), player.getName());
    }

    /**
     * Gets stats for a UUID, loading from database if needed.
     */
    public PlayerStats getStats(UUID uuid, String name) {
        return cache.computeIfAbsent(uuid, id -> loadOrCreate(id, name));
    }

    /**
     * Gets cached stats for a UUID.
     */
    public PlayerStats getCachedStats(UUID uuid) {
        return cache.get(uuid);
    }

    /**
     * Loads stats from database or creates new.
     */
    private PlayerStats loadOrCreate(UUID uuid, String name) {
        PlayerStats stats = loadStatsSync(uuid);
        if (stats == null) {
            stats = new PlayerStats(uuid, name);
        } else {
            stats.setLastKnownName(name);
        }
        return stats;
    }

    /**
     * Loads stats synchronously.
     */
    private PlayerStats loadStatsSync(UUID uuid) {
        if (connection == null) return null;

        String sql = "SELECT * FROM player_stats WHERE uuid = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PlayerStats stats = new PlayerStats(uuid);
                    stats.setLastKnownName(rs.getString("last_known_name"));
                    stats.setGamesPlayed(rs.getInt("games_played"));
                    stats.setGamesWon(rs.getInt("games_won"));
                    stats.setGamesLost(rs.getInt("games_lost"));
                    stats.setTimesAsProp(rs.getInt("times_as_prop"));
                    stats.setPropSurvives(rs.getInt("prop_survives"));
                    stats.setPropDeaths(rs.getInt("prop_deaths"));
                    stats.setTotalTimeAsHiddenProp(rs.getLong("total_time_hidden"));
                    stats.setSuccessfulTaunts(rs.getInt("successful_taunts"));
                    stats.setTimesAsHunter(rs.getInt("times_as_hunter"));
                    stats.setPropsFound(rs.getInt("props_found"));
                    stats.setPropsKilled(rs.getInt("props_killed"));
                    stats.setWrongHits(rs.getInt("wrong_hits"));
                    stats.setHunterDeaths(rs.getInt("hunter_deaths"));
                    stats.setTotalPoints(rs.getLong("total_points"));
                    stats.setHighestGamePoints(rs.getInt("highest_game_points"));
                    stats.setTotalPlayTime(rs.getLong("total_play_time"));
                    stats.setFirstPlayed(rs.getLong("first_played"));
                    stats.setLastPlayed(rs.getLong("last_played"));
                    return stats;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error loading stats for " + uuid + ": " + e.getMessage());
        }

        return null;
    }

    /**
     * Saves stats asynchronously.
     */
    public void saveStats(PlayerStats stats) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> saveStatsSync(stats));
    }

    /**
     * Saves stats synchronously.
     */
    private void saveStatsSync(PlayerStats stats) {
        if (connection == null) return;

        String sql = """
                INSERT OR REPLACE INTO player_stats (
                    uuid, last_known_name, games_played, games_won, games_lost,
                    times_as_prop, prop_survives, prop_deaths, total_time_hidden, successful_taunts,
                    times_as_hunter, props_found, props_killed, wrong_hits, hunter_deaths,
                    total_points, highest_game_points, total_play_time, first_played, last_played
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, stats.getUuid().toString());
            stmt.setString(2, stats.getLastKnownName());
            stmt.setInt(3, stats.getGamesPlayed());
            stmt.setInt(4, stats.getGamesWon());
            stmt.setInt(5, stats.getGamesLost());
            stmt.setInt(6, stats.getTimesAsProp());
            stmt.setInt(7, stats.getPropSurvives());
            stmt.setInt(8, stats.getPropDeaths());
            stmt.setLong(9, stats.getTotalTimeAsHiddenProp());
            stmt.setInt(10, stats.getSuccessfulTaunts());
            stmt.setInt(11, stats.getTimesAsHunter());
            stmt.setInt(12, stats.getPropsFound());
            stmt.setInt(13, stats.getPropsKilled());
            stmt.setInt(14, stats.getWrongHits());
            stmt.setInt(15, stats.getHunterDeaths());
            stmt.setLong(16, stats.getTotalPoints());
            stmt.setInt(17, stats.getHighestGamePoints());
            stmt.setLong(18, stats.getTotalPlayTime());
            stmt.setLong(19, stats.getFirstPlayed());
            stmt.setLong(20, stats.getLastPlayed());

            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Error saving stats for " + stats.getUuid() + ": " + e.getMessage());
        }
    }

    /**
     * Gets the top players by a stat.
     */
    public CompletableFuture<List<PlayerStats>> getTopPlayers(StatType statType, int limit) {
        return CompletableFuture.supplyAsync(() -> getTopPlayersSync(statType, limit));
    }

    /**
     * Gets top players synchronously.
     */
    private List<PlayerStats> getTopPlayersSync(StatType statType, int limit) {
        if (connection == null) return List.of();

        String column = statType.getColumn();
        String sql = "SELECT * FROM player_stats ORDER BY " + column + " DESC LIMIT ?";

        List<PlayerStats> results = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    PlayerStats stats = new PlayerStats(uuid);
                    stats.setLastKnownName(rs.getString("last_known_name"));
                    stats.setGamesPlayed(rs.getInt("games_played"));
                    stats.setGamesWon(rs.getInt("games_won"));
                    stats.setGamesLost(rs.getInt("games_lost"));
                    stats.setTimesAsProp(rs.getInt("times_as_prop"));
                    stats.setPropSurvives(rs.getInt("prop_survives"));
                    stats.setPropDeaths(rs.getInt("prop_deaths"));
                    stats.setTotalTimeAsHiddenProp(rs.getLong("total_time_hidden"));
                    stats.setSuccessfulTaunts(rs.getInt("successful_taunts"));
                    stats.setTimesAsHunter(rs.getInt("times_as_hunter"));
                    stats.setPropsFound(rs.getInt("props_found"));
                    stats.setPropsKilled(rs.getInt("props_killed"));
                    stats.setWrongHits(rs.getInt("wrong_hits"));
                    stats.setHunterDeaths(rs.getInt("hunter_deaths"));
                    stats.setTotalPoints(rs.getLong("total_points"));
                    stats.setHighestGamePoints(rs.getInt("highest_game_points"));
                    stats.setTotalPlayTime(rs.getLong("total_play_time"));
                    stats.setFirstPlayed(rs.getLong("first_played"));
                    stats.setLastPlayed(rs.getLong("last_played"));
                    results.add(stats);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error getting top players: " + e.getMessage());
        }

        return results;
    }

    /**
     * Gets a player's rank for a stat.
     */
    public CompletableFuture<Integer> getPlayerRank(UUID uuid, StatType statType) {
        return CompletableFuture.supplyAsync(() -> getPlayerRankSync(uuid, statType));
    }

    /**
     * Gets player rank synchronously.
     */
    private int getPlayerRankSync(UUID uuid, StatType statType) {
        if (connection == null) return -1;

        String column = statType.getColumn();
        String sql = """
                SELECT COUNT(*) + 1 as rank FROM player_stats
                WHERE %s > (SELECT %s FROM player_stats WHERE uuid = ?)
                """.formatted(column, column);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rank");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error getting player rank: " + e.getMessage());
        }

        return -1;
    }

    /**
     * Removes a player from cache.
     */
    public void unloadPlayer(UUID uuid) {
        PlayerStats stats = cache.remove(uuid);
        if (stats != null) {
            saveStats(stats);
        }
    }

    /**
     * Types of stats for leaderboards.
     */
    public enum StatType {
        GAMES_PLAYED("games_played", "Games Played"),
        GAMES_WON("games_won", "Games Won"),
        TOTAL_POINTS("total_points", "Total Points"),
        PROPS_KILLED("props_killed", "Props Killed"),
        PROPS_FOUND("props_found", "Props Found"),
        PROP_SURVIVES("prop_survives", "Times Survived"),
        HIGHEST_GAME_POINTS("highest_game_points", "Highest Game Score"),
        TOTAL_PLAY_TIME("total_play_time", "Total Play Time");

        private final String column;
        private final String displayName;

        StatType(String column, String displayName) {
            this.column = column;
            this.displayName = displayName;
        }

        public String getColumn() {
            return column;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
