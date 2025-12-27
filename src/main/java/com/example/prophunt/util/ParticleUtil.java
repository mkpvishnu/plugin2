package com.example.prophunt.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * Utility class for particle effects.
 */
public final class ParticleUtil {

    private ParticleUtil() {
        // Utility class
    }

    // ===== Prop Effects =====

    /**
     * Plays the prop disguise effect.
     *
     * @param location the location
     */
    public static void playDisguiseEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.ENCHANT, location.clone().add(0, 1, 0),
                30, 0.5, 0.5, 0.5, 0.5);
        world.spawnParticle(Particle.PORTAL, location.clone().add(0, 0.5, 0),
                20, 0.3, 0.3, 0.3, 0.2);
    }

    /**
     * Plays the prop lock effect.
     *
     * @param location the location
     */
    public static void playLockEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.CRIT, location.clone().add(0, 0.5, 0),
                10, 0.2, 0.2, 0.2, 0.05);
    }

    /**
     * Plays the prop found effect.
     *
     * @param location the location
     */
    public static void playFoundEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.ANGRY_VILLAGER, location.clone().add(0, 1.5, 0),
                5, 0.3, 0.3, 0.3, 0);
    }

    /**
     * Plays the prop killed effect.
     *
     * @param location the location
     */
    public static void playKillEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.EXPLOSION, location.clone().add(0, 0.5, 0),
                1, 0, 0, 0, 0);
        world.spawnParticle(Particle.ITEM, location.clone().add(0, 0.5, 0),
                30, 0.3, 0.3, 0.3, 0.1,
                new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE));
    }

    /**
     * Plays the ghost mode effect (prop moving).
     *
     * @param location the location
     */
    public static void playGhostEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.SOUL, location.clone().add(0, 0.2, 0),
                2, 0.1, 0.1, 0.1, 0.01);
    }

    // ===== Hunter Effects =====

    /**
     * Plays the hunter miss effect.
     *
     * @param location the location
     */
    public static void playMissEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.SMOKE, location,
                10, 0.2, 0.2, 0.2, 0.02);
    }

    /**
     * Plays the hunter hit effect.
     *
     * @param location the location
     */
    public static void playHitEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.DAMAGE_INDICATOR, location.clone().add(0, 0.5, 0),
                5, 0.2, 0.2, 0.2, 0);
    }

    // ===== Game Phase Effects =====

    /**
     * Plays the game start effect.
     *
     * @param location the location
     */
    public static void playGameStartEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.FIREWORK, location.clone().add(0, 2, 0),
                50, 1, 1, 1, 0.2);
    }

    /**
     * Plays the hunters released effect.
     *
     * @param location the location
     */
    public static void playHuntersReleasedEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.FLAME, location.clone().add(0, 1, 0),
                50, 1, 1, 1, 0.1);
        world.spawnParticle(Particle.LAVA, location.clone().add(0, 0.5, 0),
                20, 0.5, 0.5, 0.5, 0);
    }

    /**
     * Plays the victory celebration effect.
     *
     * @param location the location
     */
    public static void playVictoryEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.TOTEM_OF_UNDYING, location.clone().add(0, 1, 0),
                100, 0.5, 1, 0.5, 0.5);
    }

    // ===== Taunt Effect =====

    /**
     * Plays the taunt effect.
     *
     * @param location the location
     */
    public static void playTauntEffect(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        world.spawnParticle(Particle.NOTE, location.clone().add(0, 1.5, 0),
                5, 0.3, 0.3, 0.3, 0);
    }

    // ===== Utility Methods =====

    /**
     * Creates a circle of particles.
     *
     * @param center the center location
     * @param particle the particle type
     * @param radius the radius
     * @param points number of points in the circle
     */
    public static void spawnCircle(Location center, Particle particle,
                                    double radius, int points) {
        World world = center.getWorld();
        if (world == null) return;

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            world.spawnParticle(particle, x, center.getY(), z, 1, 0, 0, 0, 0);
        }
    }

    /**
     * Creates a vertical line of particles.
     *
     * @param base the base location
     * @param particle the particle type
     * @param height the height
     * @param density particles per block
     */
    public static void spawnVerticalLine(Location base, Particle particle,
                                          double height, double density) {
        World world = base.getWorld();
        if (world == null) return;

        for (double y = 0; y < height; y += 1.0 / density) {
            world.spawnParticle(particle, base.getX(), base.getY() + y, base.getZ(),
                    1, 0, 0, 0, 0);
        }
    }

    /**
     * Spawns colored dust particles.
     *
     * @param location the location
     * @param color the color
     * @param count particle count
     * @param spread the spread
     */
    public static void spawnColoredDust(Location location, Color color,
                                         int count, double spread) {
        World world = location.getWorld();
        if (world == null) return;

        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        world.spawnParticle(Particle.DUST, location, count, spread, spread, spread, 0, dustOptions);
    }

    /**
     * Spawns particles visible only to specific players.
     *
     * @param players the players who can see the particles
     * @param particle the particle type
     * @param location the location
     * @param count the count
     * @param offsetX X offset
     * @param offsetY Y offset
     * @param offsetZ Z offset
     * @param speed the speed
     */
    public static void spawnForPlayers(Collection<? extends Player> players,
                                        Particle particle, Location location,
                                        int count, double offsetX, double offsetY,
                                        double offsetZ, double speed) {
        for (Player player : players) {
            player.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed);
        }
    }

    /**
     * Creates a team-colored particle effect.
     *
     * @param location the location
     * @param isProps true for green (props), false for red (hunters)
     */
    public static void spawnTeamParticles(Location location, boolean isProps) {
        Color color = isProps ? Color.GREEN : Color.RED;
        spawnColoredDust(location.clone().add(0, 1, 0), color, 15, 0.3);
    }
}
