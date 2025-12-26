package com.example.prophunt.arena;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a cuboid region in the world.
 * Used for arena boundaries, lobby areas, and hunter cages.
 */
public class ArenaRegion {

    private final World world;
    private final int minX, minY, minZ;
    private final int maxX, maxY, maxZ;

    /**
     * Creates a region from two corner locations.
     *
     * @param pos1 first corner
     * @param pos2 second corner
     * @throws IllegalArgumentException if locations are in different worlds
     */
    public ArenaRegion(Location pos1, Location pos2) {
        if (pos1.getWorld() == null || pos2.getWorld() == null) {
            throw new IllegalArgumentException("Locations must have a world");
        }
        if (!pos1.getWorld().equals(pos2.getWorld())) {
            throw new IllegalArgumentException("Both locations must be in the same world");
        }

        this.world = pos1.getWorld();
        this.minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        this.minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        this.minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        this.maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        this.maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        this.maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
    }

    /**
     * Creates a region from explicit coordinates.
     *
     * @param world the world
     * @param minX minimum X
     * @param minY minimum Y
     * @param minZ minimum Z
     * @param maxX maximum X
     * @param maxY maximum Y
     * @param maxZ maximum Z
     */
    public ArenaRegion(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.world = Objects.requireNonNull(world, "World cannot be null");
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    /**
     * Gets the world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Gets the minimum corner location.
     *
     * @return min corner
     */
    public Location getMin() {
        return new Location(world, minX, minY, minZ);
    }

    /**
     * Gets the maximum corner location.
     *
     * @return max corner
     */
    public Location getMax() {
        return new Location(world, maxX, maxY, maxZ);
    }

    /**
     * Gets the center of the region.
     *
     * @return center location
     */
    public Location getCenter() {
        return new Location(world,
                (minX + maxX) / 2.0,
                (minY + maxY) / 2.0,
                (minZ + maxZ) / 2.0);
    }

    /**
     * Checks if a location is within this region.
     *
     * @param location the location to check
     * @return true if inside
     */
    public boolean contains(Location location) {
        if (location == null || !world.equals(location.getWorld())) {
            return false;
        }

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }

    /**
     * Checks if a block is within this region.
     *
     * @param block the block to check
     * @return true if inside
     */
    public boolean contains(Block block) {
        return contains(block.getLocation());
    }

    /**
     * Gets the volume of the region.
     *
     * @return volume in blocks
     */
    public int getVolume() {
        return (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
    }

    /**
     * Gets the dimensions string.
     *
     * @return dimensions as "WxHxD"
     */
    public String getDimensions() {
        return (maxX - minX + 1) + "x" + (maxY - minY + 1) + "x" + (maxZ - minZ + 1);
    }

    /**
     * Gets all blocks in this region.
     * Warning: Can be expensive for large regions.
     *
     * @return list of all blocks
     */
    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(world.getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    /**
     * Iterates over all blocks in the region without creating a list.
     *
     * @param consumer consumer for each block
     */
    public void forEachBlock(java.util.function.Consumer<Block> consumer) {
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    consumer.accept(world.getBlockAt(x, y, z));
                }
            }
        }
    }

    /**
     * Gets a random location within the region.
     *
     * @return random location
     */
    public Location getRandomLocation() {
        java.util.Random random = new java.util.Random();
        double x = minX + random.nextDouble() * (maxX - minX + 1);
        double y = minY + random.nextDouble() * (maxY - minY + 1);
        double z = minZ + random.nextDouble() * (maxZ - minZ + 1);
        return new Location(world, x, y, z);
    }

    /**
     * Saves the region to a configuration section.
     *
     * @param section the section to save to
     */
    public void save(ConfigurationSection section) {
        section.set("world", world.getName());
        section.set("min.x", minX);
        section.set("min.y", minY);
        section.set("min.z", minZ);
        section.set("max.x", maxX);
        section.set("max.y", maxY);
        section.set("max.z", maxZ);
    }

    /**
     * Loads a region from a configuration section.
     *
     * @param section the section to load from
     * @return the loaded region, or null if invalid
     */
    public static ArenaRegion load(ConfigurationSection section) {
        if (section == null) return null;

        String worldName = section.getString("world");
        if (worldName == null) return null;

        World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) return null;

        int minX = section.getInt("min.x");
        int minY = section.getInt("min.y");
        int minZ = section.getInt("min.z");
        int maxX = section.getInt("max.x");
        int maxY = section.getInt("max.y");
        int maxZ = section.getInt("max.z");

        return new ArenaRegion(world, minX, minY, minZ, maxX, maxY, maxZ);
    }

    // Getters for individual coordinates
    public int getMinX() { return minX; }
    public int getMinY() { return minY; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxY() { return maxY; }
    public int getMaxZ() { return maxZ; }

    @Override
    public String toString() {
        return "ArenaRegion{" +
                "world=" + world.getName() +
                ", min=(" + minX + "," + minY + "," + minZ + ")" +
                ", max=(" + maxX + "," + maxY + "," + maxZ + ")" +
                '}';
    }
}
