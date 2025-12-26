package com.example.prophunt.api;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.arena.Arena;
import com.example.prophunt.game.Game;
import com.example.prophunt.player.GamePlayer;
import com.example.prophunt.stats.PlayerStats;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;

/**
 * Public API for PropHunt plugin.
 * Provides access to game state and player information for other plugins.
 */
public class PropHuntAPI {

    private final PropHuntPlugin plugin;

    public PropHuntAPI(PropHuntPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the plugin instance.
     *
     * @return the PropHunt plugin
     */
    public PropHuntPlugin getPlugin() {
        return plugin;
    }

    // ==================== Arena Methods ====================

    /**
     * Gets all registered arenas.
     *
     * @return collection of arenas
     */
    public Collection<Arena> getArenas() {
        return plugin.getArenaManager().getArenas();
    }

    /**
     * Gets an arena by name.
     *
     * @param name the arena name
     * @return the arena, or empty if not found
     */
    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(plugin.getArenaManager().getArena(name));
    }

    // ==================== Game Methods ====================

    /**
     * Gets all active games.
     *
     * @return collection of active games
     */
    public Collection<Game> getActiveGames() {
        return plugin.getGameManager().getActiveGames();
    }

    /**
     * Gets the game in a specific arena.
     *
     * @param arena the arena
     * @return the game, or empty if no game is running
     */
    public Optional<Game> getGame(Arena arena) {
        return Optional.ofNullable(plugin.getGameManager().getGame(arena));
    }

    /**
     * Gets the game a player is in.
     *
     * @param player the player
     * @return the game, or empty if not in a game
     */
    public Optional<Game> getPlayerGame(Player player) {
        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp != null && gp.getGame() != null) {
            return Optional.of(gp.getGame());
        }
        return Optional.empty();
    }

    // ==================== Player Methods ====================

    /**
     * Checks if a player is in a game.
     *
     * @param player the player
     * @return true if in a game
     */
    public boolean isInGame(Player player) {
        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        return gp != null && gp.getGame() != null;
    }

    /**
     * Gets a player's game wrapper.
     *
     * @param player the player
     * @return the game player wrapper, or empty if not in a game
     */
    public Optional<GamePlayer> getGamePlayer(Player player) {
        return Optional.ofNullable(plugin.getPlayerManager().getPlayer(player));
    }

    /**
     * Gets a player's lifetime stats.
     *
     * @param player the player
     * @return the player's stats
     */
    public PlayerStats getPlayerStats(Player player) {
        return plugin.getStatsManager().getStats(player);
    }

    // ==================== Utility Methods ====================

    /**
     * Gets the plugin version.
     *
     * @return the version string
     */
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    /**
     * Checks if an arena is available for joining.
     *
     * @param arena the arena
     * @return true if the arena is joinable
     */
    public boolean isArenaJoinable(Arena arena) {
        if (!arena.isEnabled()) return false;

        Game game = plugin.getGameManager().getGame(arena);
        if (game == null) return true;

        return game.getState().canJoin() && !game.isFull();
    }

    /**
     * Gets the number of players in a game.
     *
     * @param arena the arena
     * @return the player count, or 0 if no game
     */
    public int getPlayerCount(Arena arena) {
        Game game = plugin.getGameManager().getGame(arena);
        return game != null ? game.getPlayerCount() : 0;
    }
}
