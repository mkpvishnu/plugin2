package com.example.prophunt.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fluent builder for creating ItemStacks.
 */
public class ItemBuilder {

    private final ItemStack itemStack;
    private final ItemMeta itemMeta;

    /**
     * Creates a new ItemBuilder with the specified material.
     *
     * @param material the material
     */
    public ItemBuilder(Material material) {
        this(material, 1);
    }

    /**
     * Creates a new ItemBuilder with the specified material and amount.
     *
     * @param material the material
     * @param amount the stack size
     */
    public ItemBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.itemMeta = itemStack.getItemMeta();
    }

    /**
     * Creates a new ItemBuilder from an existing ItemStack.
     *
     * @param itemStack the item to copy
     */
    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack.clone();
        this.itemMeta = this.itemStack.getItemMeta();
    }

    /**
     * Sets the display name.
     *
     * @param name the name (supports color codes)
     * @return this builder
     */
    public ItemBuilder name(String name) {
        if (itemMeta != null) {
            itemMeta.setDisplayName(MessageUtil.colorize(name));
        }
        return this;
    }

    /**
     * Sets the lore lines.
     *
     * @param lore the lore lines
     * @return this builder
     */
    public ItemBuilder lore(String... lore) {
        if (itemMeta != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(MessageUtil.colorize(line));
            }
            itemMeta.setLore(coloredLore);
        }
        return this;
    }

    /**
     * Sets the lore from a list.
     *
     * @param lore the lore lines
     * @return this builder
     */
    public ItemBuilder lore(List<String> lore) {
        if (itemMeta != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(MessageUtil.colorize(line));
            }
            itemMeta.setLore(coloredLore);
        }
        return this;
    }

    /**
     * Adds lines to existing lore.
     *
     * @param lines the lines to add
     * @return this builder
     */
    public ItemBuilder addLore(String... lines) {
        if (itemMeta != null) {
            List<String> lore = itemMeta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            for (String line : lines) {
                lore.add(MessageUtil.colorize(line));
            }
            itemMeta.setLore(lore);
        }
        return this;
    }

    /**
     * Sets the stack amount.
     *
     * @param amount the amount
     * @return this builder
     */
    public ItemBuilder amount(int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Adds an enchantment.
     *
     * @param enchantment the enchantment
     * @param level the level
     * @return this builder
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (itemMeta != null) {
            itemMeta.addEnchant(enchantment, level, true);
        }
        return this;
    }

    /**
     * Adds a glow effect without showing enchantments.
     *
     * @return this builder
     */
    public ItemBuilder glow() {
        if (itemMeta != null) {
            itemMeta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    /**
     * Adds item flags.
     *
     * @param flags the flags to add
     * @return this builder
     */
    public ItemBuilder flags(ItemFlag... flags) {
        if (itemMeta != null) {
            itemMeta.addItemFlags(flags);
        }
        return this;
    }

    /**
     * Hides all item flags (enchants, attributes, etc).
     *
     * @return this builder
     */
    public ItemBuilder hideFlags() {
        if (itemMeta != null) {
            itemMeta.addItemFlags(ItemFlag.values());
        }
        return this;
    }

    /**
     * Makes the item unbreakable.
     *
     * @return this builder
     */
    public ItemBuilder unbreakable() {
        if (itemMeta != null) {
            itemMeta.setUnbreakable(true);
        }
        return this;
    }

    /**
     * Sets the custom model data.
     *
     * @param data the custom model data
     * @return this builder
     */
    public ItemBuilder customModelData(int data) {
        if (itemMeta != null) {
            itemMeta.setCustomModelData(data);
        }
        return this;
    }

    /**
     * Sets leather armor color.
     *
     * @param color the color
     * @return this builder
     */
    public ItemBuilder leatherColor(Color color) {
        if (itemMeta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
        }
        return this;
    }

    /**
     * Sets skull owner for player heads.
     *
     * @param playerName the player name
     * @return this builder
     */
    public ItemBuilder skullOwner(String playerName) {
        if (itemMeta instanceof SkullMeta skullMeta) {
            skullMeta.setOwner(playerName);
        }
        return this;
    }

    /**
     * Builds and returns the ItemStack.
     *
     * @return the built ItemStack
     */
    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    // Static factory methods

    /**
     * Creates a simple named item.
     *
     * @param material the material
     * @param name the display name
     * @return the ItemStack
     */
    public static ItemStack create(Material material, String name) {
        return new ItemBuilder(material).name(name).build();
    }

    /**
     * Creates a simple named item with lore.
     *
     * @param material the material
     * @param name the display name
     * @param lore the lore lines
     * @return the ItemStack
     */
    public static ItemStack create(Material material, String name, String... lore) {
        return new ItemBuilder(material).name(name).lore(lore).build();
    }

    /**
     * Creates a GUI filler item (typically gray glass pane).
     *
     * @return the filler ItemStack
     */
    public static ItemStack filler() {
        return new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .name(" ")
                .build();
    }

    /**
     * Creates a GUI back button.
     *
     * @return the back button ItemStack
     */
    public static ItemStack backButton() {
        return new ItemBuilder(Material.ARROW)
                .name("&cBack")
                .build();
    }

    /**
     * Creates a GUI close button.
     *
     * @return the close button ItemStack
     */
    public static ItemStack closeButton() {
        return new ItemBuilder(Material.BARRIER)
                .name("&cClose")
                .build();
    }
}
