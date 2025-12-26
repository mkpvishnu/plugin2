package com.example.prophunt.game;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.arena.Arena;
import com.example.prophunt.config.GameSettings;
import com.example.prophunt.player.GamePlayer;
import com.example.prophunt.player.HunterPlayer;
import com.example.prophunt.player.PropPlayer;
import com.example.prophunt.team.Team;
import com.example.prophunt.team.TeamManager;
import com.example.prophunt.util.MessageUtil;
import com.example.prophunt.util.SoundUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Represents an individual PropHunt game instance.
 */
public class Game {

    private final PropHuntPlugin plugin;
    private final Arena arena;
    private final GameSettings settings;
    private final TeamManager teamManager;
    private final GameTimer timer;

    private GameState state;
    private final Map<UUID, GamePlayer> waitingPlayers;

    private Team winner;
    private long gameStartTime;

    public Game(PropHuntPlugin plugin, Arena arena) {
        this.plugin = plugin;
        this.arena = arena;
        this.settings = arena.getSettings().copy();
        this.teamManager = new TeamManager(this);
        this.timer = new GameTimer(plugin, this);
        this.state = GameState.WAITING;
        this.waitingPlayers = new HashMap<>();
    }

    // ===== Player Management =====

    /**
     * Adds a player to the game.
     *
     * @param player the player to add
     * @return true if added successfully
     */
    public boolean addPlayer(Player player) {
        if (!canJoin()) {
            return false;
        }

        if (isInGame(player)) {
            return false;
        }

        if (getPlayerCount() >= settings.getMaxPlayers()) {
            return false;
        }

        GamePlayer gamePlayer = new GamePlayer(player, this);
        gamePlayer.saveState();
        gamePlayer.prepare();
        waitingPlayers.put(player.getUniqueId(), gamePlayer);

        // Teleport to lobby
        if (arena.getLobbySpawn() != null) {
            player.teleport(arena.getLobbySpawn());
        }

        // Notify
        broadcastMessage("game.join",
                "player", player.getName(),
                "current", String.valueOf(getPlayerCount()),
                "max", String.valueOf(settings.getMaxPlayers()));

        plugin.getPlayerManager().addPlayer(gamePlayer);

        // Check if can start
        checkStart();

        return true;
    }

    /**
     * Removes a player from the game.
     *
     * @param player the player
     * @param disconnect true if player disconnected
     */
    public void removePlayer(Player player, boolean disconnect) {
        UUID uuid = player.getUniqueId();

        GamePlayer gamePlayer = null;

        // Check waiting players
        if (waitingPlayers.containsKey(uuid)) {
            gamePlayer = waitingPlayers.remove(uuid);
        } else {
            // Check team players
            gamePlayer = teamManager.removePlayer(uuid);
        }

        if (gamePlayer == null) return;

        // Restore state
        gamePlayer.restoreState();
        plugin.getPlayerManager().removePlayer(uuid);

        if (!disconnect) {
            // Notify
            broadcastMessage("game.leave",
                    "player", player.getName(),
                    "current", String.valueOf(getPlayerCount()),
                    "max", String.valueOf(settings.getMaxPlayers()));
        }

        // Check game state
        if (state.isInProgress()) {
            checkGameEnd();
        } else if (state == GameState.STARTING) {
            // Cancel start if not enough players
            if (getPlayerCount() < settings.getMinPlayers()) {
                cancelStart();
            }
        }
    }

    /**
     * Checks if a player is in this game.
     *
     * @param player the player
     * @return true if in game
     */
    public boolean isInGame(Player player) {
        return waitingPlayers.containsKey(player.getUniqueId()) ||
               teamManager.getPlayer(player.getUniqueId()) != null;
    }

    /**
     * Gets the total player count.
     *
     * @return player count
     */
    public int getPlayerCount() {
        if (state == GameState.WAITING || state == GameState.STARTING) {
            return waitingPlayers.size();
        }
        return teamManager.getAllPlayers().size();
    }

    // ===== Game State Management =====

    /**
     * Checks if the game can start and starts countdown if ready.
     */
    private void checkStart() {
        if (state != GameState.WAITING) return;
        if (getPlayerCount() >= settings.getMinPlayers()) {
            startCountdown();
        }
    }

    /**
     * Starts the pre-game countdown.
     */
    public void startCountdown() {
        if (state != GameState.WAITING) return;

        setState(GameState.STARTING);

        timer.start(settings.getLobbyCountdown(),
                // On tick
                remaining -> {
                    if (remaining <= 10 && remaining > 0) {
                        broadcastMessage("game.game-starting", "seconds", String.valueOf(remaining));
                        for (GamePlayer gp : waitingPlayers.values()) {
                            if (remaining <= 3) {
                                SoundUtil.playCountdownFinal(gp.getPlayer());
                            } else {
                                SoundUtil.playCountdownTick(gp.getPlayer());
                            }
                        }
                    }
                },
                // On complete
                this::startGame
        );
    }

