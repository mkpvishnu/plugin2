package com.example.prophunt.listeners;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.managers.SelectionManager;
import com.example.prophunt.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles arena setup interactions including selection wand clicks.
 */
public class SetupListener implements Listener {

    private final PropHuntPlugin plugin;

    public SetupListener(PropHuntPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if player is using a selection wand
        if (!SelectionManager.isWand(item)) {
            return;
        }

        // Only handle in setup mode
        SelectionManager selectionManager = plugin.getSelectionManager();
        if (!selectionManager.isInSetupMode(player)) {
            return;
        }

        Action action = event.getAction();

        // Handle left-click (pos1) and right-click (pos2)
        if (action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            Block block = event.getClickedBlock();
            if (block != null) {
                Location loc = block.getLocation();
                selectionManager.setPos1(player, loc);
                player.sendMessage(MessageUtil.colorize(
                    "&6&lPos1 &eset to &7(" + loc.getBlockX() + ", " +
                    loc.getBlockY() + ", " + loc.getBlockZ() + ")"));
                showSelectionStatus(player);
            }
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Block block = event.getClickedBlock();
            if (block != null) {
                Location loc = block.getLocation();
                selectionManager.setPos2(player, loc);
                player.sendMessage(MessageUtil.colorize(
                    "&6&lPos2 &eset to &7(" + loc.getBlockX() + ", " +
                    loc.getBlockY() + ", " + loc.getBlockZ() + ")"));
                showSelectionStatus(player);
            }
        } else if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR) {
            // Clicking air with wand - show current selection
            event.setCancelled(true);
            player.sendMessage(selectionManager.getSelectionInfo(player));
        }
    }

    /**
     * Shows selection status if both positions are set.
     */
    private void showSelectionStatus(Player player) {
        SelectionManager selectionManager = plugin.getSelectionManager();
        if (selectionManager.hasCompleteSelection(player)) {
            player.sendMessage(MessageUtil.colorize(
                "&aSelection complete! &7Use &e/ph setregion <type> &7to save."));
            player.sendMessage(MessageUtil.colorize(
                "&7Types: &earena&7, &elobby&7, &ehuntercage"));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up selection data when player leaves
        plugin.getSelectionManager().cleanup(event.getPlayer().getUniqueId());
    }
}
