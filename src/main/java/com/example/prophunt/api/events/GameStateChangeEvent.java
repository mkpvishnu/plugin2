package com.example.prophunt.api.events;

import com.example.prophunt.game.Game;
import com.example.prophunt.game.GameState;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a game's state changes.
 */
public class GameStateChangeEvent extends PropHuntEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final GameState previousState;
    private final GameState newState;
    private boolean cancelled;

    public GameStateChangeEvent(Game game, GameState previousState, GameState newState) {
        super(game);
        this.previousState = previousState;
        this.newState = newState;
    }

    /**
     * Gets the previous game state.
     *
     * @return the previous state
     */
    public GameState getPreviousState() {
        return previousState;
    }

    /**
     * Gets the new game state.
     *
     * @return the new state
     */
    public GameState getNewState() {
        return newState;
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
