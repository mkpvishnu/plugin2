package com.example.prophunt.api.events;

import com.example.prophunt.game.Game;
import com.example.prophunt.player.HunterPlayer;
import com.example.prophunt.player.PropPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called when a hunter finds (hits) a prop.
 */
public class PropFoundEvent extends PropHuntEvent implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final HunterPlayer hunter;
    private final PropPlayer prop;
    private double damage;
    private boolean cancelled;

    public PropFoundEvent(Game game, HunterPlayer hunter, PropPlayer prop, double damage) {
        super(game);
        this.hunter = hunter;
        this.prop = prop;
        this.damage = damage;
    }

    /**
     * Gets the hunter who found the prop.
     *
     * @return the hunter
     */
    public HunterPlayer getHunter() {
        return hunter;
    }

    /**
     * Gets the prop that was found.
     *
     * @return the prop
     */
    public PropPlayer getProp() {
        return prop;
    }

    /**
     * Gets the damage to be dealt.
     *
     * @return the damage amount
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Sets the damage to be dealt.
     *
     * @param damage the new damage amount
     */
    public void setDamage(double damage) {
        this.damage = damage;
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
