package com.example.prophunt.listeners;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.game.Game;
import com.example.prophunt.player.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player connection events.
 */
public class PlayerConnectionListener implements Listener {

    private final PropHuntPlugin plugin;

    public PlayerConnectionListener(PropHuntPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player joining the server.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if player was in a game (reconnection)
        // For now, we don't support reconnection - they would need to rejoin
    }

    /**
     * Handles player leaving the server.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Check if player is in a game
        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp == null) return;

        Game game = gp.getGame();
        if (game != null) {
            // Remove from game (disconnected)
            game.removePlayer(player, true);

            // Remove disguise if they had one
            plugin.getDisguiseManager().removeDisguise(player);

            plugin.debug("Player %s disconnected from game in arena %s",
                    player.getName(), game.getArena().getName());
        }
    }
}
