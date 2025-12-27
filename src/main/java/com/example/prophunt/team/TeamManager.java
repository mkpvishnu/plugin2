package com.example.prophunt.team;

import com.example.prophunt.config.GameSettings;
import com.example.prophunt.game.Game;
import com.example.prophunt.player.GamePlayer;
import com.example.prophunt.player.HunterPlayer;
import com.example.prophunt.player.PropPlayer;

import java.util.*;

/**
 * Manages team assignments for a game.
 */
public class TeamManager {

    private final Game game;
    private final List<PropPlayer> props;
    private final List<HunterPlayer> hunters;
    private final List<GamePlayer> spectators;

    private boolean firstBloodAwarded;

    public TeamManager(Game game) {
        this.game = game;
        this.props = new ArrayList<>();
        this.hunters = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.firstBloodAwarded = false;
    }

    /**
     * Assigns teams to all players in the game (RANDOM mode).
     *
     * @param players the players to assign
     * @param settings the game settings
     */
    public void assignTeams(Collection<GamePlayer> players, GameSettings settings) {
        List<GamePlayer> playerList = new ArrayList<>(players);
        Collections.shuffle(playerList);

        int propCount = settings.calculatePropCount(playerList.size());

        for (int i = 0; i < playerList.size(); i++) {
            GamePlayer gp = playerList.get(i);
            if (i < propCount) {
                PropPlayer prop = PropPlayer.fromGamePlayer(gp);
                props.add(prop);
            } else {
                HunterPlayer hunter = HunterPlayer.fromGamePlayer(gp);
                hunter.setAttackCooldown(settings.getAttackCooldown());
                hunters.add(hunter);
            }
        }
    }

    /**
     * Assigns teams based on player choices (CHOICE mode).
     * Players who chose a team get that team.
     * Undecided players are randomly assigned to balance teams.
     *
     * @param players the players to assign
     * @param choices map of player UUID to their team choice (null = undecided)
     * @param settings the game settings
     */
    public void assignTeamsWithChoices(Collection<GamePlayer> players,
                                        Map<UUID, Team> choices,
                                        GameSettings settings) {
        List<GamePlayer> undecided = new ArrayList<>();

        // First pass: assign players who made a choice
        for (GamePlayer gp : players) {
            Team choice = choices.get(gp.getUuid());
            if (choice == Team.PROPS) {
                PropPlayer prop = PropPlayer.fromGamePlayer(gp);
                props.add(prop);
            } else if (choice == Team.HUNTERS) {
                HunterPlayer hunter = HunterPlayer.fromGamePlayer(gp);
                hunter.setAttackCooldown(settings.getAttackCooldown());
                hunters.add(hunter);
            } else {
                undecided.add(gp);
            }
        }

        // Second pass: assign undecided players to balance teams
        Collections.shuffle(undecided);

        int minProps = settings.getMinPropsToStart();
        int minHunters = settings.getMinHuntersToStart();

        for (GamePlayer gp : undecided) {
            // Determine which team needs more players
            boolean needsMoreProps = props.size() < minProps;
            boolean needsMoreHunters = hunters.size() < minHunters;

            if (needsMoreProps && !needsMoreHunters) {
                PropPlayer prop = PropPlayer.fromGamePlayer(gp);
                props.add(prop);
            } else if (needsMoreHunters && !needsMoreProps) {
                HunterPlayer hunter = HunterPlayer.fromGamePlayer(gp);
                hunter.setAttackCooldown(settings.getAttackCooldown());
                hunters.add(hunter);
            } else {
                // Both satisfied or both need more - assign randomly (favor smaller team)
                if (props.size() <= hunters.size()) {
                    PropPlayer prop = PropPlayer.fromGamePlayer(gp);
                    props.add(prop);
                } else {
                    HunterPlayer hunter = HunterPlayer.fromGamePlayer(gp);
                    hunter.setAttackCooldown(settings.getAttackCooldown());
                    hunters.add(hunter);
                }
            }
        }
    }

    /**
     * Gets all props.
     *
     * @return unmodifiable list of props
     */
    public List<PropPlayer> getProps() {
        return Collections.unmodifiableList(props);
    }

    /**
     * Gets all hunters.
     *
     * @return unmodifiable list of hunters
     */
    public List<HunterPlayer> getHunters() {
        return Collections.unmodifiableList(hunters);
    }

    /**
     * Gets all spectators.
     *
     * @return unmodifiable list of spectators
     */
    public List<GamePlayer> getSpectators() {
        return Collections.unmodifiableList(spectators);
    }

    /**
     * Gets all alive props.
     *
     * @return list of alive props
     */
    public List<PropPlayer> getAliveProps() {
        return props.stream()
                .filter(p -> p.getTeam() == Team.PROPS)
                .toList();
    }

