package com.example.prophunt.disguise;

import com.example.prophunt.player.PropPlayer;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * Represents a prop disguise using BlockDisplay entities.
 */
public class PropDisguise {

    private final PropPlayer propPlayer;
    private PropType propType;
    private BlockDisplay displayEntity;

    private boolean locked;
    private float rotation;
    private float transparency; // 0 = opaque, 1 = invisible

    // Constants
    private static final float GHOST_TRANSPARENCY = 0.5f;
    private static final float SOLID_TRANSPARENCY = 0f;

    /**
     * Creates a new prop disguise.
     *
     * @param propPlayer the prop player
     */
    public PropDisguise(PropPlayer propPlayer) {
        this.propPlayer = propPlayer;
        this.locked = false;
        this.rotation = 0;
        this.transparency = SOLID_TRANSPARENCY;
    }

    /**
     * Applies the disguise to the player.
     *
     * @param propType the prop type to disguise as
     */
    public void apply(PropType propType) {
        this.propType = propType;

        Player player = propPlayer.getPlayer();

        // Make player invisible
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                Integer.MAX_VALUE,
                0,
                false, false, false));

        // Spawn the BlockDisplay entity
        spawnDisplayEntity();

        // Set player's health based on prop size
        player.setMaxHealth(propType.getHealth());
        player.setHealth(propType.getHealth());

        propPlayer.setDisguise(propType);
    }

    /**
     * Spawns the BlockDisplay entity.
     */
    private void spawnDisplayEntity() {
        if (displayEntity != null) {
            displayEntity.remove();
        }

        Player player = propPlayer.getPlayer();
        Location loc = player.getLocation();

        // Spawn BlockDisplay at player location
        displayEntity = (BlockDisplay) player.getWorld().spawnEntity(
                loc, EntityType.BLOCK_DISPLAY);

        // Set the block data
        displayEntity.setBlock(propType.getBlockData());

        // Configure display properties
        displayEntity.setPersistent(false);
        displayEntity.setGlowing(false);

        // Set transformation for proper size and positioning
        updateTransformation();

        // Hide the entity from the player wearing it
        player.hideEntity(propPlayer.getGame().getPlugin(), displayEntity);
    }

    /**
     * Updates the entity transformation (size, rotation, position).
     */
    private void updateTransformation() {
        if (displayEntity == null || !displayEntity.isValid()) return;

        // Calculate scale based on prop size
        float scale = getScaleForSize(propType.getSize());

        // Create transformation
        // Translation puts the block at feet level, centered
        Vector3f translation = new Vector3f(
                -scale / 2,  // Center X
                0,           // At feet level
                -scale / 2   // Center Z
        );

        // Rotation around Y axis
        AxisAngle4f rotationAxis = new AxisAngle4f(
                (float) Math.toRadians(rotation),
                0, 1, 0
        );

        // Scale
        Vector3f scaleVec = new Vector3f(scale, scale, scale);

        Transformation transformation = new Transformation(
                translation,
                rotationAxis,
                scaleVec,
                new AxisAngle4f()
        );

        displayEntity.setTransformation(transformation);

        // Set interpolation for smooth movement
        displayEntity.setInterpolationDuration(2);
        displayEntity.setInterpolationDelay(0);
    }

    /**
     * Gets the scale for a prop size.
     */
    private float getScaleForSize(PropSize size) {
        return switch (size) {
            case SMALL -> 0.5f;
            case MEDIUM -> 0.75f;
            case LARGE -> 1.0f;
        };
    }

    /**
     * Updates the disguise position to follow the player.
     */
    public void updatePosition() {
        if (displayEntity == null || !displayEntity.isValid()) return;

        Player player = propPlayer.getPlayer();
        Location playerLoc = player.getLocation();

        // Teleport display entity to player
        displayEntity.teleport(playerLoc);

        // Update transformation for any rotation changes
        updateTransformation();
    }

    /**
     * Sets the locked state.
     *
     * @param locked true to lock in place
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
        propPlayer.setLocked(locked);

        // Update transparency
        setTransparency(locked ? SOLID_TRANSPARENCY : GHOST_TRANSPARENCY);
    }

    /**
     * Checks if the disguise is locked.
     *
     * @return true if locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Sets the rotation.
     *
     * @param rotation rotation in degrees
     */
    public void setRotation(float rotation) {
        this.rotation = rotation % 360;
        updateTransformation();
    }

    /**
     * Gets the rotation.
     *
     * @return rotation in degrees
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Rotates by an amount.
     *
     * @param degrees degrees to rotate
     */
    public void rotate(float degrees) {
        setRotation(rotation + degrees);
    }

    /**
     * Sets the transparency level.
     *
     * @param transparency 0 = opaque, 1 = invisible
     */
    public void setTransparency(float transparency) {
        this.transparency = Math.max(0, Math.min(1, transparency));

        // Note: BlockDisplay doesn't support transparency directly
        // We use glowing/brightness as a visual indicator instead
        // In a full implementation, you might use resource packs or alternative methods
    }

    /**
     * Sets ghost mode (semi-transparent when moving).
     *
     * @param ghost true for ghost mode
     */
    public void setGhostMode(boolean ghost) {
        setTransparency(ghost ? GHOST_TRANSPARENCY : SOLID_TRANSPARENCY);
    }

    /**
     * Sets the glowing effect (when found by hunter).
     *
     * @param glowing true to glow
     */
    public void setGlowing(boolean glowing) {
        if (displayEntity != null && displayEntity.isValid()) {
            displayEntity.setGlowing(glowing);
        }
    }

    /**
     * Removes the disguise.
     */
    public void remove() {
        Player player = propPlayer.getPlayer();

        // Remove invisibility
        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        // Remove display entity
        if (displayEntity != null) {
            displayEntity.remove();
            displayEntity = null;
        }

        // Reset health
        player.setMaxHealth(20);
        player.setHealth(20);
    }

    /**
     * Checks if the disguise is active.
     *
     * @return true if active
     */
    public boolean isActive() {
        return displayEntity != null && displayEntity.isValid();
    }

    /**
     * Gets the prop type.
     *
     * @return the prop type
     */
    public PropType getPropType() {
        return propType;
    }

    /**
     * Gets the display entity.
     *
     * @return the BlockDisplay entity
     */
    public BlockDisplay getDisplayEntity() {
        return displayEntity;
    }

    /**
     * Gets the prop player.
     *
     * @return the prop player
     */
    public PropPlayer getPropPlayer() {
        return propPlayer;
    }

    /**
     * Gets the location of the disguise.
     *
     * @return the location
     */
    public Location getLocation() {
        if (displayEntity != null && displayEntity.isValid()) {
            return displayEntity.getLocation();
        }
        return propPlayer.getLocation();
    }
}
