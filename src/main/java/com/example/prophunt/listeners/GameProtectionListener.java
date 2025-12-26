package com.example.prophunt.listeners;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.game.Game;
import com.example.prophunt.player.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Protects game state by preventing unwanted actions.
 */
public class GameProtectionListener implements Listener {

    private final PropHuntPlugin plugin;

    public GameProtectionListener(PropHuntPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Prevents block breaking in games.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents block placing in games.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents item dropping in games.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (isInGame(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents item pickup in games.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && isInGame(player)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents hunger in games.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && isInGame(player)) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents inventory modification in games.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && isInGame(player)) {
            // Allow clicking in custom GUIs (handled by GUI system)
            // For now, cancel all inventory clicks
            if (event.getClickedInventory() != null &&
                event.getClickedInventory().equals(player.getInventory())) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Checks if a player is in a game.
     */
    private boolean isInGame(Player player) {
        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        return gp != null && gp.getGame() != null;
    }
}
