package com.example.prophunt.commands;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.arena.Arena;
import com.example.prophunt.arena.ArenaRegion;
import com.example.prophunt.arena.ArenaScanner;
import com.example.prophunt.game.Game;
import com.example.prophunt.game.GameState;
import com.example.prophunt.managers.SelectionManager;
import com.example.prophunt.player.GamePlayer;
import com.example.prophunt.stats.PlayerStats;
import com.example.prophunt.stats.StatsManager;
import com.example.prophunt.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main command handler for PropHunt.
 */
public class PropHuntCommand implements CommandExecutor, TabCompleter {

    private final PropHuntPlugin plugin;
    private final MessageUtil msg;

    private static final List<String> PLAYER_COMMANDS = Arrays.asList(
            "join", "leave", "list", "stats", "top", "help"
    );

    private static final List<String> ADMIN_COMMANDS = Arrays.asList(
            "create", "delete", "setup", "setspawn", "setregion",
            "scan", "enable", "disable", "forcestart", "forcestop",
            "reload", "info"
    );

    public PropHuntCommand(PropHuntPlugin plugin) {
        this.plugin = plugin;
        this.msg = plugin.getMessageUtil();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);

        switch (subCommand) {
            // Player commands
            case "join" -> handleJoin(sender, subArgs);
            case "leave" -> handleLeave(sender);
            case "list" -> handleList(sender);
            case "stats" -> handleStats(sender, subArgs);
            case "top" -> handleTop(sender, subArgs);
            case "help" -> showHelp(sender);

            // Admin commands
            case "create" -> handleCreate(sender, subArgs);
            case "delete" -> handleDelete(sender, subArgs);
            case "setup" -> handleSetup(sender, subArgs);
            case "setspawn" -> handleSetSpawn(sender, subArgs);
            case "setregion" -> handleSetRegion(sender, subArgs);
            case "scan" -> handleScan(sender, subArgs);
            case "enable" -> handleEnable(sender, subArgs);
            case "disable" -> handleDisable(sender, subArgs);
            case "forcestart" -> handleForceStart(sender, subArgs);
            case "forcestop" -> handleForceStop(sender, subArgs);
            case "reload" -> handleReload(sender);
            case "info" -> handleInfo(sender, subArgs);

            default -> {
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUnknown command. Use /ph help"));
            }
        }

