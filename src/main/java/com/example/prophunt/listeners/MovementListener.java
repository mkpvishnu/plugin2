package com.example.prophunt.listeners;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.disguise.DisguiseManager;
import com.example.prophunt.disguise.PropDisguise;
import com.example.prophunt.game.Game;
import com.example.prophunt.game.GameState;
import com.example.prophunt.player.GamePlayer;
import com.example.prophunt.player.PropPlayer;
import com.example.prophunt.util.ParticleUtil;
import com.example.prophunt.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * Handles movement mechanics for props.
 */
public class MovementListener implements Listener {

    private final PropHuntPlugin plugin;

    public MovementListener(PropHuntPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles prop movement (ghost mode when moving).
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check for actual position changes
        if (!hasMovedBlock(event.getFrom(), event.getTo())) {
            return;
        }

        Player player = event.getPlayer();

        // Check if player is a prop
        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp == null || !(gp instanceof PropPlayer prop)) return;

        Game game = prop.getGame();
        if (game == null || !game.getState().isInProgress()) return;

        DisguiseManager dm = plugin.getDisguiseManager();
        PropDisguise disguise = dm.getDisguise(prop);

        if (disguise == null || !disguise.isActive()) return;

        // If locked, prevent movement
        if (disguise.isLocked()) {
            // Allow rotation but not movement
            if (hasMovedPosition(event.getFrom(), event.getTo())) {
                // Reset position but allow rotation
                Location to = event.getTo().clone();
                to.setX(event.getFrom().getX());
                to.setY(event.getFrom().getY());
                to.setZ(event.getFrom().getZ());
                event.setTo(to);
            }
            return;
        }

        // Player is moving while unlocked - ghost mode
        disguise.setGhostMode(true);

        // Play subtle footstep particles
        if (game.getState() == GameState.HUNTING) {
            ParticleUtil.playGhostEffect(player.getLocation());
        }

        // Schedule return to solid after movement stops
        // This is handled by the disguise update task
    }

    /**
     * Handles sneaking to lock/unlock.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp == null || !(gp instanceof PropPlayer prop)) return;

        Game game = prop.getGame();
        if (game == null || !game.getState().isInProgress()) return;

        // Check if prop can lock
        if (!game.getSettings().isCanLock()) return;

        DisguiseManager dm = plugin.getDisguiseManager();
        PropDisguise disguise = dm.getDisguise(prop);

        if (disguise == null || !disguise.isActive()) return;

        // Toggle lock state on sneak
        if (event.isSneaking()) {
            boolean wasLocked = disguise.isLocked();
            dm.toggleLocked(prop);

            if (!wasLocked && disguise.isLocked()) {
                // Just locked
                plugin.getMessageUtil().send(player, "prop.locked");
            } else if (wasLocked && !disguise.isLocked()) {
                // Just unlocked
                plugin.getMessageUtil().send(player, "prop.unlocked");
            }
        }
    }

    /**
     * Checks if the player moved to a different block.
     */
    private boolean hasMovedBlock(Location from, Location to) {
        if (from == null || to == null) return false;

        return from.getBlockX() != to.getBlockX() ||
               from.getBlockY() != to.getBlockY() ||
               from.getBlockZ() != to.getBlockZ();
    }

    /**
     * Checks if the player's position changed.
     */
    private boolean hasMovedPosition(Location from, Location to) {
        if (from == null || to == null) return false;

        return from.getX() != to.getX() ||
               from.getY() != to.getY() ||
               from.getZ() != to.getZ();
    }
}
