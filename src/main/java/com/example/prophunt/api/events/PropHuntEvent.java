package com.example.prophunt.api.events;

import com.example.prophunt.game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base class for all PropHunt events.
 */
public abstract class PropHuntEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    protected final Game game;

    public PropHuntEvent(Game game) {
        this.game = game;
    }

    public PropHuntEvent(Game game, boolean async) {
        super(async);
        this.game = game;
    }

    /**
     * Gets the game associated with this event.
     *
     * @return the game
     */
    public Game getGame() {
        return game;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
