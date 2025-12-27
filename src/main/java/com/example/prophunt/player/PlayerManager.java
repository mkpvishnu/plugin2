package com.example.prophunt.player;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.game.Game;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages all players currently in PropHunt games.
 */
public class PlayerManager {

    private final PropHuntPlugin plugin;
    private final Map<UUID, GamePlayer> players;

    public PlayerManager(PropHuntPlugin plugin) {
        this.plugin = plugin;
        this.players = new HashMap<>();
    }

    /**
     * Adds a player to tracking.
     *
     * @param gamePlayer the game player
     */
    public void addPlayer(GamePlayer gamePlayer) {
        players.put(gamePlayer.getUuid(), gamePlayer);
    }

    /**
     * Removes a player from tracking.
     *
     * @param uuid the player's UUID
     * @return the removed player, or null
     */
    public GamePlayer removePlayer(UUID uuid) {
        return players.remove(uuid);
    }

    /**
     * Gets a GamePlayer by UUID.
     *
     * @param uuid the UUID
     * @return the GamePlayer, or null
     */
    public GamePlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    /**
     * Gets a GamePlayer by Bukkit Player.
     *
     * @param player the player
     * @return the GamePlayer, or null
     */
    public GamePlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }

    /**
     * Gets a PropPlayer if the player is a prop.
     *
     * @param player the player
     * @return the PropPlayer, or null
     */
    public PropPlayer getPropPlayer(Player player) {
        GamePlayer gp = getPlayer(player);
        return gp instanceof PropPlayer ? (PropPlayer) gp : null;
    }

    /**
     * Gets a HunterPlayer if the player is a hunter.
     *
     * @param player the player
     * @return the HunterPlayer, or null
     */
    public HunterPlayer getHunterPlayer(Player player) {
        GamePlayer gp = getPlayer(player);
        return gp instanceof HunterPlayer ? (HunterPlayer) gp : null;
    }

    /**
     * Checks if a player is in a game.
     *
     * @param player the player
     * @return true if in a game
     */
    public boolean isInGame(Player player) {
        return players.containsKey(player.getUniqueId());
    }

    /**
     * Checks if a player is in a game.
     *
     * @param uuid the player's UUID
     * @return true if in a game
     */
    public boolean isInGame(UUID uuid) {
        return players.containsKey(uuid);
    }

    /**
     * Gets the game a player is in.
     *
     * @param player the player
     * @return the game, or null
     */
    public Game getPlayerGame(Player player) {
        GamePlayer gp = getPlayer(player);
        return gp != null ? gp.getGame() : null;
    }

    /**
     * Gets all tracked players.
     *
     * @return unmodifiable collection of players
     */
    public Collection<GamePlayer> getAllPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    /**
     * Gets the count of players in games.
     *
     * @return player count
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Clears all tracked players.
     */
    public void clear() {
        players.clear();
    }

    /**
     * Handles player disconnect.
     *
     * @param player the player
     */
    public void handleDisconnect(Player player) {
        GamePlayer gp = getPlayer(player);
        if (gp != null && gp.getGame() != null) {
            gp.getGame().removePlayer(player, true);
        }
    }
}
