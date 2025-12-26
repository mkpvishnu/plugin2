package com.example.prophunt.api.events;

import com.example.prophunt.game.Game;
import com.example.prophunt.team.Team;
import org.bukkit.event.HandlerList;

/**
 * Called when a PropHunt game ends.
 */
public class GameEndEvent extends PropHuntEvent {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Team winningTeam;
    private final EndReason reason;

    public GameEndEvent(Game game, Team winningTeam, EndReason reason) {
        super(game);
        this.winningTeam = winningTeam;
        this.reason = reason;
    }

    /**
     * Gets the winning team.
     *
     * @return the winning team, or null for a draw
     */
    public Team getWinningTeam() {
        return winningTeam;
    }

    /**
     * Gets the reason the game ended.
     *
     * @return the end reason
     */
    public EndReason getReason() {
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
     * Reasons a game might end.
     */
    public enum EndReason {
        ALL_PROPS_KILLED,      // Hunters killed all props
        ALL_HUNTERS_DEAD,      // All hunters eliminated themselves
        TIME_EXPIRED,          // Timer ran out (props win)
        NOT_ENOUGH_PLAYERS,    // Too few players remain
        FORCE_ENDED           // Admin force ended the game
    }
}