    /**
     * Gets all alive hunters.
     *
     * @return list of alive hunters
     */
    public List<HunterPlayer> getAliveHunters() {
        return hunters.stream()
                .filter(h -> h.getTeam() == Team.HUNTERS)
                .toList();
    }

    /**
     * Gets the count of alive props.
     *
     * @return alive prop count
     */
    public int getAlivePropCount() {
        return (int) props.stream()
                .filter(p -> p.getTeam() == Team.PROPS)
                .count();
    }

    /**
     * Gets the count of alive hunters.
     *
     * @return alive hunter count
     */
    public int getAliveHunterCount() {
        return (int) hunters.stream()
                .filter(h -> h.getTeam() == Team.HUNTERS)
                .count();
    }

    /**
     * Gets total prop count (including dead).
     *
     * @return total props
     */
    public int getTotalPropCount() {
        return props.size();
    }

    /**
     * Gets total hunter count (including dead).
     *
     * @return total hunters
     */
    public int getTotalHunterCount() {
        return hunters.size();
    }

    /**
     * Eliminates a prop (moves to spectator).
     *
     * @param prop the prop to eliminate
     */
    public void eliminateProp(PropPlayer prop) {
        prop.setTeam(Team.SPECTATOR);
        prop.setSpectator();
        spectators.add(prop);
    }

    /**
     * Eliminates a hunter (moves to spectator).
     *
     * @param hunter the hunter to eliminate
     */
    public void eliminateHunter(HunterPlayer hunter) {
        hunter.setTeam(Team.SPECTATOR);
        hunter.setSpectator();
        spectators.add(hunter);
    }

    /**
     * Gets a prop player by UUID.
     *
     * @param uuid the UUID
     * @return the prop, or null
     */
    public PropPlayer getProp(UUID uuid) {
        return props.stream()
                .filter(p -> p.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets a hunter player by UUID.
     *
     * @param uuid the UUID
     * @return the hunter, or null
     */
    public HunterPlayer getHunter(UUID uuid) {
        return hunters.stream()
                .filter(h -> h.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets any player by UUID (prop, hunter, or spectator).
     *
     * @param uuid the UUID
     * @return the player, or null
     */
    public GamePlayer getPlayer(UUID uuid) {
        PropPlayer prop = getProp(uuid);
        if (prop != null) return prop;

        HunterPlayer hunter = getHunter(uuid);
        if (hunter != null) return hunter;

        return spectators.stream()
                .filter(s -> s.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets all players in the game.
     *
     * @return list of all players
     */
    public List<GamePlayer> getAllPlayers() {
        List<GamePlayer> all = new ArrayList<>();
        all.addAll(props);
        all.addAll(hunters);
        all.addAll(spectators);
        return all;
    }

    /**
     * Removes a player from their team.
     *
     * @param uuid the player's UUID
     * @return the removed player, or null
     */
    public GamePlayer removePlayer(UUID uuid) {
        // Check props
        for (Iterator<PropPlayer> it = props.iterator(); it.hasNext(); ) {
            PropPlayer p = it.next();
            if (p.getUuid().equals(uuid)) {
                it.remove();
                return p;
            }
        }

        // Check hunters
        for (Iterator<HunterPlayer> it = hunters.iterator(); it.hasNext(); ) {
            HunterPlayer h = it.next();
            if (h.getUuid().equals(uuid)) {
                it.remove();
                return h;
            }
        }

        // Check spectators
        for (Iterator<GamePlayer> it = spectators.iterator(); it.hasNext(); ) {
            GamePlayer s = it.next();
            if (s.getUuid().equals(uuid)) {
                it.remove();
                return s;
            }
        }

        return null;
    }

    /**
     * Checks if first blood has been awarded.
     *
     * @return true if awarded
     */
    public boolean isFirstBloodAwarded() {
        return firstBloodAwarded;
    }

    /**
     * Awards first blood.
     *
     * @param hunter the hunter who got first blood
     */
    public void awardFirstBlood(HunterPlayer hunter) {
        if (!firstBloodAwarded) {
            firstBloodAwarded = true;
            hunter.setFirstBlood();
        }
    }

    /**
     * Gets the winning team.
     *
     * @return winning team, or null if game should continue
     */
    public Team getWinningTeam() {
        if (getAlivePropCount() == 0) {
            return Team.HUNTERS;
        }
        if (getAliveHunterCount() == 0) {
            return Team.PROPS;
        }
        return null; // Game continues
    }

    /**
     * Clears all team data.
     */
    public void clear() {
        props.clear();
        hunters.clear();
        spectators.clear();
        firstBloodAwarded = false;
    }
}