    /**
     * Cancels the starting countdown.
     */
    public void cancelStart() {
        if (state != GameState.STARTING) return;

        timer.stop();
        setState(GameState.WAITING);
        broadcastRawMessage("&cNot enough players! Countdown cancelled.");
    }

    /**
     * Force starts the game (admin command).
     */
    public void forceStart() {
        if (state == GameState.WAITING || state == GameState.STARTING) {
            timer.stop();
            startGame();
        }
    }

    /**
     * Starts the game.
     */
    private void startGame() {
        setState(GameState.HIDING);
        gameStartTime = System.currentTimeMillis();

        // Assign teams
        teamManager.assignTeams(waitingPlayers.values(), settings);
        waitingPlayers.clear();

        // Setup props
        for (PropPlayer prop : teamManager.getProps()) {
            setupProp(prop);
        }

        // Setup hunters
        for (HunterPlayer hunter : teamManager.getHunters()) {
            setupHunter(hunter);
        }

        // Start hiding phase timer
        timer.start(settings.getHideTime(),
                remaining -> {
                    // Tick updates
                    if (remaining == 10 || remaining == 5 || remaining <= 3) {
                        for (PropPlayer prop : teamManager.getProps()) {
                            sendMessage(prop.getPlayer(), "game.hide-phase", "seconds", String.valueOf(remaining));
                        }
                    }
                },
                this::startHuntingPhase
        );
    }

    /**
     * Sets up a prop player.
     */
    private void setupProp(PropPlayer prop) {
        Player player = prop.getPlayer();

        // Teleport to random prop spawn
        Location spawn = arena.getRandomPropSpawn();
        if (spawn != null) {
            player.teleport(spawn);
        }

        // Send team reveal
        plugin.getMessageUtil().sendTitle(player,
                "&a&lYOU ARE A PROP!",
                "&7Hide from the hunters!");
        SoundUtil.playGameStart(player);

        // Give prop selector item (will be implemented with disguise system)
        // Give nether star for prop selection
        player.getInventory().setItem(0,
                new com.example.prophunt.util.ItemBuilder(org.bukkit.Material.NETHER_STAR)
                        .name("&b&lSelect Disguise")
                        .lore("&7Right-click to choose your prop!")
                        .build());
    }

