package com.example.prophunt.api.events;

import com.example.prophunt.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 * Called when a player leaves a PropHunt game.
 */
public class PlayerLeaveGameEvent extends PropHuntEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final LeaveReason reason;

    public PlayerLeaveGameEvent(Game game, Player player, LeaveReason reason) {
        super(game);
        this.player = player;
        this.reason = reason;
    }

    /**
     * Gets the player leaving the game.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the reason for leaving.
     *
     * @return the leave reason
     */
    public LeaveReason getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Reasons a player might leave a game.
     */
    public enum LeaveReason {
        QUIT,           // Player used /ph leave
        DISCONNECT,     // Player disconnected
        KICK,           // Player was kicked
        ELIMINATED,     // Player was eliminated
        GAME_END        // Game ended
    }
}
