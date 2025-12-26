package com.example.prophunt.mechanics;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.game.Game;
import com.example.prophunt.game.GameState;
import com.example.prophunt.player.PropPlayer;
import com.example.prophunt.util.MessageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages late-game mechanics that help hunters find remaining props.
 */
public class LateGameManager {

    private final PropHuntPlugin plugin;
    private final Map<String, LateGameState> gameStates;

    // Late game thresholds (seconds remaining)
    private static final int PHASE_1_TIME = 120; // 2 minutes - hint particles
    private static final int PHASE_2_TIME = 60;  // 1 minute - more frequent hints
    private static final int PHASE_3_TIME = 30;  // 30 seconds - constant glow

    public LateGameManager(PropHuntPlugin plugin) {
        this.plugin = plugin;
        this.gameStates = new HashMap<>();
    }

    /**
     * Starts monitoring a game for late-game mechanics.
     */
    public void startMonitoring(Game game) {
        String arenaName = game.getArena().getName();
        LateGameState state = new LateGameState();
        gameStates.put(arenaName, state);

        // Schedule periodic check
        state.task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            checkLateGame(game);
        }, 20L, 20L); // Check every second
    }

    /**
     * Stops monitoring a game.
     */
    public void stopMonitoring(Game game) {
        String arenaName = game.getArena().getName();
        LateGameState state = gameStates.remove(arenaName);
        if (state != null && state.task != null) {
            state.task.cancel();
        }
    }

    /**
     * Checks and applies late-game mechanics.
     */
    private void checkLateGame(Game game) {
        if (game.getState() != GameState.HUNTING) {
            stopMonitoring(game);
            return;
        }

        int timeRemaining = game.getTimeRemaining();
        LateGameState state = gameStates.get(game.getArena().getName());
        if (state == null) return;

        // Phase 3: Constant glow (last 30 seconds)
        if (timeRemaining <= PHASE_3_TIME && !state.phase3Active) {
            activatePhase3(game, state);
        }
        // Phase 2: Frequent hints (last minute)
        else if (timeRemaining <= PHASE_2_TIME && !state.phase2Active) {
            activatePhase2(game, state);
        }
        // Phase 1: Occasional hints (last 2 minutes)
        else if (timeRemaining <= PHASE_1_TIME && !state.phase1Active) {
            activatePhase1(game, state);
        }

        // Run phase-specific effects
        if (state.phase3Active) {
            runPhase3Effects(game);
        } else if (state.phase2Active && game.getTimeRemaining() % 5 == 0) {
            runPhase2Effects(game);
        } else if (state.phase1Active && game.getTimeRemaining() % 15 == 0) {
            runPhase1Effects(game);
        }
    }

    /**
     * Activates Phase 1: Hint particles every 15 seconds.
     */
    private void activatePhase1(Game game, LateGameState state) {
        state.phase1Active = true;

        game.broadcast(MessageUtil.colorize(
                "&6&l[!] &eLate game! &7Hunters are getting hints..."));

        plugin.debug("Late game phase 1 activated for %s", game.getArena().getName());
    }

    /**
     * Activates Phase 2: Frequent hint particles.
     */
    private void activatePhase2(Game game, LateGameState state) {
        state.phase2Active = true;

        game.broadcast(MessageUtil.colorize(
                "&c&l[!] &eFinal minute! &7Props are becoming visible..."));

        // Give hunters speed boost
        for (Player hunter : game.getTeamManager().getAliveHunterPlayers()) {
            hunter.addPotionEffect(new PotionEffect(
                    PotionEffectType.SPEED, 1200, 0, false, false));
        }

        plugin.debug("Late game phase 2 activated for %s", game.getArena().getName());
    }

    /**
     * Activates Phase 3: Constant glow on props.
     */
    private void activatePhase3(Game game, LateGameState state) {
        state.phase3Active = true;

        game.broadcast(MessageUtil.colorize(
                "&4&l[!] &c30 SECONDS! &7All props are now glowing!"));

        // Make all props glow
        for (PropPlayer prop : game.getTeamManager().getAliveProps()) {
            plugin.getDisguiseManager().setGlowing(prop, true);
            plugin.getMessageUtil().send(prop.getPlayer(), "prop.late-game-glow");
        }

        // Play intense sound
        for (Player player : game.getPlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f, 1.5f);
        }

        plugin.debug("Late game phase 3 activated for %s", game.getArena().getName());
    }

    /**
     * Runs Phase 1 effects (hint particles).
     */
    private void runPhase1Effects(Game game) {
        for (PropPlayer prop : game.getTeamManager().getAliveProps()) {
            Location loc = prop.getLocation().clone().add(0, 2, 0);

            // Spawn particles visible to hunters
            for (Player hunter : game.getTeamManager().getAliveHunterPlayers()) {
                if (hunter.getLocation().distance(loc) < 30) {
                    hunter.spawnParticle(Particle.END_ROD, loc, 3, 0.5, 0.5, 0.5, 0.01);
                }
            }
        }
    }

    /**
     * Runs Phase 2 effects (frequent hint particles + sound).
     */
    private void runPhase2Effects(Game game) {
        for (PropPlayer prop : game.getTeamManager().getAliveProps()) {
            Location loc = prop.getLocation().clone().add(0, 1.5, 0);

            // More visible particles
            loc.getWorld().spawnParticle(Particle.SOUL, loc, 5, 0.3, 0.5, 0.3, 0.02);

            // Heartbeat sound to nearby hunters
            for (Player hunter : game.getTeamManager().getAliveHunterPlayers()) {
                if (hunter.getLocation().distance(loc) < 20) {
                    hunter.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BASS, 0.3f, 0.5f);
                }
            }
        }
    }

    /**
     * Runs Phase 3 effects (constant visual).
     */
    private void runPhase3Effects(Game game) {
        // Glowing is already applied, just add some ambient effects
        for (PropPlayer prop : game.getTeamManager().getAliveProps()) {
            Location loc = prop.getLocation().clone().add(0, 2, 0);
            loc.getWorld().spawnParticle(Particle.FLAME, loc, 2, 0.3, 0.3, 0.3, 0.01);
        }
    }

    /**
     * Shuts down the manager.
     */
    public void shutdown() {
        for (LateGameState state : gameStates.values()) {
            if (state.task != null) {
                state.task.cancel();
            }
        }
        gameStates.clear();
    }

    /**
     * Tracks late game state for a game.
     */
    private static class LateGameState {
        BukkitTask task;
        boolean phase1Active;
        boolean phase2Active;
        boolean phase3Active;
    }
}
