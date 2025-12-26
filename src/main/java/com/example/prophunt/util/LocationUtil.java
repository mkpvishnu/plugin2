package com.example.prophunt.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for location-related operations.
 */
public final class LocationUtil {

    private LocationUtil() {
        // Utility class
    }

    /**
     * Saves a location to a configuration section.
     *
     * @param section the section to save to
     * @param location the location to save
     */
    public static void save(ConfigurationSection section, Location location) {
        if (location == null || location.getWorld() == null) return;

        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }

    /**
     * Loads a location from a configuration section.
     *
     * @param section the section to load from
     * @return the loaded location, or null if invalid
     */
    public static Location load(ConfigurationSection section) {
        if (section == null) return null;

        String worldName = section.getString("world");
        if (worldName == null) return null;

        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw", 0);
        float pitch = (float) section.getDouble("pitch", 0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Serializes a location to a string.
     *
     * @param location the location
     * @return serialized string
     */
    public static String serialize(Location location) {
        if (location == null || location.getWorld() == null) return null;

        return String.format("%s;%.2f;%.2f;%.2f;%.2f;%.2f",
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    /**
     * Deserializes a location from a string.
     *
     * @param string the serialized string
     * @return the location, or null if invalid
     */
    public static Location deserialize(String string) {
        if (string == null || string.isEmpty()) return null;

        String[] parts = string.split(";");
        if (parts.length < 4) return null;

        World world = Bukkit.getWorld(parts[0]);
        if (world == null) return null;

        try {
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0;

            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Centers a location on the block.
     *
     * @param location the location
     * @return centered location
     */
    public static Location center(Location location) {
        if (location == null) return null;

        Location centered = location.clone();
        centered.setX(location.getBlockX() + 0.5);
        centered.setZ(location.getBlockZ() + 0.5);
        return centered;
    }

    /**
     * Gets a safe spawn location (on ground, not in blocks).
     *
     * @param location the original location
     * @return safe location
     */
    public static Location getSafeLocation(Location location) {
        if (location == null || location.getWorld() == null) return null;

        Location safe = center(location.clone());
        World world = safe.getWorld();

        // Move up if inside a block
        while (safe.getY() < world.getMaxHeight() - 1 &&
               (!safe.getBlock().isPassable() ||
                !safe.getBlock().getRelative(BlockFace.UP).isPassable())) {
            safe.add(0, 1, 0);
        }

        // Move down to ground
        while (safe.getY() > world.getMinHeight() + 1 &&
               safe.getBlock().getRelative(BlockFace.DOWN).isPassable()) {
            safe.subtract(0, 1, 0);
        }

        return safe;
    }

    /**
     * Checks if a location is safe for spawning.
     *
     * @param location the location
     * @return true if safe
     */
    public static boolean isSafe(Location location) {
        if (location == null || location.getWorld() == null) return false;

        Block block = location.getBlock();
        Block above = block.getRelative(BlockFace.UP);
        Block below = block.getRelative(BlockFace.DOWN);

        return block.isPassable() &&
               above.isPassable() &&
               !below.isPassable();
    }

    /**
     * Gets the distance between two locations (ignoring Y).
     *
     * @param loc1 first location
     * @param loc2 second location
     * @return horizontal distance
     */
    public static double getHorizontalDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return Double.MAX_VALUE;
        if (!loc1.getWorld().equals(loc2.getWorld())) return Double.MAX_VALUE;

        double dx = loc1.getX() - loc2.getX();
        double dz = loc1.getZ() - loc2.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /**
     * Gets a location string for display.
     *
     * @param location the location
     * @return formatted string
     */
    public static String toString(Location location) {
        if (location == null) return "null";

        return String.format("(%d, %d, %d in %s)",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ(),
                location.getWorld() != null ? location.getWorld().getName() : "null");
    }

    /**
     * Gets a compact location string.
     *
     * @param location the location
     * @return compact string
     */
    public static String toCompactString(Location location) {
        if (location == null) return "null";

        return String.format("%d, %d, %d",
                location.getBlockX(),
                location.getBlockY(),
                location.getBlockZ());
    }

    /**
     * Gets players within a radius of a location.
     *
     * @param location center location
     * @param radius the radius
     * @return list of nearby players
     */
    public static List<Player> getNearbyPlayers(Location location, double radius) {
        List<Player> nearby = new ArrayList<>();
        if (location == null || location.getWorld() == null) return nearby;

        double radiusSquared = radius * radius;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(location) <= radiusSquared) {
                nearby.add(player);
            }
        }
        return nearby;
    }

    /**
     * Checks if two locations are in the same block.
     *
     * @param loc1 first location
     * @param loc2 second location
     * @return true if same block
     */
    public static boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        if (!loc1.getWorld().equals(loc2.getWorld())) return false;

        return loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }

    /**
     * Copies location values without creating a new object.
     *
     * @param from source location
     * @param to target location
     */
    public static void copyTo(Location from, Location to) {
        if (from == null || to == null) return;

        to.setWorld(from.getWorld());
        to.setX(from.getX());
        to.setY(from.getY());
        to.setZ(from.getZ());
        to.setYaw(from.getYaw());
        to.setPitch(from.getPitch());
    }
}
