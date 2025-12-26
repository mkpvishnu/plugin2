package com.example.prophunt.team;

import org.bukkit.ChatColor;

/**
 * Represents the different teams in PropHunt.
 */
public enum Team {

    /**
     * Props - players who hide as blocks.
     */
    PROPS("Props", "Prop", ChatColor.GREEN, "§a"),

    /**
     * Hunters - players who seek and eliminate props.
     */
    HUNTERS("Hunters", "Hunter", ChatColor.RED, "§c"),

    /**
     * Spectators - eliminated players or observers.
     */
    SPECTATOR("Spectators", "Spectator", ChatColor.GRAY, "§7"),

    /**
     * No team assigned yet.
     */
    NONE("None", "None", ChatColor.WHITE, "§f");

    private final String displayName;
    private final String singularName;
    private final ChatColor color;
    private final String colorCode;

    Team(String displayName, String singularName, ChatColor color, String colorCode) {
        this.displayName = displayName;
        this.singularName = singularName;
        this.color = color;
        this.colorCode = colorCode;
    }

    /**
     * Gets the plural display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Gets the singular name.
     *
     * @return the singular name
     */
    public String getSingularName() {
        return singularName;
    }

    /**
     * Gets the team color.
     *
     * @return the ChatColor
     */
    public ChatColor getColor() {
        return color;
    }

    /**
     * Gets the color code string.
     *
     * @return the color code
     */
    public String getColorCode() {
        return colorCode;
    }

    /**
     * Gets the colored display name.
     *
     * @return colored name
     */
    public String getColoredName() {
        return colorCode + displayName;
    }

    /**
     * Gets the colored singular name.
     *
     * @return colored singular name
     */
    public String getColoredSingularName() {
        return colorCode + singularName;
    }

    /**
     * Checks if this is a playing team.
     *
     * @return true if PROPS or HUNTERS
     */
    public boolean isPlayingTeam() {
        return this == PROPS || this == HUNTERS;
    }
}