        return true;
    }

    // ===== Player Commands =====

    private void handleJoin(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            msg.send(sender, "general.player-only");
            return;
        }

        if (!player.hasPermission("prophunt.play")) {
            msg.send(sender, "general.no-permission");
            return;
        }

        // Check if already in game
        if (plugin.getPlayerManager().isInGame(player)) {
            msg.send(sender, "game.already-in-game");
            return;
        }

        Game game;

        if (args.length > 0) {
            // Join specific arena
            Arena arena = plugin.getArenaManager().getArena(args[0]);
            if (arena == null) {
                msg.send(sender, "arena.not-found", "name", args[0]);
                return;
            }
            if (!arena.isEnabled()) {
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cArena is disabled!"));
                return;
            }
            if (!plugin.getGameManager().joinGame(player, arena)) {
                msg.send(sender, "game.game-full");
                return;
            }
            game = plugin.getGameManager().getGame(arena);
        } else {
            // Join any available arena
            game = plugin.getGameManager().joinAnyGame(player);
            if (game == null) {
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cNo available games!"));
                return;
            }
        }
    }

    private void handleLeave(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            msg.send(sender, "general.player-only");
            return;
        }

        if (!plugin.getPlayerManager().isInGame(player)) {
            msg.send(sender, "game.not-in-game");
            return;
        }

        plugin.getGameManager().leaveGame(player);
        msg.send(sender, "game.leave");
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(MessageUtil.colorize("&6&l========== PropHunt Arenas =========="));

        for (Arena arena : plugin.getArenaManager().getArenas()) {
            Game game = plugin.getGameManager().getGame(arena);
            String status;
            String players = "";

            if (!arena.isEnabled()) {
                status = "&8[DISABLED]";
            } else if (game == null || game.getState() == GameState.WAITING) {
                int count = game != null ? game.getPlayerCount() : 0;
                status = "&a[WAITING]";
                players = " &7(" + count + "/" + arena.getSettings().getMaxPlayers() + ")";
            } else if (game.getState() == GameState.STARTING) {
                status = "&e[STARTING]";
                players = " &7(" + game.getPlayerCount() + "/" + arena.getSettings().getMaxPlayers() + ")";
            } else if (game.getState().isInProgress()) {
                status = "&c[IN GAME]";
                players = " &7- " + game.getTimer().getFormattedTime() + " remaining";
            } else {
                status = "&7[ENDING]";
            }

            sender.sendMessage(MessageUtil.colorize(" &e" + arena.getName() + " " + status + players));
        }

        if (plugin.getArenaManager().getCount() == 0) {
            sender.sendMessage(MessageUtil.colorize(" &7No arenas configured."));
        }

        sender.sendMessage(MessageUtil.colorize("&6&l======================================"));
    }

    private void handleStats(CommandSender sender, String[] args) {
        Player targetPlayer;

        if (args.length > 0) {
            // Look up another player's stats
            @SuppressWarnings("deprecation")
            OfflinePlayer offline = Bukkit.getOfflinePlayer(args[0]);
            if (!offline.hasPlayedBefore() && !offline.isOnline()) {
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cPlayer not found: " + args[0]));
                return;
            }
            if (offline.isOnline()) {
                targetPlayer = offline.getPlayer();
            } else {
                // For offline players, we'd need async lookup - for now show error
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cPlayer must be online to view stats."));
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph stats <player>"));
                return;
            }
            targetPlayer = (Player) sender;
        }

        PlayerStats stats = plugin.getStatsManager().getStats(targetPlayer);

        sender.sendMessage(MessageUtil.colorize("&6&l========== " + targetPlayer.getName() + "'s Stats =========="));
        sender.sendMessage(MessageUtil.colorize("&7Games: &f" + stats.getGamesPlayed() +
                " &7| Wins: &a" + stats.getGamesWon() +
                " &7| Losses: &c" + stats.getGamesLost()));
        sender.sendMessage(MessageUtil.colorize("&7Win Rate: &e" + String.format("%.1f", stats.getWinRate()) + "%"));
        sender.sendMessage(MessageUtil.colorize(""));
        sender.sendMessage(MessageUtil.colorize("&a&lAs Prop:"));
        sender.sendMessage(MessageUtil.colorize("  &7Times Played: &f" + stats.getTimesAsProp() +
                " &7| Survived: &a" + stats.getPropSurvives() +
                " &7| Caught: &c" + stats.getPropDeaths()));
        sender.sendMessage(MessageUtil.colorize("  &7Survival Rate: &e" + String.format("%.1f", stats.getPropSurvivalRate()) + "%"));
        sender.sendMessage(MessageUtil.colorize(""));
        sender.sendMessage(MessageUtil.colorize("&c&lAs Hunter:"));
        sender.sendMessage(MessageUtil.colorize("  &7Times Played: &f" + stats.getTimesAsHunter() +
                " &7| Found: &e" + stats.getPropsFound() +
                " &7| Killed: &c" + stats.getPropsKilled()));
        sender.sendMessage(MessageUtil.colorize("  &7Accuracy: &e" + String.format("%.1f", stats.getHunterAccuracy()) + "%" +
                " &7| K/D: &f" + String.format("%.2f", stats.getKDR())));
        sender.sendMessage(MessageUtil.colorize(""));
        sender.sendMessage(MessageUtil.colorize("&6Total Points: &e" + stats.getTotalPoints() +
                " &7| Best Game: &e" + stats.getHighestGamePoints()));
        sender.sendMessage(MessageUtil.colorize("&6&l=========================================="));
    }

    private void handleTop(CommandSender sender, String[] args) {
        StatsManager.StatType statType = StatsManager.StatType.TOTAL_POINTS;

        if (args.length > 0) {
            try {
                statType = StatsManager.StatType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize(
                        "&cInvalid stat type. Valid: points, wins, kills, found, survives"));
                return;
            }
        }

        final StatsManager.StatType finalStatType = statType;
        sender.sendMessage(MessageUtil.colorize("&6&l========== Leaderboard: " + statType.getDisplayName() + " =========="));

        plugin.getStatsManager().getTopPlayers(statType, 10).thenAccept(topPlayers -> {
            if (topPlayers.isEmpty()) {
                sender.sendMessage(MessageUtil.colorize("&7No statistics recorded yet."));
                return;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                int rank = 1;
                for (PlayerStats stats : topPlayers) {
                    String value = switch (finalStatType) {
                        case TOTAL_POINTS -> String.valueOf(stats.getTotalPoints());
                        case GAMES_WON -> String.valueOf(stats.getGamesWon());
                        case GAMES_PLAYED -> String.valueOf(stats.getGamesPlayed());
                        case PROPS_KILLED -> String.valueOf(stats.getPropsKilled());
                        case PROPS_FOUND -> String.valueOf(stats.getPropsFound());
                        case PROP_SURVIVES -> String.valueOf(stats.getPropSurvives());
                        case HIGHEST_GAME_POINTS -> String.valueOf(stats.getHighestGamePoints());
                        case TOTAL_PLAY_TIME -> formatPlayTime(stats.getTotalPlayTime());
                    };

                    String color = rank <= 3 ? (rank == 1 ? "&6" : rank == 2 ? "&7" : "&c") : "&f";
                    sender.sendMessage(MessageUtil.colorize(
                            color + "#" + rank + " &e" + stats.getLastKnownName() + " &7- &f" + value));
                    rank++;
                }
                sender.sendMessage(MessageUtil.colorize("&6&l============================================"));
            });
        });
    }

    private String formatPlayTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(MessageUtil.colorize("&6&l========== PropHunt Help =========="));
        sender.sendMessage(MessageUtil.colorize("&e/ph join [arena] &7- Join a game"));
        sender.sendMessage(MessageUtil.colorize("&e/ph leave &7- Leave current game"));
        sender.sendMessage(MessageUtil.colorize("&e/ph list &7- List arenas"));
        sender.sendMessage(MessageUtil.colorize("&e/ph stats [player] &7- View statistics"));
        sender.sendMessage(MessageUtil.colorize("&e/ph help &7- Show this help"));

        if (sender.hasPermission("prophunt.admin")) {
            sender.sendMessage(MessageUtil.colorize(""));
            sender.sendMessage(MessageUtil.colorize("&c&lAdmin Commands:"));
            sender.sendMessage(MessageUtil.colorize("&e/ph create <name> &7- Create arena"));
            sender.sendMessage(MessageUtil.colorize("&e/ph delete <arena> &7- Delete arena"));
            sender.sendMessage(MessageUtil.colorize("&e/ph setspawn <type> &7- Set spawn (prop/hunter/lobby)"));
            sender.sendMessage(MessageUtil.colorize("&e/ph setregion <type> &7- Set region"));
            sender.sendMessage(MessageUtil.colorize("&e/ph scan <arena> &7- Scan for props"));
            sender.sendMessage(MessageUtil.colorize("&e/ph enable/disable <arena> &7- Toggle arena"));
            sender.sendMessage(MessageUtil.colorize("&e/ph forcestart/forcestop <arena> &7- Control games"));
            sender.sendMessage(MessageUtil.colorize("&e/ph reload &7- Reload config"));
        }
        sender.sendMessage(MessageUtil.colorize("&6&l===================================="));
    }

    // ===== Admin Commands =====

    private void handleCreate(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;

        if (args.length == 0) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph create <name>"));
            return;
        }

        String name = args[0];

        if (plugin.getArenaManager().exists(name)) {
            msg.send(sender, "arena.already-exists", "name", name);
            return;
        }

        Arena arena = plugin.getArenaManager().create(name);
        if (arena != null) {
            msg.send(sender, "arena.created", "name", name);
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&7Use /ph setregion, /ph setspawn, and /ph scan to set up the arena."));
        }
    }

    private void handleDelete(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;

        if (args.length == 0) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph delete <arena>"));
            return;
        }

        String name = args[0];

        if (!plugin.getArenaManager().exists(name)) {
            msg.send(sender, "arena.not-found", "name", name);
            return;
        }

        // Force stop any running game
        Arena arena = plugin.getArenaManager().getArena(name);
        plugin.getGameManager().forceStop(arena);

        if (plugin.getArenaManager().delete(name)) {
            msg.send(sender, "arena.deleted", "name", name);
        }
    }

    private void handleSetSpawn(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        if (!(sender instanceof Player player)) {
            msg.send(sender, "general.player-only");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph setspawn <arena> <prop|hunter|lobby>"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            msg.send(sender, "arena.not-found", "name", args[0]);
            return;
        }

        String type = args[1].toLowerCase();
        switch (type) {
            case "prop" -> {
                arena.addPropSpawn(player.getLocation());
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aProp spawn #" + arena.getPropSpawns().size() + " set!"));
            }
            case "hunter" -> {
                arena.addHunterSpawn(player.getLocation());
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aHunter spawn #" + arena.getHunterSpawns().size() + " set!"));
            }
            case "lobby" -> {
                arena.setLobbySpawn(player.getLocation());
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aLobby spawn set!"));
            }
            default -> sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cInvalid type! Use: prop, hunter, lobby"));
        }

        plugin.getArenaManager().save(arena);
    }

    private void handleSetup(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        if (!(sender instanceof Player player)) {
            msg.send(sender, "general.player-only");
            return;
        }

        SelectionManager selectionManager = plugin.getSelectionManager();

        // Check if exiting setup mode
        if (args.length > 0 && args[0].equalsIgnoreCase("done")) {
            if (selectionManager.isInSetupMode(player)) {
                selectionManager.exitSetupMode(player);
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aExited setup mode."));
            } else {
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cYou are not in setup mode."));
            }
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph setup <arena> or /ph setup done"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            msg.send(sender, "arena.not-found", "name", args[0]);
            return;
        }

        // Enter setup mode
        selectionManager.enterSetupMode(player);

        // Give selection wand
        player.getInventory().addItem(SelectionManager.createWand());

        sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&a&lEntered setup mode for arena: &e" + arena.getName()));
        sender.sendMessage(MessageUtil.colorize(""));
        sender.sendMessage(MessageUtil.colorize("&6You received a &eRegion Selector &6(Golden Axe):"));
        sender.sendMessage(MessageUtil.colorize("  &7Left-click a block: &eSet position 1"));
        sender.sendMessage(MessageUtil.colorize("  &7Right-click a block: &eSet position 2"));
        sender.sendMessage(MessageUtil.colorize(""));
        sender.sendMessage(MessageUtil.colorize("&6After selecting, use:"));
        sender.sendMessage(MessageUtil.colorize("  &e/ph setregion " + arena.getName() + " arena &7- Set play area"));
        sender.sendMessage(MessageUtil.colorize("  &e/ph setregion " + arena.getName() + " lobby &7- Set lobby area"));
        sender.sendMessage(MessageUtil.colorize("  &e/ph setregion " + arena.getName() + " huntercage &7- Set hunter cage"));
        sender.sendMessage(MessageUtil.colorize(""));
        sender.sendMessage(MessageUtil.colorize("&7Use &e/ph setup done &7when finished."));

        // Show current arena status
        handleInfo(sender, args);
    }

    private void handleSetRegion(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;
        if (!(sender instanceof Player player)) {
            msg.send(sender, "general.player-only");
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph setregion <arena> <arena|lobby|huntercage>"));
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&7First select two corners with the Region Selector wand."));
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&7Use &e/ph setup <arena> &7to get the wand."));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            msg.send(sender, "arena.not-found", "name", args[0]);
            return;
        }

        SelectionManager selectionManager = plugin.getSelectionManager();

        // Check if player has a complete selection
        if (!selectionManager.hasCompleteSelection(player)) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cYou need to select two corners first!"));
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&7Use &e/ph setup " + arena.getName() + " &7to get the selection wand."));
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&7Left-click = Pos1, Right-click = Pos2"));
            return;
        }

        ArenaRegion region = selectionManager.createRegionFromSelection(player);
        if (region == null) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cFailed to create region. Ensure both positions are in the same world."));
            return;
        }

        String type = args[1].toLowerCase();
        switch (type) {
            case "arena" -> {
                arena.setArenaRegion(region);
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aArena region set! &7" + region.getDimensions() + " (" + region.getVolume() + " blocks)"));
            }
            case "lobby" -> {
                arena.setLobbyRegion(region);
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aLobby region set! &7" + region.getDimensions() + " (" + region.getVolume() + " blocks)"));
            }
            case "huntercage", "cage" -> {
                arena.setHunterCageRegion(region);
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aHunter cage region set! &7" + region.getDimensions() + " (" + region.getVolume() + " blocks)"));
            }
            default -> {
                sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cInvalid region type! Use: arena, lobby, huntercage"));
                return;
            }
        }

        // Save and clear selection
        plugin.getArenaManager().save(arena);
        selectionManager.clearSelection(player);
        sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&7Selection cleared. Select new corners for another region."));
    }

    private void handleScan(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;

        if (args.length == 0) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph scan <arena>"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            msg.send(sender, "arena.not-found", "name", args[0]);
            return;
        }

        if (arena.getArenaRegion() == null) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cArena region not set! Set it first."));
            return;
        }

        sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&7Scanning arena..."));

        ArenaScanner.ScanResult result = plugin.getArenaManager().scanArena(arena);

        if (result.isSuccess()) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&a" + result.getSummary()));
        } else {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&c" + result.getError()));
        }
    }

    private void handleEnable(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;

        if (args.length == 0) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph enable <arena>"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            msg.send(sender, "arena.not-found", "name", args[0]);
            return;
        }

        List<String> errors = plugin.getArenaManager().enable(arena);
        if (errors.isEmpty()) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aArena '" + arena.getName() + "' enabled!"));
        } else {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cCannot enable arena. Missing:"));
            for (String error : errors) {
                sender.sendMessage(MessageUtil.colorize("  &7- " + error));
            }
        }
    }

    private void handleDisable(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;

        if (args.length == 0) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph disable <arena>"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            msg.send(sender, "arena.not-found", "name", args[0]);
            return;
        }

        plugin.getGameManager().forceStop(arena);
        plugin.getArenaManager().disable(arena);
        sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aArena '" + arena.getName() + "' disabled!"));
    }

    private void handleForceStart(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;

        if (args.length == 0) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph forcestart <arena>"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            msg.send(sender, "arena.not-found", "name", args[0]);
            return;
        }

        if (plugin.getGameManager().forceStart(arena)) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aGame force started in " + arena.getName() + "!"));
        } else {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cCannot start game. Need at least 2 players."));
        }
    }

    private void handleForceStop(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;

        if (args.length == 0) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph forcestop <arena>"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            msg.send(sender, "arena.not-found", "name", args[0]);
            return;
        }

        if (plugin.getGameManager().forceStop(arena)) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&aGame force stopped in " + arena.getName() + "!"));
        } else {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cNo game running in that arena."));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!checkAdmin(sender)) return;

        plugin.reload();
        msg.send(sender, "general.reload-success");
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!checkAdmin(sender)) return;

        if (args.length == 0) {
            sender.sendMessage(msg.getPrefix() + MessageUtil.colorize("&cUsage: /ph info <arena>"));
            return;
        }

        Arena arena = plugin.getArenaManager().getArena(args[0]);
        if (arena == null) {
            msg.send(sender, "arena.not-found", "name", args[0]);
            return;
        }

        sender.sendMessage(MessageUtil.colorize("&6&l===== Arena: " + arena.getName() + " ====="));
        sender.sendMessage(MessageUtil.colorize("&7Enabled: " + (arena.isEnabled() ? "&aYes" : "&cNo")));
        sender.sendMessage(MessageUtil.colorize("&7Arena Region: " + (arena.getArenaRegion() != null ? "&aSet" : "&cNot set")));
        sender.sendMessage(MessageUtil.colorize("&7Lobby Spawn: " + (arena.getLobbySpawn() != null ? "&aSet" : "&cNot set")));
        sender.sendMessage(MessageUtil.colorize("&7Hunter Cage: " + (arena.getHunterCageRegion() != null ? "&aSet" : "&cNot set")));
        sender.sendMessage(MessageUtil.colorize("&7Prop Spawns: &e" + arena.getPropSpawns().size()));
        sender.sendMessage(MessageUtil.colorize("&7Hunter Spawns: &e" + arena.getHunterSpawns().size()));
        sender.sendMessage(MessageUtil.colorize("&7Valid Props: &e" + arena.getPropRegistry().size()));

        List<String> missing = arena.validate();
        if (!missing.isEmpty()) {
            sender.sendMessage(MessageUtil.colorize("&c&lMissing:"));
            for (String m : missing) {
                sender.sendMessage(MessageUtil.colorize("  &7- " + m));
            }
        }
    }

    private boolean checkAdmin(CommandSender sender) {
        if (!sender.hasPermission("prophunt.admin")) {
            msg.send(sender, "general.no-permission");
            return false;
        }
        return true;
    }

    // ===== Tab Completion =====

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Main subcommands
            List<String> commands = new ArrayList<>(PLAYER_COMMANDS);
            if (sender.hasPermission("prophunt.admin")) {
                commands.addAll(ADMIN_COMMANDS);
            }
            String partial = args[0].toLowerCase();
            for (String cmd : commands) {
                if (cmd.startsWith(partial)) {
                    completions.add(cmd);
                }
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String partial = args[1].toLowerCase();

            // Arena name completion
            if (Arrays.asList("join", "delete", "enable", "disable", "forcestart",
                    "forcestop", "scan", "setspawn", "setregion", "setup", "info").contains(sub)) {
                for (String name : plugin.getArenaManager().getArenaNames()) {
                    if (name.toLowerCase().startsWith(partial)) {
                        completions.add(name);
                    }
                }
                // "done" option for setup command
                if (sub.equals("setup") && "done".startsWith(partial)) {
                    completions.add("done");
                }
            }

            // Spawn type completion
            if (sub.equals("setspawn")) {
                for (String type : Arrays.asList("prop", "hunter", "lobby")) {
                    if (type.startsWith(partial)) {
                        completions.add(type);
                    }
                }
            }
        } else if (args.length == 3) {
            String sub = args[0].toLowerCase();
            String partial = args[2].toLowerCase();

            // Spawn type after arena name
            if (sub.equals("setspawn")) {
                for (String type : Arrays.asList("prop", "hunter", "lobby")) {
                    if (type.startsWith(partial)) {
                        completions.add(type);
                    }
                }
            }

            // Region type after arena name
            if (sub.equals("setregion")) {
                for (String type : Arrays.asList("arena", "lobby", "huntercage")) {
                    if (type.startsWith(partial)) {
                        completions.add(type);
                    }
                }
            }
        }

        return completions;
    }
}
