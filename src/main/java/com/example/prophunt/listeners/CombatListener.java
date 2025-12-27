package com.example.prophunt.listeners;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.api.events.PropFoundEvent;
import com.example.prophunt.api.events.PropKilledEvent;
import com.example.prophunt.config.GameSettings;
import com.example.prophunt.disguise.DisguiseManager;
import com.example.prophunt.disguise.PropDisguise;
import com.example.prophunt.game.Game;
import com.example.prophunt.game.GameState;
import com.example.prophunt.player.GamePlayer;
import com.example.prophunt.player.HunterPlayer;
import com.example.prophunt.player.PropPlayer;
import com.example.prophunt.team.Team;
import com.example.prophunt.util.ParticleUtil;
import com.example.prophunt.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Handles combat mechanics for PropHunt.
 */
public class CombatListener implements Listener {

    private final PropHuntPlugin plugin;

    public CombatListener(PropHuntPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles hunters hitting blocks (checking for props).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Check if player is a hunter in a game
        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp == null || !(gp instanceof HunterPlayer hunter)) return;

        Game game = gp.getGame();
        if (game == null || game.getState() != GameState.HUNTING) return;

        // Check cooldown
        if (!hunter.canAttack()) {
            return;
        }

        // Record the attack
        hunter.recordAttack();

        // Check if the block is a prop disguise
        PropPlayer prop = findPropAtLocation(block.getLocation(), game);

        if (prop != null) {
            // Hit a prop!
            handlePropHit(hunter, prop, game);
        } else {
            // Missed - hit a real block
            handleMiss(hunter, block.getLocation(), game);
        }

