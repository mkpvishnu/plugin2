package com.example.prophunt.gui;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.game.Game;
import com.example.prophunt.team.Team;
import com.example.prophunt.util.ItemBuilder;
import com.example.prophunt.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * GUI for players to select their team in the lobby.
 */
public class TeamSelectorGUI implements Listener {

    private static final String GUI_TITLE = MessageUtil.colorize("&6&lSelect Your Team");
    private static final int GUI_SIZE = 27; // 3 rows

    private final PropHuntPlugin plugin;

    public TeamSelectorGUI(PropHuntPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the team selector GUI for a player.
     *
     * @param player the player
     * @param game the game they're in
     */
    public void open(Player player, Game game) {
        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);

        // Get current team counts
        int propCount = game.getTeamChoiceCount(Team.PROPS);
        int hunterCount = game.getTeamChoiceCount(Team.HUNTERS);
        int undecidedCount = game.getTeamChoiceCount(null);
        Team currentChoice = game.getTeamChoice(player);

        // Prop option (slot 11 - left side)
        ItemStack propItem = new ItemBuilder(Material.SLIME_BALL)
                .name("&a&lJoin Props")
                .lore(
                        "&7Hide as a block and survive!",
                        "",
                        "&7Current props: &a" + propCount,
                        "",
                        currentChoice == Team.PROPS ? "&a&l> SELECTED <" : "&eClick to select!"
                )
                .build();
        gui.setItem(11, propItem);

        // Random option (slot 13 - center)
        ItemStack randomItem = new ItemBuilder(Material.ENDER_PEARL)
                .name("&e&lRandom Team")
                .lore(
                        "&7Let the game decide!",
                        "",
                        "&7Undecided: &e" + undecidedCount,
                        "",
                        currentChoice == null ? "&a&l> SELECTED <" : "&eClick to select!"
                )
                .build();
        gui.setItem(13, randomItem);

        // Hunter option (slot 15 - right side)
        ItemStack hunterItem = new ItemBuilder(Material.IRON_SWORD)
                .name("&c&lJoin Hunters")
                .lore(
                        "&7Find and eliminate the props!",
                        "",
                        "&7Current hunters: &c" + hunterCount,
                        "",
                        currentChoice == Team.HUNTERS ? "&a&l> SELECTED <" : "&eClick to select!"
                )
                .build();
        gui.setItem(15, hunterItem);

        // Info item (slot 22 - bottom center)
        ItemStack infoItem = new ItemBuilder(Material.BOOK)
                .name("&6&lTeam Info")
                .lore(
                        "&7Props: &a" + propCount + " &7players",
                        "&7Hunters: &c" + hunterCount + " &7players",
                        "&7Undecided: &e" + undecidedCount + " &7players",
                        "",
                        "&7Total: &f" + (propCount + hunterCount + undecidedCount) + " &7players"
                )
                .build();
        gui.setItem(22, infoItem);

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= GUI_SIZE) return;

        // Check if player is in a game
        Game game = plugin.getGameManager().getPlayerGame(player);
        if (game == null) {
            player.closeInventory();
            return;
        }

        Team newChoice = null;

        switch (slot) {
            case 11 -> { // Prop
                newChoice = Team.PROPS;
                game.setTeamChoice(player, Team.PROPS);
                plugin.getMessageUtil().send(player, "team.joined-props");
            }
            case 13 -> { // Random
                newChoice = null;
                game.setTeamChoice(player, null);
                plugin.getMessageUtil().send(player, "team.joined-random");
            }
            case 15 -> { // Hunter
                newChoice = Team.HUNTERS;
                game.setTeamChoice(player, Team.HUNTERS);
                plugin.getMessageUtil().send(player, "team.joined-hunters");
            }
            default -> {
                return; // Clicked elsewhere, don't close
            }
        }

        // Refresh GUI to show new selection
        open(player, game);
    }

    /**
     * Creates the team selector item for the hotbar.
     *
     * @return the item
     */
    public static ItemStack createSelectorItem() {
        return new ItemBuilder(Material.COMPASS)
                .name("&6&lSelect Team")
                .lore(
                        "&7Click to choose your team!",
                        "",
                        "&aProps &7- Hide as blocks",
                        "&cHunters &7- Find the props"
                )
                .build();
    }
}
