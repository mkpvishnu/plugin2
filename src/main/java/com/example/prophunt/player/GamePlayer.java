package com.example.prophunt.player;

import com.example.prophunt.game.Game;
import com.example.prophunt.team.Team;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Base class for a player participating in a PropHunt game.
 */
public class GamePlayer {

    protected final UUID uuid;
    protected final Player player;
    protected Game game;
    protected Team team;

    // Stats for current game
    protected int points;
    protected long gameJoinTime;

    // Saved state to restore after game
    private Location previousLocation;
    private ItemStack[] previousInventory;
    private ItemStack[] previousArmor;
    private GameMode previousGameMode;
    private double previousHealth;
    private int previousFoodLevel;

    /**
     * Creates a new GamePlayer.
     *
     * @param player the Bukkit player
     * @param game the game they're joining
     */
    public GamePlayer(Player player, Game game) {
        this.uuid = player.getUniqueId();
        this.player = player;
        this.game = game;
        this.team = Team.NONE;
        this.points = 0;
        this.gameJoinTime = System.currentTimeMillis();
    }

    /**
     * Saves the player's current state before game modifications.
     */
    public void saveState() {
        this.previousLocation = player.getLocation().clone();
        this.previousInventory = player.getInventory().getContents().clone();
        this.previousArmor = player.getInventory().getArmorContents().clone();
        this.previousGameMode = player.getGameMode();
        this.previousHealth = player.getHealth();
        this.previousFoodLevel = player.getFoodLevel();
    }

    /**
     * Restores the player's state after game ends.
     */
    public void restoreState() {
        if (previousLocation != null) {
            player.teleport(previousLocation);
        }
        if (previousInventory != null) {
            player.getInventory().setContents(previousInventory);
        }
        if (previousArmor != null) {
            player.getInventory().setArmorContents(previousArmor);
        }
        if (previousGameMode != null) {
            player.setGameMode(previousGameMode);
        }
        player.setHealth(Math.min(previousHealth, player.getMaxHealth()));
        player.setFoodLevel(previousFoodLevel);

        // Clear effects
        player.getActivePotionEffects().forEach(effect ->
                player.removePotionEffect(effect.getType()));
    }

    /**
     * Prepares the player for the game.
     */
    public void prepare() {
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setGameMode(GameMode.ADVENTURE);
        player.setExp(0);
        player.setLevel(0);
        player.getActivePotionEffects().forEach(effect ->
                player.removePotionEffect(effect.getType()));
    }

    /**
     * Makes the player a spectator.
     */
    public void setSpectator() {
        this.team = Team.SPECTATOR;
        player.setGameMode(GameMode.SPECTATOR);
    }

    /**
     * Adds points to this player.
     *
     * @param amount points to add
     */
    public void addPoints(int amount) {
        this.points += amount;
    }

    /**
     * Gets the time elapsed since joining the game.
     *
     * @return time in milliseconds
     */
    public long getTimeInGame() {
        return System.currentTimeMillis() - gameJoinTime;
    }

    /**
     * Gets the time in game formatted as seconds.
     *
     * @return time in seconds
     */
    public int getTimeInGameSeconds() {
        return (int) (getTimeInGame() / 1000);
    }

    // ===== Getters =====

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return player.getName();
    }

    public Game getGame() {
        return game;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public int getPoints() {
        return points;
    }

    public boolean isOnline() {
        return player.isOnline();
    }

    public boolean isProp() {
        return team == Team.PROPS;
    }

    public boolean isHunter() {
        return team == Team.HUNTERS;
    }

    public boolean isSpectator() {
        return team == Team.SPECTATOR;
    }

    public boolean isAlive() {
        return team == Team.PROPS || team == Team.HUNTERS;
    }

    public Location getLocation() {
        return player.getLocation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GamePlayer that = (GamePlayer) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "GamePlayer{" +
                "name=" + getName() +
                ", team=" + team +
                ", points=" + points +
                '}';
    }
}