    /**
     * Sets up a hunter player.
     */
    private void setupHunter(HunterPlayer hunter) {
        Player player = hunter.getPlayer();

        // Teleport to hunter cage
        Location spawn = arena.getRandomHunterSpawn();
        if (spawn == null && arena.getHunterCageRegion() != null) {
            spawn = arena.getHunterCageRegion().getCenter();
        }
        if (spawn != null) {
            player.teleport(spawn);
        }

        // Send team reveal
        plugin.getMessageUtil().sendTitle(player,
                "&c&lYOU ARE A HUNTER!",
                "&7Find all the props!");
        SoundUtil.playGameStart(player);

        // Apply blindness
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS,
                settings.getHideTime() * 20, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS,
                settings.getHideTime() * 20, 255, false, false));
    }

    /**
     * Starts the hunting phase.
     */
    private void startHuntingPhase() {
        setState(GameState.HUNTING);

        // Release hunters
        for (HunterPlayer hunter : teamManager.getHunters()) {
            Player player = hunter.getPlayer();

            // Remove effects
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOWNESS);

            // Teleport to arena (random hunter spawn)
            Location spawn = arena.getRandomHunterSpawn();
            if (spawn != null) {
                player.teleport(spawn);
            }

            // Give hunter kit
            player.getInventory().setItem(0,
                    new com.example.prophunt.util.ItemBuilder(org.bukkit.Material.IRON_SWORD)
                            .name("&c&lProp Finder")
                            .lore("&7Hit blocks to find props!", "&c&oBe careful - wrong hits hurt you!")
                            .build());

            plugin.getMessageUtil().sendTitle(player,
                    "&c&lHUNT BEGINS!",
                    "&7Find the props!");
            SoundUtil.playHuntersReleased(player);
        }

        // Notify props
        for (PropPlayer prop : teamManager.getProps()) {
            plugin.getMessageUtil().sendTitle(prop.getPlayer(),
                    "&e&lHUNTERS RELEASED!",
                    "&7Stay hidden!");
            SoundUtil.playHuntersReleased(prop.getPlayer());
        }

        // Start hunting timer
        timer.start(settings.getSeekTime(),
                remaining -> {
                    // Late game notifications
                    if (remaining == 120) { // 2 minutes
                        broadcastRawMessage("&e&lThe hunt intensifies!");
                    } else if (remaining == 60) { // 1 minute
                        broadcastRawMessage("&c&lFinal minute!");
                    } else if (remaining == 30) { // 30 seconds
                        broadcastRawMessage("&4&lLast stand!");
                    }
                },
                () -> endGame(Team.PROPS) // Props win if time runs out
        );
    }

    /**
     * Checks if the game should end.
     */
    public void checkGameEnd() {
        if (!state.isInProgress()) return;

        Team winner = teamManager.getWinningTeam();
        if (winner != null) {
            endGame(winner);
        }
    }

    /**
     * Ends the game with the specified winner.
     *
     * @param winner the winning team
     */
    public void endGame(Team winner) {
        if (state == GameState.ENDING || state == GameState.DISABLED) return;

        timer.stop();
        setState(GameState.ENDING);
        this.winner = winner;

        // Announce winner
        String winMessage = winner == Team.PROPS ? "game.prop-win" : "game.hunter-win";
        int alivePropCount = teamManager.getAlivePropCount();

        for (GamePlayer gp : teamManager.getAllPlayers()) {
            Player player = gp.getPlayer();
            if (gp.getTeam() == winner ||
                (gp.isSpectator() && wasOnTeam(gp, winner))) {
                plugin.getMessageUtil().sendTitle(player,
                        "&6&lVICTORY!",
                        winner == Team.PROPS ? "&aProps survived!" : "&cAll props eliminated!");
                SoundUtil.playVictory(player);
            } else {
                plugin.getMessageUtil().sendTitle(player,
                        "&c&lDEFEAT!",
                        winner == Team.PROPS ? "&7Props survived..." : "&7You were found...");
                SoundUtil.playDefeat(player);
            }
        }

        // End game after delay
        timer.runDelayed(200L, this::reset); // 10 second delay
    }

    private boolean wasOnTeam(GamePlayer gp, Team team) {
        // Check if player was originally on the team
        if (team == Team.PROPS) {
            return teamManager.getProp(gp.getUuid()) != null;
        } else {
            return teamManager.getHunter(gp.getUuid()) != null;
        }
    }

    /**
     * Forces the game to stop.
     */
    public void forceStop() {
        timer.stop();
        reset();
    }

    /**
     * Resets the game to waiting state.
     */
    public void reset() {
        timer.stop();

        // Restore all players
        for (GamePlayer gp : teamManager.getAllPlayers()) {
            gp.restoreState();
            plugin.getPlayerManager().removePlayer(gp.getUuid());
        }

        for (GamePlayer gp : waitingPlayers.values()) {
            gp.restoreState();
            plugin.getPlayerManager().removePlayer(gp.getUuid());
        }

        // Clear state
        teamManager.clear();
        waitingPlayers.clear();
        winner = null;

        setState(GameState.WAITING);
    }

    // ===== State Management =====

    /**
     * Sets the game state.
     *
     * @param state the new state
     */
    private void setState(GameState state) {
        this.state = state;
        plugin.debug("Game %s state changed to %s", arena.getName(), state);
    }

    /**
     * Checks if players can join.
     *
     * @return true if joinable
     */
    public boolean canJoin() {
        return state.isJoinable() && getPlayerCount() < settings.getMaxPlayers();
    }

    // ===== Messaging =====

    /**
     * Broadcasts a message to all players in the game.
     *
     * @param path the message path
     * @param replacements placeholder replacements
     */
    public void broadcastMessage(String path, Object... replacements) {
        String message = plugin.getMessageUtil().get(path, replacements);
        for (GamePlayer gp : getAllPlayers()) {
            gp.getPlayer().sendMessage(plugin.getMessageUtil().getPrefix() + message);
        }
    }

    /**
     * Broadcasts a raw message to all players.
     *
     * @param message the message
     */
    public void broadcastRawMessage(String message) {
        String colorized = MessageUtil.colorize(message);
        for (GamePlayer gp : getAllPlayers()) {
            gp.getPlayer().sendMessage(plugin.getMessageUtil().getPrefix() + colorized);
        }
    }

    /**
     * Sends a message to a player.
     *
     * @param player the player
     * @param path the message path
     * @param replacements replacements
     */
    public void sendMessage(Player player, String path, Object... replacements) {
        plugin.getMessageUtil().send(player, path, replacements);
    }

    // ===== Getters =====

    private List<GamePlayer> getAllPlayers() {
        if (state == GameState.WAITING || state == GameState.STARTING) {
            return new ArrayList<>(waitingPlayers.values());
        }
        return teamManager.getAllPlayers();
    }

    public PropHuntPlugin getPlugin() {
        return plugin;
    }

    public Arena getArena() {
        return arena;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public GameState getState() {
        return state;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public GameTimer getTimer() {
        return timer;
    }

    public Team getWinner() {
        return winner;
    }

    public int getTimeRemaining() {
        return timer.getTimeRemaining();
    }

    public long getGameDuration() {
        if (gameStartTime == 0) return 0;
        return System.currentTimeMillis() - gameStartTime;
    }

    @Override
    public String toString() {
        return "Game{" +
                "arena=" + arena.getName() +
                ", state=" + state +
                ", players=" + getPlayerCount() +
                '}';
    }
}
