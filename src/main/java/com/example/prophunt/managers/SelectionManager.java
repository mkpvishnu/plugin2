package com.example.prophunt.managers;

import com.example.prophunt.arena.ArenaRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Manages player selections for defining arena regions.
 * Tracks pos1/pos2 for each player and provides selection wand functionality.
 */
public class SelectionManager {

    /**
     * The material used for the selection wand.
     */
    public static final Material WAND_MATERIAL = Material.GOLDEN_AXE;

    /**
     * The display name of the selection wand.
     */
    public static final String WAND_NAME = "§6§lRegion Selector";

    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();
    private final Set<UUID> playersInSetupMode = new HashSet<>();

    /**
     * Sets position 1 for a player.
     *
     * @param player the player
     * @param location the location
     */
    public void setPos1(Player player, Location location) {
        pos1Map.put(player.getUniqueId(), location.clone());
    }

    /**
     * Sets position 2 for a player.
     *
     * @param player the player
     * @param location the location
     */
    public void setPos2(Player player, Location location) {
        pos2Map.put(player.getUniqueId(), location.clone());
    }

    /**
     * Gets position 1 for a player.
     *
     * @param player the player
     * @return the location, or null if not set
     */
    public Location getPos1(Player player) {
        return pos1Map.get(player.getUniqueId());
    }

    /**
     * Gets position 2 for a player.
     *
     * @param player the player
     * @return the location, or null if not set
     */
    public Location getPos2(Player player) {
        return pos2Map.get(player.getUniqueId());
    }

    /**
     * Checks if a player has a complete selection (both positions set).
     *
     * @param player the player
     * @return true if both positions are set
     */
    public boolean hasCompleteSelection(Player player) {
        Location p1 = getPos1(player);
        Location p2 = getPos2(player);
        return p1 != null && p2 != null && p1.getWorld() != null &&
               p1.getWorld().equals(p2.getWorld());
    }

    /**
     * Creates an ArenaRegion from the player's current selection.
     *
     * @param player the player
     * @return the region, or null if selection is incomplete
     */
    public ArenaRegion createRegionFromSelection(Player player) {
        if (!hasCompleteSelection(player)) {
            return null;
        }
        return new ArenaRegion(getPos1(player), getPos2(player));
    }

    /**
     * Clears the selection for a player.
     *
     * @param player the player
     */
    public void clearSelection(Player player) {
        pos1Map.remove(player.getUniqueId());
        pos2Map.remove(player.getUniqueId());
    }

    /**
     * Puts a player in setup mode.
     *
     * @param player the player
     */
    public void enterSetupMode(Player player) {
        playersInSetupMode.add(player.getUniqueId());
        clearSelection(player);
    }

    /**
     * Removes a player from setup mode.
     *
     * @param player the player
     */
    public void exitSetupMode(Player player) {
        playersInSetupMode.remove(player.getUniqueId());
        clearSelection(player);
    }

    /**
     * Checks if a player is in setup mode.
     *
     * @param player the player
     * @return true if in setup mode
     */
    public boolean isInSetupMode(Player player) {
        return playersInSetupMode.contains(player.getUniqueId());
    }

    /**
     * Gets the arena name a player is setting up, if tracked.
     * This is a simple implementation - we track just setup mode status.
     * The arena being edited is tracked via command context.
     *
     * @param player the player
     * @return true if in setup mode
     */
    public boolean isInSetupMode(UUID playerId) {
        return playersInSetupMode.contains(playerId);
    }

    /**
     * Creates a selection wand item.
     *
     * @return the wand item
     */
    public static ItemStack createWand() {
        ItemStack wand = new ItemStack(WAND_MATERIAL);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(WAND_NAME);
            meta.setLore(Arrays.asList(
                "§7Left-click: Set position 1",
                "§7Right-click: Set position 2",
                "",
                "§eUse §f/ph setregion <type>",
                "§eto save your selection"
            ));
            wand.setItemMeta(meta);
        }
        return wand;
    }

    /**
     * Checks if an item is a selection wand.
     *
     * @param item the item to check
     * @return true if it's a selection wand
     */
    public static boolean isWand(ItemStack item) {
        if (item == null || item.getType() != WAND_MATERIAL) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta != null && WAND_NAME.equals(meta.getDisplayName());
    }

    /**
     * Gets selection info string for a player.
     *
     * @param player the player
     * @return formatted selection info
     */
    public String getSelectionInfo(Player player) {
        Location p1 = getPos1(player);
        Location p2 = getPos2(player);

        StringBuilder sb = new StringBuilder();
        sb.append("§7Selection: ");

        if (p1 != null) {
            sb.append("§aPos1 §7(").append(p1.getBlockX()).append(", ")
              .append(p1.getBlockY()).append(", ").append(p1.getBlockZ()).append(")");
        } else {
            sb.append("§cPos1 not set");
        }

        sb.append(" §7| ");

        if (p2 != null) {
            sb.append("§aPos2 §7(").append(p2.getBlockX()).append(", ")
              .append(p2.getBlockY()).append(", ").append(p2.getBlockZ()).append(")");
        } else {
            sb.append("§cPos2 not set");
        }

        if (hasCompleteSelection(player)) {
            ArenaRegion region = createRegionFromSelection(player);
            if (region != null) {
                sb.append("\n§7Size: §e").append(region.getDimensions())
                  .append(" §7(").append(region.getVolume()).append(" blocks)");
            }
        }

        return sb.toString();
    }

    /**
     * Cleans up when a player disconnects.
     *
     * @param playerId the player's UUID
     */
    public void cleanup(UUID playerId) {
        pos1Map.remove(playerId);
        pos2Map.remove(playerId);
        playersInSetupMode.remove(playerId);
    }
}
