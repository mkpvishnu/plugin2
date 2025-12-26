package com.example.prophunt.gui;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.arena.Arena;
import com.example.prophunt.disguise.PropSize;
import com.example.prophunt.disguise.PropType;
import com.example.prophunt.player.PropPlayer;
import com.example.prophunt.util.ItemBuilder;
import com.example.prophunt.util.MessageUtil;
import com.example.prophunt.util.SoundUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * GUI for selecting a prop disguise.
 */
public class PropSelectorGUI implements Listener {

    private final PropHuntPlugin plugin;
    private final Map<UUID, PropSelectorSession> sessions;

    private static final String GUI_TITLE = MessageUtil.colorize("&8Select Your Disguise");
    private static final int GUI_SIZE = 54; // 6 rows

    public PropSelectorGUI(PropHuntPlugin plugin) {
        this.plugin = plugin;
        this.sessions = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the prop selector for a player.
     *
     * @param propPlayer the prop player
     */
    public void open(PropPlayer propPlayer) {
        Player player = propPlayer.getPlayer();
        Arena arena = propPlayer.getGame().getArena();

        Inventory gui = Bukkit.createInventory(null, GUI_SIZE, GUI_TITLE);

        // Get all available props
        List<PropType> allProps = new ArrayList<>(arena.getPropRegistry().getProps());

        // Sort by size
        allProps.sort(Comparator.comparing(PropType::getSize));

        // Create session
        PropSelectorSession session = new PropSelectorSession(propPlayer, allProps, 0);
        sessions.put(player.getUniqueId(), session);

        // Populate GUI
        populateGUI(gui, session);

        player.openInventory(gui);
        SoundUtil.playClick(player);
    }

    /**
     * Populates the GUI with props.
     */
    private void populateGUI(Inventory gui, PropSelectorSession session) {
        gui.clear();

        List<PropType> props = session.props;
        int page = session.page;
        int propsPerPage = 45; // 5 rows of props
        int startIndex = page * propsPerPage;

        // Add size filter buttons (top row)
        gui.setItem(0, createFilterButton(null, session.sizeFilter));
        gui.setItem(2, createFilterButton(PropSize.SMALL, session.sizeFilter));
        gui.setItem(4, createFilterButton(PropSize.MEDIUM, session.sizeFilter));
        gui.setItem(6, createFilterButton(PropSize.LARGE, session.sizeFilter));

        // Info item
        gui.setItem(8, new ItemBuilder(Material.BOOK)
                .name("&e&lProp Selection")
                .lore(
                        "&7Choose your disguise wisely!",
                        "",
                        "&aSmall: &f4 hearts &7(hard to spot)",
                        "&eMedium: &f7 hearts &7(balanced)",
                        "&cLarge: &f10 hearts &7(easy to spot)",
                        "",
                        "&7Changes remaining: &e" + session.propPlayer.getPropChangesRemaining()
                )
                .build());

        // Filter props by size if filter is active
        List<PropType> filteredProps = props;
        if (session.sizeFilter != null) {
            filteredProps = props.stream()
                    .filter(p -> p.getSize() == session.sizeFilter)
                    .toList();
        }

        // Add prop items (rows 2-6)
        for (int i = 0; i < propsPerPage && startIndex + i < filteredProps.size(); i++) {
            PropType prop = filteredProps.get(startIndex + i);
            int slot = 9 + i; // Start from row 2

            gui.setItem(slot, createPropItem(prop));
        }

        // Navigation buttons (bottom row if needed)
        int totalPages = (int) Math.ceil((double) filteredProps.size() / propsPerPage);
        if (page > 0) {
            gui.setItem(45, new ItemBuilder(Material.ARROW)
                    .name("&a← Previous Page")
                    .build());
        }
        if (page < totalPages - 1) {
            gui.setItem(53, new ItemBuilder(Material.ARROW)
                    .name("&aNext Page →")
                    .build());
        }

        // Page indicator
        gui.setItem(49, new ItemBuilder(Material.PAPER)
                .name("&7Page " + (page + 1) + "/" + Math.max(1, totalPages))
                .build());
    }

    /**
     * Creates a filter button.
     */
    private ItemStack createFilterButton(PropSize size, PropSize currentFilter) {
        Material material;
        String name;
        String health;

        if (size == null) {
            material = Material.NETHER_STAR;
            name = "&f&lAll Props";
            health = "";
        } else {
            material = switch (size) {
                case SMALL -> Material.FLOWER_POT;
                case MEDIUM -> Material.LANTERN;
                case LARGE -> Material.BARREL;
            };
            name = "&" + getSizeColorCode(size) + "&l" + size.getDisplayName();
            health = " &7(" + size.getHearts() + " hearts)";
        }

        boolean selected = Objects.equals(size, currentFilter);

        return new ItemBuilder(material)
                .name((selected ? "&a▶ " : "") + name + health)
                .lore(selected ? "&aCurrently selected" : "&7Click to filter")
                .glow()
                .build();
    }

    /**
     * Creates a prop item for the GUI.
     */
    private ItemStack createPropItem(PropType prop) {
        Material material = prop.getMaterial();
        PropSize size = prop.getSize();

        // Use the block material if it's an item, otherwise use a placeholder
        Material displayMaterial = material.isItem() ? material : Material.BARRIER;

        String sizeColor = "&" + getSizeColorCode(size);

        return new ItemBuilder(displayMaterial)
                .name(sizeColor + prop.getDisplayName())
                .lore(
                        "",
                        "&7Size: " + sizeColor + size.getDisplayName(),
                        "&7Health: " + sizeColor + size.getHearts() + " hearts",
                        "",
                        "&eClick to select!"
                )
                .build();
    }

    private char getSizeColorCode(PropSize size) {
        return switch (size) {
            case SMALL -> 'a';
            case MEDIUM -> 'e';
            case LARGE -> 'c';
        };
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;

        event.setCancelled(true);

        PropSelectorSession session = sessions.get(player.getUniqueId());
        if (session == null) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        int slot = event.getSlot();

        // Filter buttons (top row)
        if (slot == 0) {
            session.sizeFilter = null;
            session.page = 0;
            populateGUI(event.getInventory(), session);
            SoundUtil.playClick(player);
            return;
        }
        if (slot == 2) {
            session.sizeFilter = PropSize.SMALL;
            session.page = 0;
            populateGUI(event.getInventory(), session);
            SoundUtil.playClick(player);
            return;
        }
        if (slot == 4) {
            session.sizeFilter = PropSize.MEDIUM;
            session.page = 0;
            populateGUI(event.getInventory(), session);
            SoundUtil.playClick(player);
            return;
        }
        if (slot == 6) {
            session.sizeFilter = PropSize.LARGE;
            session.page = 0;
            populateGUI(event.getInventory(), session);
            SoundUtil.playClick(player);
            return;
        }

        // Navigation
        if (slot == 45 && session.page > 0) {
            session.page--;
            populateGUI(event.getInventory(), session);
            SoundUtil.playClick(player);
            return;
        }
        if (slot == 53) {
            session.page++;
            populateGUI(event.getInventory(), session);
            SoundUtil.playClick(player);
            return;
        }

        // Prop selection (slots 9-53)
        if (slot >= 9 && slot < 54) {
            // Find which prop was clicked
            int propsPerPage = 45;
            int propIndex = (session.page * propsPerPage) + (slot - 9);

            List<PropType> filteredProps = session.props;
            if (session.sizeFilter != null) {
                filteredProps = session.props.stream()
                        .filter(p -> p.getSize() == session.sizeFilter)
                        .toList();
            }

            if (propIndex < filteredProps.size()) {
                PropType selectedProp = filteredProps.get(propIndex);
                selectProp(session.propPlayer, selectedProp);
                player.closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) {
            sessions.remove(player.getUniqueId());
        }
    }

    /**
     * Handles prop selection.
     */
    private void selectProp(PropPlayer propPlayer, PropType propType) {
        Player player = propPlayer.getPlayer();

        // Check cooldown
        int cooldown = propPlayer.getGame().getSettings().getVoluntaryTauntCooldown();
        if (!propPlayer.canChangeProp(cooldown)) {
            int remaining = propPlayer.getPropChangeCooldown(cooldown);
            plugin.getMessageUtil().send(player, "prop.cooldown", "seconds", String.valueOf(remaining));
            SoundUtil.playError(player);
            return;
        }

        // Check changes remaining
        if (propPlayer.getPropChangesRemaining() <= 0) {
            plugin.getMessageUtil().send(player, "prop.no-changes-left");
            SoundUtil.playError(player);
            return;
        }

        // Apply disguise
        propPlayer.usePropChange();
        plugin.getDisguiseManager().createDisguise(propPlayer, propType);

        plugin.getMessageUtil().send(player, "prop.disguised", "block", propType.getDisplayName());
        SoundUtil.playSuccess(player);
    }

    /**
     * Session data for a player using the GUI.
     */
    private static class PropSelectorSession {
        final PropPlayer propPlayer;
        final List<PropType> props;
        int page;
        PropSize sizeFilter;

        PropSelectorSession(PropPlayer propPlayer, List<PropType> props, int page) {
            this.propPlayer = propPlayer;
            this.props = props;
            this.page = page;
            this.sizeFilter = null;
        }
    }
}
