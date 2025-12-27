package com.example.prophunt.listeners;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.game.Game;
import com.example.prophunt.game.GameState;
import com.example.prophunt.player.GamePlayer;
import com.example.prophunt.player.PropPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles prop-specific item interactions.
 */
public class PropItemListener implements Listener {

    private final PropHuntPlugin plugin;

    public PropItemListener(PropHuntPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles right-click interactions for props.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Only handle right-clicks
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        // Check if player is a prop in a game
        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp == null || !(gp instanceof PropPlayer prop)) return;

        Game game = prop.getGame();
        if (game == null) return;

        // Handle Nether Star - Open prop selector GUI
        if (item.getType() == Material.NETHER_STAR) {
            event.setCancelled(true);

            // Only allow during hiding phase or hunting phase
            if (game.getState() == GameState.HIDING || game.getState() == GameState.HUNTING) {
                plugin.getPropSelectorGUI().open(prop);
            } else {
                plugin.getMessageUtil().send(player, "prop.cannot-change-now");
            }
            return;
        }

        // Handle Feather - Voluntary taunt
        if (item.getType() == Material.FEATHER) {
            event.setCancelled(true);

            if (game.getState() == GameState.HUNTING) {
                plugin.getTauntManager().voluntaryTaunt(prop);
            } else {
                plugin.getMessageUtil().send(player, "prop.cannot-taunt-now");
            }
            return;
        }
    }
}