        event.setCancelled(true);
    }

    /**
     * Handles hitting a BlockDisplay entity directly.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // Check if this is a disguise entity
        PropPlayer prop = plugin.getDisguiseManager().findByEntity(entity);
        if (prop == null) return;

        // Check if player is a hunter in the same game
        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp == null || !(gp instanceof HunterPlayer hunter)) return;

        Game game = gp.getGame();
        if (game == null || game.getState() != GameState.HUNTING) return;
        if (!game.equals(prop.getGame())) return;

        if (!hunter.canAttack()) return;

        hunter.recordAttack();
        handlePropHit(hunter, prop, game);
        event.setCancelled(true);
    }

    /**
     * Handles direct entity damage (for hitting disguise entities).
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        Entity entity = event.getEntity();
        PropPlayer prop = plugin.getDisguiseManager().findByEntity(entity);
        if (prop == null) return;

        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp == null || !(gp instanceof HunterPlayer hunter)) return;

        Game game = gp.getGame();
        if (game == null || game.getState() != GameState.HUNTING) return;

        event.setCancelled(true);

        if (!hunter.canAttack()) return;

        hunter.recordAttack();
        handlePropHit(hunter, prop, game);
    }

    /**
     * Prevents damage to game players except from game mechanics.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        GamePlayer gp = plugin.getPlayerManager().getPlayer(player);
        if (gp == null) return;

        Game game = gp.getGame();
        if (game == null) return;

        // Only allow game-controlled damage
        if (event.getCause() != EntityDamageEvent.DamageCause.CUSTOM) {
            event.setCancelled(true);
        }
    }

    /**
     * Finds a prop at a specific block location.
     */
    private PropPlayer findPropAtLocation(Location blockLoc, Game game) {
        DisguiseManager dm = plugin.getDisguiseManager();

        for (PropPlayer prop : game.getTeamManager().getAliveProps()) {
            PropDisguise disguise = dm.getDisguise(prop);
            if (disguise == null || !disguise.isActive()) continue;

            // Check if prop is at this block location
            Location propLoc = prop.getLocation();
            if (propLoc.getBlockX() == blockLoc.getBlockX() &&
                propLoc.getBlockY() == blockLoc.getBlockY() &&
                propLoc.getBlockZ() == blockLoc.getBlockZ()) {
                return prop;
            }
        }

        return null;
    }

    /**
     * Handles a hunter hitting a prop.
     */
    private void handlePropHit(HunterPlayer hunter, PropPlayer prop, Game game) {
        Player hunterPlayer = hunter.getPlayer();
        Player propPlayer = prop.getPlayer();
        GameSettings settings = game.getSettings();

        double damage = 4.0; // 2 hearts

        // Fire prop found event (cancellable)
        PropFoundEvent foundEvent = new PropFoundEvent(game, hunter, prop, damage);
        Bukkit.getPluginManager().callEvent(foundEvent);
        if (foundEvent.isCancelled()) {
            return;
        }
        damage = foundEvent.getDamage();

        // First hit on this prop?
        boolean firstHit = !prop.isRevealed();

        // Reveal the prop
        prop.reveal();
        plugin.getDisguiseManager().setGlowing(prop, true);

        // Effects
        SoundUtil.playHunterHit(hunterPlayer);
        SoundUtil.playPropFound(propPlayer);
        ParticleUtil.playFoundEffect(propPlayer.getLocation());

        // Give prop speed boost to escape
        propPlayer.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.SPEED, 60, 1, false, false));

        // Deal damage to prop (using damage from event which can be modified)
        propPlayer.damage(0.01); // Trigger damage animation
        propPlayer.setHealth(Math.max(0, propPlayer.getHealth() - damage));

        // Heal hunter
        double healAmount = settings.getMissPenalty(); // Use same amount
        hunterPlayer.setHealth(Math.min(hunterPlayer.getMaxHealth(),
                hunterPlayer.getHealth() + healAmount));

        if (firstHit) {
            hunter.recordPropFound();

            // First blood?
            if (!game.getTeamManager().isFirstBloodAwarded()) {
                game.getTeamManager().awardFirstBlood(hunter);
                hunter.addPoints(25);
                game.broadcastRawMessage("&6&lFIRST BLOOD! &e" + hunter.getName() +
                        " &7found the first prop!");
            }

            hunter.addPoints(25);
            plugin.getMessageUtil().send(hunterPlayer, "hunter.hit-prop");
        }

        // Check if prop is dead
        if (propPlayer.getHealth() <= 0) {
            killProp(hunter, prop, game);
        } else {
            plugin.getMessageUtil().send(propPlayer, "prop.found");

            // Schedule glow removal if they escape
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (prop.canHideAgain(10)) { // 10 seconds to escape
                    prop.hide();
                    plugin.getDisguiseManager().setGlowing(prop, false);
                    plugin.getMessageUtil().send(propPlayer, "prop.escaped");
                    prop.addPoints(20);
                }
            }, 200L); // 10 seconds
        }
    }

    /**
     * Handles killing a prop.
     */
    private void killProp(HunterPlayer hunter, PropPlayer prop, Game game) {
        Player hunterPlayer = hunter.getPlayer();
        Player propPlayer = prop.getPlayer();

        // Fire prop killed event
        PropKilledEvent killedEvent = new PropKilledEvent(game, hunter, prop);
        Bukkit.getPluginManager().callEvent(killedEvent);

        // Remove disguise
        plugin.getDisguiseManager().removeDisguise(prop);

        // Effects
        SoundUtil.playHunterKill(hunterPlayer);
        SoundUtil.playPropKilled(propPlayer.getLocation());
        ParticleUtil.playKillEffect(propPlayer.getLocation());

        // Update stats
        hunter.recordPropKill();
        hunter.addPoints(50);

        // Eliminate prop
        game.getTeamManager().eliminateProp(prop);

        // Messages
        plugin.getMessageUtil().send(hunterPlayer, "hunter.kill", "player", prop.getName());
        plugin.getMessageUtil().sendTitle(propPlayer,
                "&c&lELIMINATED!",
                "&7You survived for " + (prop.getTimeInGameSeconds() / 60) + "m " +
                        (prop.getTimeInGameSeconds() % 60) + "s");

        game.broadcastRawMessage("&c" + prop.getName() + " &7was eliminated by &c" + hunter.getName());

        // Check game end
        game.checkGameEnd();
    }

    /**
     * Handles a hunter missing (hitting a real block).
     */
    private void handleMiss(HunterPlayer hunter, Location blockLoc, Game game) {
        Player player = hunter.getPlayer();
        GameSettings settings = game.getSettings();

        // Damage the hunter
        double damage = settings.getMissPenalty();
        player.damage(0.01); // Trigger damage animation
        player.setHealth(Math.max(0, player.getHealth() - damage));

        // Record and effects
        hunter.recordWrongHit();
        SoundUtil.playHunterMiss(player);
        ParticleUtil.playMissEffect(blockLoc.add(0.5, 0.5, 0.5));

        plugin.getMessageUtil().send(player, "hunter.hit-wrong",
                "damage", String.valueOf((int) (damage / 2)));

        // Check if hunter died
        if (player.getHealth() <= 0) {
            killHunter(hunter, game);
        }
    }

    /**
     * Handles a hunter dying from too many misses.
     */
    private void killHunter(HunterPlayer hunter, Game game) {
        Player player = hunter.getPlayer();

        // Eliminate hunter
        game.getTeamManager().eliminateHunter(hunter);

        // Message
        game.broadcastRawMessage("&c" + hunter.getName() +
                " &7eliminated themselves with wrong hits!");

        plugin.getMessageUtil().sendTitle(player,
                "&c&lELIMINATED!",
                "&7Too many wrong hits!");

        // Check game end
        game.checkGameEnd();
    }
}
