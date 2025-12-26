package com.example.prophunt.game;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.arena.Arena;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages all active PropHunt games.
 */
public class GameManager {

    private final PropHuntPlugin plugin;
    private final Map<String, Game> games;

    public GameManager(PropHuntPlugin plugin) {
        this.plugin = plugin;
        this.games = new HashMap<>();
    }

    /**
     * Gets or creates a game for an arena.
     *
     * @param arena the arena
     * @return the game instance
     */
    public Game getOrCreateGame(Arena arena) {
        String key = arena.getName().toLowerCase();
        return games.computeIfAbsent(key, k -> new Game(plugin, arena));
    }

    /**
     * Gets a game by arena name.
     *
     * @param arenaName the arena name
     * @return the game, or null
     */
    public Game getGame(String arenaName) {
        return games.get(arenaName.toLowerCase());
    }

    /**
     * Gets a game by arena.
     *
     * @param arena the arena
     * @return the game, or null
     */
    public Game getGame(Arena arena) {
        return getGame(arena.getName());
    }

    /**
     * Gets the game a player is in.
     *
     * @param player the player
     * @return the game, or null
     */
    public Game getPlayerGame(Player player) {
        for (Game game : games.values()) {
            if (game.isInGame(player)) {
                return game;
            }
        }
        return null;
    }

    /**
     * Checks if a player is in any game.
     *
     * @param player the player
     * @return true if in a game
     */
    public boolean isInGame(Player player) {
        return getPlayerGame(player) != null;
    }

    /**
     * Gets all active games.
     *
     * @return collection of games
     */
    public Collection<Game> getGames() {
        return Collections.unmodifiableCollection(games.values());
    }

    /**
     * Gets games that are in progress.
     *
     * @return list of active games
     */
    public List<Game> getActiveGames() {
        return games.values().stream()
                .filter(g -> g.getState().isInProgress())
                .toList();
    }

    /**
     * Gets games that are joinable.
     *
     * @return list of joinable games
     */
    public List<Game> getJoinableGames() {
        return games.values().stream()
                .filter(Game::canJoin)
                .toList();
    }

    /**
     * Finds the best game to join.
     * Prefers games with more players (closer to starting).
     *
     * @return best game to join, or null if none available
     */
    public Game findBestGame() {
        return getJoinableGames().stream()
                .max(Comparator.comparingInt(Game::getPlayerCount))
                .orElse(null);
    }

    /**
     * Adds a player to a game.
     *
     * @param player the player
     * @param arena the arena
     * @return true if successful
     */
    public boolean joinGame(Player player, Arena arena) {
        if (!arena.isEnabled()) {
            return false;
        }

        if (isInGame(player)) {
            return false;
        }

        Game game = getOrCreateGame(arena);
        return game.addPlayer(player);
    }

    /**
     * Adds a player to any available game.
     *
     * @param player the player
     * @return the game joined, or null if none available
     */
    public Game joinAnyGame(Player player) {
        if (isInGame(player)) {
            return null;
        }

        // Try to find a game that's waiting and has space
        Game best = findBestGame();
        if (best != null && best.addPlayer(player)) {
            return best;
        }

        // Try to create a new game from any enabled arena
        for (Arena arena : plugin.getArenaManager().getEnabledArenas()) {
            Game game = getOrCreateGame(arena);
            if (game.canJoin() && game.addPlayer(player)) {
                return game;
            }
        }

        return null;
    }

    /**
     * Removes a player from their current game.
     *
     * @param player the player
     * @return true if removed
     */
    public boolean leaveGame(Player player) {
        Game game = getPlayerGame(player);
        if (game == null) {
            return false;
        }

        game.removePlayer(player, false);
        return true;
    }

    /**
     * Force starts a game.
     *
     * @param arena the arena
     * @return true if started
     */
    public boolean forceStart(Arena arena) {
        Game game = getGame(arena);
        if (game == null || game.getPlayerCount() < 2) {
            return false;
        }

        game.forceStart();
        return true;
    }

    /**
     * Force stops a game.
     *
     * @param arena the arena
     * @return true if stopped
     */
    public boolean forceStop(Arena arena) {
        Game game = getGame(arena);
        if (game == null) {
            return false;
        }

        game.forceStop();
        return true;
    }

    /**
     * Ends all running games.
     */
    public void endAllGames() {
        for (Game game : new ArrayList<>(games.values())) {
            if (game.getState() != GameState.DISABLED) {
                game.forceStop();
            }
        }
    }

    /**
     * Removes a game instance.
     *
     * @param arenaName the arena name
     */
    public void removeGame(String arenaName) {
        Game game = games.remove(arenaName.toLowerCase());
        if (game != null) {
            game.forceStop();
        }
    }

    /**
     * Gets the count of active games.
     *
     * @return active game count
     */
    public int getActiveGameCount() {
        return (int) games.values().stream()
                .filter(g -> g.getState().isInProgress())
                .count();
    }

    /**
     * Gets the total player count across all games.
     *
     * @return total players
     */
    public int getTotalPlayerCount() {
        return games.values().stream()
                .mapToInt(Game::getPlayerCount)
                .sum();
    }
}
