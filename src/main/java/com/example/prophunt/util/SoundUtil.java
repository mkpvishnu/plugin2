package com.example.prophunt.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Random;

/**
 * Utility class for playing sounds.
 */
public final class SoundUtil {

    private static final Random RANDOM = new Random();

    private SoundUtil() {
        // Utility class
    }

    // ===== Game Sound Effects =====

    /**
     * Plays the game start sound.
     *
     * @param player the player
     */
    public static void playGameStart(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL,
                SoundCategory.MASTER, 0.8f, 1.2f);
    }

    /**
     * Plays the countdown tick sound.
     *
     * @param player the player
     */
    public static void playCountdownTick(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT,
                SoundCategory.MASTER, 1f, 1f);
    }

    /**
     * Plays the countdown final tick sound.
     *
     * @param player the player
     */
    public static void playCountdownFinal(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING,
                SoundCategory.MASTER, 1f, 2f);
    }

    /**
     * Plays the hunters released sound.
     *
     * @param player the player
     */
    public static void playHuntersReleased(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN,
                SoundCategory.MASTER, 0.7f, 1.5f);
    }

    /**
     * Plays the game end sound for winners.
     *
     * @param player the player
     */
    public static void playVictory(Player player) {
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE,
                SoundCategory.MASTER, 1f, 1f);
    }

    /**
     * Plays the game end sound for losers.
     *
     * @param player the player
     */
    public static void playDefeat(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH,
                SoundCategory.MASTER, 0.5f, 0.5f);
    }

    // ===== Prop Sound Effects =====

    /**
     * Plays the prop disguise sound.
     *
     * @param player the player
     */
    public static void playDisguise(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.PLAYERS, 1f, 1.2f);
    }

    /**
     * Plays the prop lock sound.
     *
     * @param player the player
     */
    public static void playLock(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_CLOSE,
                SoundCategory.PLAYERS, 0.5f, 1.5f);
    }

    /**
     * Plays the prop unlock sound.
     *
     * @param player the player
     */
    public static void playUnlock(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_TRAPDOOR_OPEN,
                SoundCategory.PLAYERS, 0.5f, 1.5f);
    }

    /**
     * Plays the prop found sound.
     *
     * @param player the player
     */
    public static void playPropFound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_HURT,
                SoundCategory.PLAYERS, 1f, 0.8f);
    }

    /**
     * Plays the prop killed sound at a location.
     *
     * @param location the location
     */
    public static void playPropKilled(Location location) {
        if (location.getWorld() == null) return;
        location.getWorld().playSound(location, Sound.ENTITY_ITEM_BREAK,
                SoundCategory.PLAYERS, 1f, 0.5f);
    }

    // ===== Hunter Sound Effects =====

    /**
     * Plays the hunter hit (successful) sound.
     *
     * @param player the player
     */
    public static void playHunterHit(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.PLAYERS, 1f, 1f);
    }

    /**
     * Plays the hunter miss sound.
     *
     * @param player the player
     */
    public static void playHunterMiss(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO,
                SoundCategory.PLAYERS, 0.8f, 1f);
    }

    /**
     * Plays the hunter kill sound.
     *
     * @param player the player
     */
    public static void playHunterKill(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,
                SoundCategory.PLAYERS, 1f, 1.5f);
    }

    // ===== Taunt Sounds =====

    /** Available taunt sounds */
    private static final Sound[] TAUNT_SOUNDS = {
            Sound.ENTITY_CHICKEN_AMBIENT,
            Sound.ENTITY_PIG_AMBIENT,
            Sound.ENTITY_COW_AMBIENT,
            Sound.ENTITY_CAT_AMBIENT,
            Sound.ENTITY_WOLF_AMBIENT,
            Sound.ENTITY_SHEEP_AMBIENT,
            Sound.ENTITY_VILLAGER_AMBIENT,
            Sound.BLOCK_CHEST_OPEN,
            Sound.BLOCK_WOODEN_DOOR_OPEN,
            Sound.ENTITY_PLAYER_BURP,
            Sound.ENTITY_EXPERIENCE_ORB_PICKUP,
            Sound.BLOCK_NOTE_BLOCK_BELL,
            Sound.BLOCK_ANVIL_LAND
    };

    /**
     * Gets a random taunt sound.
     *
     * @return random taunt sound
     */
    public static Sound getRandomTauntSound() {
        return TAUNT_SOUNDS[RANDOM.nextInt(TAUNT_SOUNDS.length)];
    }

    /**
     * Plays a random taunt sound at a location.
     *
     * @param location the location
     * @param volume the volume
     */
    public static void playTaunt(Location location, float volume) {
        if (location.getWorld() == null) return;
        Sound sound = getRandomTauntSound();
        location.getWorld().playSound(location, sound, SoundCategory.PLAYERS, volume, 1f);
    }

    // ===== UI Sounds =====

    /**
     * Plays a click sound.
     *
     * @param player the player
     */
    public static void playClick(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK,
                SoundCategory.MASTER, 0.5f, 1f);
    }

    /**
     * Plays an error sound.
     *
     * @param player the player
     */
    public static void playError(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT,
                SoundCategory.MASTER, 0.5f, 0.5f);
    }

    /**
     * Plays a success sound.
     *
     * @param player the player
     */
    public static void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP,
                SoundCategory.MASTER, 0.5f, 2f);
    }

    // ===== Utility Methods =====

    /**
     * Plays a sound for all players in a collection.
     *
     * @param players the players
     * @param sound the sound
     * @param volume the volume
     * @param pitch the pitch
     */
    public static void playForAll(Collection<? extends Player> players, Sound sound,
                                   float volume, float pitch) {
        for (Player player : players) {
            player.playSound(player.getLocation(), sound, SoundCategory.MASTER, volume, pitch);
        }
    }

    /**
     * Plays a sound at a location for all nearby players.
     *
     * @param location the location
     * @param sound the sound
     * @param volume the volume (also affects radius)
     * @param pitch the pitch
     */
    public static void playAtLocation(Location location, Sound sound, float volume, float pitch) {
        if (location.getWorld() == null) return;
        location.getWorld().playSound(location, sound, SoundCategory.PLAYERS, volume, pitch);
    }
}
