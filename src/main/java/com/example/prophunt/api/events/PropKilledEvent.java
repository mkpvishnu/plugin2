package com.example.prophunt.api.events;

import com.example.prophunt.game.Game;
import com.example.prophunt.player.HunterPlayer;
import com.example.prophunt.player.PropPlayer;
import org.bukkit.event.HandlerList;

/**
 * Called when a prop is killed.
 */
public class PropKilledEvent extends PropHuntEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final HunterPlayer killer;
    private final PropPlayer prop;

    public PropKilledEvent(Game game, HunterPlayer killer, PropPlayer prop) {
        super(game);
        this.killer = killer;
        this.prop = prop;
    }

    /**
     * Gets the hunter who killed the prop.
     *
     * @return the killer
     */
    public HunterPlayer getKiller() {
        return killer;
    }

    /**
     * Gets the prop that was killed.
     *
     * @return the prop
     */
    public PropPlayer getProp() {
        return prop;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
