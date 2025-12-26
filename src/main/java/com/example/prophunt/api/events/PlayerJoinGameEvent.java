package com.example.prophunt.api.events;

import com.example.prophunt.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a player joins a PropHunt game.
 */
public class PlayerJoinGameEvent extends PropHuntEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private boolean cancelled;
    private String cancelReason;

    public PlayerJoinGameEvent(Game game, Player player) {
        super(game);
        this.player = player;
    }

    /**
     * Gets the player joining the game.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the cancellation reason.
     *
     * @return the reason, or null if not cancelled
     */
    public String getCancelReason() {
        return cancelReason;
    }

    /**
     * Sets the cancellation reason.
     *
     * @param reason the reason to show the player
     */
    public void setCancelReason(String reason) {
        this.cancelReason = reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
