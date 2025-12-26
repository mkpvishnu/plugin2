package com.example.prophunt.util;

import com.example.prophunt.PropHuntPlugin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling messages and translations.
 */
public class MessageUtil {

    private final PropHuntPlugin plugin;
    private FileConfiguration messages;
    private String prefix;

    public MessageUtil(PropHuntPlugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    /**
     * Loads or reloads the messages configuration.
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);

        // Load defaults from jar
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaultMessages);
        }

        prefix = colorize(messages.getString("prefix", "&8[&6PropHunt&8] "));
    }

    /**
     * Gets the message prefix.
     *
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets a message from the configuration.
     *
     * @param path the message path
     * @return the colorized message
     */
    public String get(String path) {
        String message = messages.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Missing message: " + path);
            return colorize("&c[Missing: " + path + "]");
        }
        return colorize(message);
    }

    /**
     * Gets a message with placeholder replacements.
     *
     * @param path the message path
     * @param replacements placeholder-value pairs
     * @return the formatted message
     */
    public String get(String path, Object... replacements) {
        String message = get(path);
        return replacePlaceholders(message, replacements);
    }

    /**
     * Sends a prefixed message to a command sender.
     *
     * @param sender the receiver
     * @param path the message path
     */
    public void send(CommandSender sender, String path) {
        sender.sendMessage(prefix + get(path));
    }

    /**
     * Sends a prefixed message with placeholders.
     *
     * @param sender the receiver
     * @param path the message path
     * @param replacements placeholder-value pairs
     */
    public void send(CommandSender sender, String path, Object... replacements) {
        sender.sendMessage(prefix + get(path, replacements));
    }

    /**
     * Sends a raw message without prefix.
     *
     * @param sender the receiver
     * @param path the message path
     */
    public void sendRaw(CommandSender sender, String path) {
        sender.sendMessage(get(path));
    }

    /**
     * Sends a raw message with placeholders.
     *
     * @param sender the receiver
     * @param path the message path
     * @param replacements placeholder-value pairs
     */
    public void sendRaw(CommandSender sender, String path, Object... replacements) {
        sender.sendMessage(get(path, replacements));
    }

    /**
     * Sends an action bar message to a player.
     *
     * @param player the player
     * @param message the message
     */
    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(colorize(message)));
    }

    /**
     * Sends a title to a player.
     *
     * @param player the player
     * @param title the main title
     * @param subtitle the subtitle
     * @param fadeIn fade in ticks
     * @param stay display ticks
     * @param fadeOut fade out ticks
     */
    public void sendTitle(Player player, String title, String subtitle,
                          int fadeIn, int stay, int fadeOut) {
        player.sendTitle(colorize(title), colorize(subtitle), fadeIn, stay, fadeOut);
    }

    /**
     * Sends a title with default timings.
     *
     * @param player the player
     * @param title the main title
     * @param subtitle the subtitle
     */
    public void sendTitle(Player player, String title, String subtitle) {
        sendTitle(player, title, subtitle, 10, 70, 20);
    }

    /**
     * Broadcasts a message to all players.
     *
     * @param path the message path
     */
    public void broadcast(String path) {
        String message = prefix + get(path);
        Bukkit.broadcastMessage(message);
    }

    /**
     * Broadcasts a message with placeholders.
     *
     * @param path the message path
     * @param replacements placeholder-value pairs
     */
    public void broadcast(String path, Object... replacements) {
        String message = prefix + get(path, replacements);
        Bukkit.broadcastMessage(message);
    }

    /**
     * Colorizes a string using & color codes.
     *
     * @param text the text to colorize
     * @return colorized text
     */
    public static String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Strips color codes from a string.
     *
     * @param text the text to strip
     * @return stripped text
     */
    public static String stripColor(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(colorize(text));
    }

    /**
     * Replaces placeholders in a message.
     *
     * @param message the message
     * @param replacements key-value pairs
     * @return formatted message
     */
    public static String replacePlaceholders(String message, Object... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Replacements must be in key-value pairs");
        }

        String result = message;
        for (int i = 0; i < replacements.length; i += 2) {
            String key = String.valueOf(replacements[i]);
            String value = String.valueOf(replacements[i + 1]);
            result = result.replace("{" + key + "}", value);
        }
        return result;
    }

    /**
     * Formats time in seconds to mm:ss format.
     *
     * @param seconds the time in seconds
     * @return formatted time string
     */
    public static String formatTime(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", mins, secs);
    }

    /**
     * Formats time to a more readable format.
     *
     * @param seconds the time in seconds
     * @return formatted time string (e.g., "5m 30s")
     */
    public static String formatTimeVerbose(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        int mins = seconds / 60;
        int secs = seconds % 60;
        if (secs == 0) {
            return mins + "m";
        }
        return mins + "m " + secs + "s";
    }
}
