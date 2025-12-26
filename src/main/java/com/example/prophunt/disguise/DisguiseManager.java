package com.example.prophunt.disguise;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.player.PropPlayer;
import com.example.prophunt.util.ParticleUtil;
import com.example.prophunt.util.SoundUtil;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Manages all prop disguises in the plugin.
 */
public class DisguiseManager {

    private final PropHuntPlugin plugin;
    private final Map<UUID, PropDisguise> disguises;
    private BukkitTask updateTask;

    public DisguiseManager(PropHuntPlugin plugin) {
        this.plugin = plugin;
        this.disguises = new HashMap<>();
        startUpdateTask();
    }

    /**
     * Starts the position update task.
     */
    private void startUpdateTask() {
        // Update disguise positions every tick
        updateTask = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (PropDisguise disguise : disguises.values()) {
                if (disguise.isActive() && !disguise.isLocked()) {
                    disguise.updatePosition();
                }
            }
        }, 1L, 1L);
    }

    /**
     * Creates a disguise for a prop player.
     *
     * @param propPlayer the prop player
     * @param propType the prop type
     * @return the created disguise
     */
    public PropDisguise createDisguise(PropPlayer propPlayer, PropType propType) {
        // Remove existing disguise if any
        removeDisguise(propPlayer);

        PropDisguise disguise = new PropDisguise(propPlayer);
        disguise.apply(propType);
        disguises.put(propPlayer.getUuid(), disguise);

        // Effects
        Player player = propPlayer.getPlayer();
        SoundUtil.playDisguise(player);
        ParticleUtil.playDisguiseEffect(player.getLocation());

        plugin.debug("Created disguise for %s as %s",
                propPlayer.getName(), propType.getMaterial());

        return disguise;
    }

    /**
     * Gets a disguise by player.
     *
     * @param player the player
     * @return the disguise, or null
     */
    public PropDisguise getDisguise(Player player) {
        return disguises.get(player.getUniqueId());
    }

    /**
     * Gets a disguise by UUID.
     *
     * @param uuid the player's UUID
     * @return the disguise, or null
     */
    public PropDisguise getDisguise(UUID uuid) {
        return disguises.get(uuid);
    }

    /**
     * Gets a disguise by prop player.
     *
     * @param propPlayer the prop player
     * @return the disguise, or null
     */
    public PropDisguise getDisguise(PropPlayer propPlayer) {
        return disguises.get(propPlayer.getUuid());
    }

    /**
     * Checks if a player has a disguise.
     *
     * @param player the player
     * @return true if disguised
     */
    public boolean hasDisguise(Player player) {
        PropDisguise disguise = disguises.get(player.getUniqueId());
        return disguise != null && disguise.isActive();
    }

    /**
     * Removes a disguise.
     *
     * @param propPlayer the prop player
     */
    public void removeDisguise(PropPlayer propPlayer) {
        PropDisguise disguise = disguises.remove(propPlayer.getUuid());
        if (disguise != null) {
            disguise.remove();
            plugin.debug("Removed disguise for %s", propPlayer.getName());
        }
    }

    /**
     * Removes a disguise by player.
     *
     * @param player the player
     */
    public void removeDisguise(Player player) {
        PropDisguise disguise = disguises.remove(player.getUniqueId());
        if (disguise != null) {
            disguise.remove();
        }
    }

    /**
     * Removes all disguises.
     */
    public void removeAllDisguises() {
        for (PropDisguise disguise : new ArrayList<>(disguises.values())) {
            disguise.remove();
        }
        disguises.clear();
    }

    /**
     * Sets the locked state for a disguise.
     *
     * @param propPlayer the prop player
     * @param locked true to lock
     */
    public void setLocked(PropPlayer propPlayer, boolean locked) {
        PropDisguise disguise = disguises.get(propPlayer.getUuid());
        if (disguise != null) {
            disguise.setLocked(locked);

            Player player = propPlayer.getPlayer();
            if (locked) {
                SoundUtil.playLock(player);
                ParticleUtil.playLockEffect(player.getLocation());
            } else {
                SoundUtil.playUnlock(player);
            }
        }
    }

    /**
     * Toggles the locked state for a disguise.
     *
     * @param propPlayer the prop player
     * @return the new locked state
     */
    public boolean toggleLocked(PropPlayer propPlayer) {
        PropDisguise disguise = disguises.get(propPlayer.getUuid());
        if (disguise != null) {
            boolean newState = !disguise.isLocked();
            setLocked(propPlayer, newState);
            return newState;
        }
        return false;
    }

    /**
     * Sets the glowing effect for a disguise (when found).
     *
     * @param propPlayer the prop player
     * @param glowing true to glow
     */
    public void setGlowing(PropPlayer propPlayer, boolean glowing) {
        PropDisguise disguise = disguises.get(propPlayer.getUuid());
        if (disguise != null) {
            disguise.setGlowing(glowing);
        }
    }

    /**
     * Finds a prop player by their display entity.
     *
     * @param entity the BlockDisplay entity
     * @return the prop player, or null
     */
    public PropPlayer findByEntity(Entity entity) {
        if (!(entity instanceof BlockDisplay)) {
            return null;
        }

        for (PropDisguise disguise : disguises.values()) {
            if (disguise.getDisplayEntity() != null &&
                disguise.getDisplayEntity().equals(entity)) {
                return disguise.getPropPlayer();
            }
        }
        return null;
    }

    /**
     * Checks if an entity is a prop disguise.
     *
     * @param entity the entity
     * @return true if it's a disguise
     */
    public boolean isDisguiseEntity(Entity entity) {
        return findByEntity(entity) != null;
    }

    /**
     * Gets all active disguises.
     *
     * @return collection of disguises
     */
    public Collection<PropDisguise> getAllDisguises() {
        return Collections.unmodifiableCollection(disguises.values());
    }

    /**
     * Gets the count of active disguises.
     *
     * @return disguise count
     */
    public int getDisguiseCount() {
        return disguises.size();
    }

    /**
     * Cleans up orphaned display entities.
     */
    public void cleanup() {
        // Remove any display entities that don't belong to active disguises
        Set<BlockDisplay> validEntities = new HashSet<>();
        for (PropDisguise disguise : disguises.values()) {
            if (disguise.getDisplayEntity() != null) {
                validEntities.add(disguise.getDisplayEntity());
            }
        }

        // This would need to iterate through worlds to find orphaned entities
        // Implementation depends on how thorough the cleanup needs to be
    }

    /**
     * Shuts down the disguise manager.
     */
    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        removeAllDisguises();
    }
}
