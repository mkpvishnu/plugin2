package com.example.prophunt.mechanics;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.game.Game;
import com.example.prophunt.game.GameState;
import com.example.prophunt.player.PropPlayer;
import com.example.prophunt.util.MessageUtil;
import com.example.prophunt.util.ParticleUtil;
import com.example.prophunt.util.SoundUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages prop taunting mechanics.
 */
public class TauntManager {

    private final PropHuntPlugin plugin;
    private final Map<UUID, Long> lastVoluntaryTaunt;
    private final Map<UUID, BukkitTask> forcedTauntTasks;

    // Taunt sounds to play
    private static final Sound[] TAUNT_SOUNDS = {
            Sound.ENTITY_VILLAGER_YES,
            Sound.ENTITY_VILLAGER_NO,
            Sound.ENTITY_VILLAGER_CELEBRATE,
            Sound.ENTITY_PARROT_IMITATE_ZOMBIE,
            Sound.ENTITY_CAT_AMBIENT,
            Sound.ENTITY_WOLF_WHINE,
            Sound.ENTITY_CHICKEN_AMBIENT,
            Sound.ENTITY_COW_AMBIENT,
            Sound.ENTITY_PIG_AMBIENT,
            Sound.ENTITY_SHEEP_AMBIENT,
            Sound.ENTITY_DONKEY_AMBIENT,
            Sound.ENTITY_GOAT_SCREAMING_AMBIENT,
            Sound.BLOCK_NOTE_BLOCK_PLING,
            Sound.BLOCK_NOTE_BLOCK_BELL,
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP
    };

    public TauntManager(PropHuntPlugin plugin) {
        this.plugin = plugin;
        this.lastVoluntaryTaunt = new HashMap<>();
        this.forcedTauntTasks = new HashMap<>();
    }

    /**
     * Starts forced taunting for a game.
     */
    public void startForcedTaunts(Game game) {
        int interval = game.getSettings().getForcedTauntInterval();
        if (interval <= 0) return;

        // Schedule random forced taunts
        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (game.getState() != GameState.HUNTING) {
                stopForcedTaunts(game);
                return;
            }

            // Pick a random alive prop
            List<PropPlayer> aliveProps = game.getTeamManager().getAliveProps();
            if (aliveProps.isEmpty()) return;

            PropPlayer prop = aliveProps.get(ThreadLocalRandom.current().nextInt(aliveProps.size()));
            forceTaunt(prop);

        }, interval * 20L, interval * 20L);

        // Store task for cleanup
        forcedTauntTasks.put(game.getArena().getName().hashCode() + UUID.randomUUID(), task);
    }

    /**
     * Stops forced taunting for a game.
     */
    public void stopForcedTaunts(Game game) {
        // Cancel all tasks (simplified - in production, track per-game)
        forcedTauntTasks.values().forEach(BukkitTask::cancel);
        forcedTauntTasks.clear();
    }

    /**
     * Forces a prop to taunt.
     */
    public void forceTaunt(PropPlayer prop) {
        Player player = prop.getPlayer();

        // Play taunt effect
        playTauntEffect(player.getLocation());

        // Message to prop
        plugin.getMessageUtil().send(player, "prop.forced-taunt");

        // Message to hunters
        prop.getGame().broadcastToHunters(
                MessageUtil.colorize("&e[!] &7A prop just taunted nearby!"));

        plugin.debug("Forced taunt for prop %s", player.getName());
    }

    /**
     * Handles a voluntary taunt request.
     */
    public boolean voluntaryTaunt(PropPlayer prop) {
        Player player = prop.getPlayer();
        Game game = prop.getGame();

        if (game == null || game.getState() != GameState.HUNTING) {
            return false;
        }

        // Check cooldown
        int cooldown = game.getSettings().getVoluntaryTauntCooldown();
        long lastTaunt = lastVoluntaryTaunt.getOrDefault(player.getUniqueId(), 0L);
        long elapsed = (System.currentTimeMillis() - lastTaunt) / 1000;

        if (elapsed < cooldown) {
            long remaining = cooldown - elapsed;
            plugin.getMessageUtil().send(player, "prop.taunt-cooldown",
                    "seconds", String.valueOf(remaining));
            return false;
        }

        // Record taunt
        lastVoluntaryTaunt.put(player.getUniqueId(), System.currentTimeMillis());

        // Play taunt effect
        playTauntEffect(player.getLocation());

        // Award points for risky taunt
        prop.addPoints(15);

        // Update stats
        plugin.getStatsManager().getStats(player).incrementSuccessfulTaunts();

        plugin.getMessageUtil().send(player, "prop.taunt-success");

        // Message to hunters
        game.broadcastToHunters(
                MessageUtil.colorize("&e[!] &7A prop is taunting! Listen carefully..."));

        plugin.debug("Voluntary taunt by prop %s", player.getName());

        return true;
    }

    /**
     * Plays a taunt effect at a location.
     */
    private void playTauntEffect(Location loc) {
        // Play random sound
        Sound sound = TAUNT_SOUNDS[ThreadLocalRandom.current().nextInt(TAUNT_SOUNDS.length)];
        loc.getWorld().playSound(loc, sound, 2.0f, 1.0f);

        // Play particles
        loc.getWorld().spawnParticle(
                Particle.NOTE,
                loc.clone().add(0, 2, 0),
                5, 0.5, 0.5, 0.5, 0.1);

        loc.getWorld().spawnParticle(
                Particle.HAPPY_VILLAGER,
                loc.clone().add(0, 1, 0),
                10, 0.5, 0.5, 0.5, 0);
    }

    /**
     * Clears taunt data for a player.
     */
    public void clearPlayer(UUID uuid) {
        lastVoluntaryTaunt.remove(uuid);
    }

    /**
     * Shuts down the taunt manager.
     */
    public void shutdown() {
        forcedTauntTasks.values().forEach(BukkitTask::cancel);
        forcedTauntTasks.clear();
        lastVoluntaryTaunt.clear();
    }
}
