package com.example.prophunt.arena;

import com.example.prophunt.PropHuntPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages all arenas in the plugin.
 */
public class ArenaManager {

    private final PropHuntPlugin plugin;
    private final File arenasFolder;
    private final Map<String, Arena> arenas;
    private final ArenaScanner scanner;

    public ArenaManager(PropHuntPlugin plugin) {
        this.plugin = plugin;
        this.arenasFolder = new File(plugin.getDataFolder(), "arenas");
        this.arenas = new LinkedHashMap<>();
        this.scanner = new ArenaScanner(plugin);

        // Create arenas folder if it doesn't exist
        if (!arenasFolder.exists()) {
            arenasFolder.mkdirs();
        }

        // Load all arenas
        loadAll();
    }

    /**
     * Loads all arenas from disk.
     */
    public void loadAll() {
        arenas.clear();

        File[] files = arenasFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                Arena arena = Arena.load(file);
                if (arena != null) {
                    arenas.put(arena.getName().toLowerCase(), arena);
                    plugin.debug("Loaded arena: " + arena.getName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load arena from " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + arenas.size() + " arenas");
    }

    /**
     * Saves all arenas to disk.
     */
    public void saveAll() {
        for (Arena arena : arenas.values()) {
            save(arena);
        }
        plugin.debug("Saved " + arenas.size() + " arenas");
    }

    /**
     * Saves a specific arena.
     *
     * @param arena the arena to save
     */
    public void save(Arena arena) {
        File file = new File(arenasFolder, arena.getName().toLowerCase() + ".yml");
        try {
            arena.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save arena " + arena.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Creates a new arena.
     *
     * @param name the arena name
     * @return the created arena, or null if already exists
     */
    public Arena create(String name) {
        String key = name.toLowerCase();
        if (arenas.containsKey(key)) {
            return null; // Already exists
        }

        Arena arena = new Arena(name);
        arena.setSettings(plugin.getConfigManager().getDefaultGameSettings());
        arenas.put(key, arena);
        save(arena);

        plugin.debug("Created arena: " + name);
        return arena;
    }

    /**
     * Deletes an arena.
     *
     * @param name the arena name
     * @return true if deleted
     */
    public boolean delete(String name) {
        String key = name.toLowerCase();
        Arena arena = arenas.remove(key);
        if (arena == null) {
            return false;
        }

        File file = new File(arenasFolder, key + ".yml");
        if (file.exists()) {
            file.delete();
        }

        plugin.debug("Deleted arena: " + name);
        return true;
    }

    /**
     * Gets an arena by name.
     *
     * @param name the arena name
     * @return the arena, or null if not found
     */
    public Arena getArena(String name) {
        return arenas.get(name.toLowerCase());
    }

    /**
     * Checks if an arena exists.
     *
     * @param name the arena name
     * @return true if exists
     */
    public boolean exists(String name) {
        return arenas.containsKey(name.toLowerCase());
    }

    /**
     * Gets all arenas.
     *
     * @return unmodifiable collection of arenas
     */
    public Collection<Arena> getArenas() {
        return Collections.unmodifiableCollection(arenas.values());
    }

    /**
     * Gets all enabled arenas.
     *
     * @return list of enabled arenas
     */
    public List<Arena> getEnabledArenas() {
        List<Arena> enabled = new ArrayList<>();
        for (Arena arena : arenas.values()) {
            if (arena.isEnabled()) {
                enabled.add(arena);
            }
        }
        return enabled;
    }

    /**
     * Gets all arena names.
     *
     * @return set of arena names
     */
    public Set<String> getArenaNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Arena arena : arenas.values()) {
            names.add(arena.getName());
        }
        return names;
    }

    /**
     * Gets the arena scanner.
     *
     * @return the scanner
     */
    public ArenaScanner getScanner() {
        return scanner;
    }

    /**
     * Scans an arena for valid props.
     *
     * @param arena the arena to scan
     * @return scan result
     */
    public ArenaScanner.ScanResult scanArena(Arena arena) {
        ArenaScanner.ScanResult result = scanner.scan(arena);
        if (result.isSuccess()) {
            save(arena);
        }
        return result;
    }

    /**
     * Enables an arena if it's valid.
     *
     * @param arena the arena
     * @return list of validation errors, empty if successful
     */
    public List<String> enable(Arena arena) {
        List<String> errors = arena.validate();
        if (errors.isEmpty()) {
            arena.setEnabled(true);
            save(arena);
        }
        return errors;
    }

    /**
     * Disables an arena.
     *
     * @param arena the arena
     */
    public void disable(Arena arena) {
        arena.setEnabled(false);
        save(arena);
    }

    /**
     * Gets the count of arenas.
     *
     * @return arena count
     */
    public int getCount() {
        return arenas.size();
    }

    /**
     * Gets the count of enabled arenas.
     *
     * @return enabled arena count
     */
    public int getEnabledCount() {
        int count = 0;
        for (Arena arena : arenas.values()) {
            if (arena.isEnabled()) count++;
        }
        return count;
    }

    /**
     * Reloads all arenas from disk.
     */
    public void reload() {
        loadAll();
    }
}
