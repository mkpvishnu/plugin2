package com.example.prophunt.stats;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.util.MessageUtil;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages leaderboard display and caching.
 */
public class LeaderboardManager {

    private final PropHuntPlugin plugin;
    private final StatsManager statsManager;

    // Cached leaderboards (refreshed periodically)
    private List<PlayerStats> topByPoints;
    private List<PlayerStats> topByWins;
    private List<PlayerStats> topByKills;
    private long lastRefresh;

    private static final long CACHE_DURATION = TimeUnit.MINUTES.toMillis(5);
    private static final int LEADERBOARD_SIZE = 10;

    public LeaderboardManager(PropHuntPlugin plugin, StatsManager statsManager) {
        this.plugin = plugin;
        this.statsManager = statsManager;
    }

    /**
     * Shows the leaderboard to a player.
     */
    public void showLeaderboard(Player player, StatsManager.StatType statType) {
        refreshIfNeeded();

        List<PlayerStats> leaderboard = switch (statType) {
            case TOTAL_POINTS -> topByPoints;
            case GAMES_WON -> topByWins;
            case PROPS_KILLED -> topByKills;
            default -> null;
        };

        if (leaderboard == null) {
            // Fetch fresh for uncommon stats
            statsManager.getTopPlayers(statType, LEADERBOARD_SIZE).thenAccept(stats -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    displayLeaderboard(player, stats, statType);
                });
            });
            return;
        }

        displayLeaderboard(player, leaderboard, statType);
    }

    /**
     * Displays the leaderboard to a player.
     */
    private void displayLeaderboard(Player player, List<PlayerStats> stats, StatsManager.StatType statType) {
        MessageUtil messageUtil = plugin.getMessageUtil();

        player.sendMessage("");
        player.sendMessage(MessageUtil.colorize("&6&l━━━ PropHunt Leaderboard ━━━"));
        player.sendMessage(MessageUtil.colorize("&7Top 10 by " + statType.getDisplayName()));
        player.sendMessage("");

        if (stats.isEmpty()) {
            player.sendMessage(MessageUtil.colorize("&7No stats recorded yet."));
        } else {
            for (int i = 0; i < stats.size(); i++) {
                PlayerStats ps = stats.get(i);
                String rank = getRankColor(i + 1) + "#" + (i + 1);
                String name = ps.getLastKnownName() != null ? ps.getLastKnownName() : "Unknown";
                String value = formatStatValue(ps, statType);

                player.sendMessage(MessageUtil.colorize(
                        rank + " &f" + name + " &7- &e" + value));
            }
        }

        player.sendMessage("");

        // Show player's rank
        statsManager.getPlayerRank(player.getUniqueId(), statType).thenAccept(rank -> {
            if (rank > 0) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(MessageUtil.colorize("&7Your rank: &e#" + rank));
                    player.sendMessage("");
                });
            }
        });
    }

    /**
     * Gets rank color based on position.
     */
    private String getRankColor(int rank) {
        return switch (rank) {
            case 1 -> "&6"; // Gold
            case 2 -> "&f"; // Silver (white)
            case 3 -> "&c"; // Bronze (red-ish)
            default -> "&7"; // Gray
        };
    }

    /**
     * Formats a stat value for display.
     */
    private String formatStatValue(PlayerStats stats, StatsManager.StatType statType) {
        return switch (statType) {
            case GAMES_PLAYED -> String.valueOf(stats.getGamesPlayed());
            case GAMES_WON -> String.valueOf(stats.getGamesWon());
            case TOTAL_POINTS -> formatNumber(stats.getTotalPoints());
            case PROPS_KILLED -> String.valueOf(stats.getPropsKilled());
            case PROPS_FOUND -> String.valueOf(stats.getPropsFound());
            case PROP_SURVIVES -> String.valueOf(stats.getPropSurvives());
            case HIGHEST_GAME_POINTS -> formatNumber(stats.getHighestGamePoints());
            case TOTAL_PLAY_TIME -> formatTime(stats.getTotalPlayTime());
        };
    }

    /**
     * Formats a large number.
     */
    private String formatNumber(long number) {
        if (number >= 1_000_000) {
            return String.format("%.1fM", number / 1_000_000.0);
        } else if (number >= 1_000) {
            return String.format("%.1fK", number / 1_000.0);
        }
        return String.valueOf(number);
    }

    /**
     * Formats time in seconds to readable format.
     */
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes);
        }
        return String.format("%dm", minutes);
    }

    /**
     * Refreshes leaderboard cache if expired.
     */
    private void refreshIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastRefresh < CACHE_DURATION && topByPoints != null) {
            return;
        }

        lastRefresh = now;

        // Refresh common leaderboards asynchronously
        statsManager.getTopPlayers(StatsManager.StatType.TOTAL_POINTS, LEADERBOARD_SIZE)
                .thenAccept(stats -> topByPoints = stats);

        statsManager.getTopPlayers(StatsManager.StatType.GAMES_WON, LEADERBOARD_SIZE)
                .thenAccept(stats -> topByWins = stats);

        statsManager.getTopPlayers(StatsManager.StatType.PROPS_KILLED, LEADERBOARD_SIZE)
                .thenAccept(stats -> topByKills = stats);
    }

    /**
     * Shows a player their own stats.
     */
    public void showPlayerStats(Player player) {
        showPlayerStats(player, player);
    }

    /**
     * Shows a player's stats to a viewer.
     */
    public void showPlayerStats(Player viewer, Player target) {
        PlayerStats stats = statsManager.getStats(target);

        viewer.sendMessage("");
        viewer.sendMessage(MessageUtil.colorize("&6&l━━━ " + target.getName() + "'s Stats ━━━"));
        viewer.sendMessage("");

        // General stats
        viewer.sendMessage(MessageUtil.colorize("&e&lGeneral"));
        viewer.sendMessage(MessageUtil.colorize("  &7Games Played: &f" + stats.getGamesPlayed()));
        viewer.sendMessage(MessageUtil.colorize("  &7Win Rate: &f" + String.format("%.1f%%", stats.getWinRate())));
        viewer.sendMessage(MessageUtil.colorize("  &7Total Points: &f" + formatNumber(stats.getTotalPoints())));
        viewer.sendMessage(MessageUtil.colorize("  &7Highest Score: &f" + formatNumber(stats.getHighestGamePoints())));
        viewer.sendMessage("");

        // Prop stats
        viewer.sendMessage(MessageUtil.colorize("&a&lAs Prop"));
        viewer.sendMessage(MessageUtil.colorize("  &7Times as Prop: &f" + stats.getTimesAsProp()));
        viewer.sendMessage(MessageUtil.colorize("  &7Survival Rate: &f" + String.format("%.1f%%", stats.getPropSurvivalRate())));
        viewer.sendMessage(MessageUtil.colorize("  &7Time Hidden: &f" + formatTime(stats.getTotalTimeAsHiddenProp())));
        viewer.sendMessage(MessageUtil.colorize("  &7Successful Taunts: &f" + stats.getSuccessfulTaunts()));
        viewer.sendMessage("");

        // Hunter stats
        viewer.sendMessage(MessageUtil.colorize("&c&lAs Hunter"));
        viewer.sendMessage(MessageUtil.colorize("  &7Times as Hunter: &f" + stats.getTimesAsHunter()));
        viewer.sendMessage(MessageUtil.colorize("  &7Props Found: &f" + stats.getPropsFound()));
        viewer.sendMessage(MessageUtil.colorize("  &7Props Killed: &f" + stats.getPropsKilled()));
        viewer.sendMessage(MessageUtil.colorize("  &7Accuracy: &f" + String.format("%.1f%%", stats.getHunterAccuracy())));
        viewer.sendMessage(MessageUtil.colorize("  &7K/D Ratio: &f" + String.format("%.2f", stats.getKDR())));
        viewer.sendMessage("");
    }
}
