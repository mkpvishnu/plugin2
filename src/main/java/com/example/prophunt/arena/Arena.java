package com.example.prophunt.arena;

import com.example.prophunt.config.GameSettings;
import com.example.prophunt.disguise.PropType;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Represents a PropHunt arena with all its configuration.
 */
public class Arena {

    private final String name;
    private boolean enabled;

    // Regions
    private ArenaRegion arenaRegion;      // Main play area
    private ArenaRegion lobbyRegion;       // Lobby waiting area
    private ArenaRegion hunterCageRegion;  // Hunter cage/waiting area

    // Spawn points
    private Location lobbySpawn;
    private final List<Location> propSpawns;
    private final List<Location> hunterSpawns;

    // Valid props for this arena
    private final PropRegistry propRegistry;

    // Arena-specific settings (overrides defaults)
    private GameSettings settings;

    /**
     * Creates a new arena with the given name.
     *
     * @param name the arena name
     */
    public Arena(String name) {
        this.name = name;
        this.enabled = false;
        this.propSpawns = new ArrayList<>();
        this.hunterSpawns = new ArrayList<>();
        this.propRegistry = new PropRegistry();
        this.settings = new GameSettings();
    }

    // ===== Region Methods =====

    /**
     * Gets the arena name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if the arena is enabled.
     *
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the arena is enabled.
     *
     * @param enabled true to enable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the main arena region.
     *
     * @return the arena region
     */
    public ArenaRegion getArenaRegion() {
        return arenaRegion;
    }

    /**
     * Sets the main arena region.
     *
     * @param arenaRegion the region
     */
    public void setArenaRegion(ArenaRegion arenaRegion) {
        this.arenaRegion = arenaRegion;
    }

    /**
     * Gets the lobby region.
     *
     * @return the lobby region
     */
    public ArenaRegion getLobbyRegion() {
        return lobbyRegion;
    }

    /**
     * Sets the lobby region.
     *
     * @param lobbyRegion the region
     */
    public void setLobbyRegion(ArenaRegion lobbyRegion) {
        this.lobbyRegion = lobbyRegion;
    }

    /**
     * Gets the hunter cage region.
     *
     * @return the hunter cage region
     */
    public ArenaRegion getHunterCageRegion() {
        return hunterCageRegion;
    }

    /**
     * Sets the hunter cage region.
     *
     * @param hunterCageRegion the region
     */
    public void setHunterCageRegion(ArenaRegion hunterCageRegion) {
        this.hunterCageRegion = hunterCageRegion;
    }

    // ===== Spawn Methods =====

    /**
     * Gets the lobby spawn location.
     *
     * @return the lobby spawn
     */
    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    /**
     * Sets the lobby spawn location.
     *
     * @param lobbySpawn the spawn location
     */
    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    /**
     * Gets all prop spawn locations.
     *
     * @return unmodifiable list of prop spawns
     */
    public List<Location> getPropSpawns() {
        return Collections.unmodifiableList(propSpawns);
    }

    /**
     * Adds a prop spawn location.
     *
     * @param location the spawn location
     */
    public void addPropSpawn(Location location) {
        propSpawns.add(location.clone());
    }

    /**
     * Removes a prop spawn by index.
     *
     * @param index the index
     * @return true if removed
     */
    public boolean removePropSpawn(int index) {
        if (index >= 0 && index < propSpawns.size()) {
            propSpawns.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Clears all prop spawns.
     */
    public void clearPropSpawns() {
        propSpawns.clear();
    }

    /**
     * Gets a random prop spawn location.
     *
     * @return random prop spawn, or null if none
     */
    public Location getRandomPropSpawn() {
        if (propSpawns.isEmpty()) return null;
        return propSpawns.get(new Random().nextInt(propSpawns.size())).clone();
    }

    /**
     * Gets all hunter spawn locations.
     *
     * @return unmodifiable list of hunter spawns
     */
    public List<Location> getHunterSpawns() {
        return Collections.unmodifiableList(hunterSpawns);
    }

    /**
     * Adds a hunter spawn location.
     *
     * @param location the spawn location
     */
    public void addHunterSpawn(Location location) {
        hunterSpawns.add(location.clone());
    }

    /**
     * Removes a hunter spawn by index.
     *
     * @param index the index
     * @return true if removed
     */
    public boolean removeHunterSpawn(int index) {
        if (index >= 0 && index < hunterSpawns.size()) {
            hunterSpawns.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Clears all hunter spawns.
     */
    public void clearHunterSpawns() {
        hunterSpawns.clear();
    }

    /**
     * Gets a random hunter spawn location.
     *
     * @return random hunter spawn, or null if none
     */
    public Location getRandomHunterSpawn() {
        if (hunterSpawns.isEmpty()) return null;
        return hunterSpawns.get(new Random().nextInt(hunterSpawns.size())).clone();
    }

    // ===== Prop Registry =====

    /**
     * Gets the prop registry for this arena.
     *
     * @return the prop registry
     */
    public PropRegistry getPropRegistry() {
        return propRegistry;
    }

    // ===== Settings =====

    /**
     * Gets the arena-specific settings.
     *
     * @return the settings
     */
    public GameSettings getSettings() {
        return settings;
    }

    /**
     * Sets the arena-specific settings.
     *
     * @param settings the settings
     */
    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    // ===== Location Checks =====

    /**
     * Checks if a location is within the arena play area.
     *
     * @param location the location
     * @return true if inside arena
     */
    public boolean isInArena(Location location) {
        return arenaRegion != null && arenaRegion.contains(location);
    }

    /**
     * Checks if a location is within the lobby.
     *
     * @param location the location
     * @return true if inside lobby
     */
    public boolean isInLobby(Location location) {
        return lobbyRegion != null && lobbyRegion.contains(location);
    }

    /**
     * Checks if a location is within the hunter cage.
     *
     * @param location the location
     * @return true if inside hunter cage
     */
    public boolean isInHunterCage(Location location) {
        return hunterCageRegion != null && hunterCageRegion.contains(location);
    }

    // ===== Validation =====

    /**
     * Validates that the arena is properly configured.
     *
     * @return list of missing requirements
     */
    public List<String> validate() {
        List<String> missing = new ArrayList<>();

        if (arenaRegion == null) {
            missing.add("Arena region not defined");
        }
        if (lobbySpawn == null) {
            missing.add("Lobby spawn not set");
        }
        if (hunterCageRegion == null) {
            missing.add("Hunter cage region not defined");
        }
        if (propSpawns.isEmpty()) {
            missing.add("No prop spawn points");
        }
        if (hunterSpawns.isEmpty()) {
            missing.add("No hunter spawn points");
        }
        if (propRegistry.getProps().size() < 5) {
            missing.add("Less than 5 valid props (run /ph scan)");
        }

        return missing;
    }

    /**
     * Checks if the arena is valid and ready to be enabled.
     *
     * @return true if valid
     */
    public boolean isValid() {
        return validate().isEmpty();
    }

    // ===== Serialization =====

    /**
     * Saves the arena to a file.
     *
     * @param file the file to save to
     * @throws IOException if save fails
     */
    public void save(File file) throws IOException {
        YamlConfiguration config = new YamlConfiguration();

        config.set("name", name);
        config.set("enabled", enabled);

        // Save regions
        if (arenaRegion != null) {
            arenaRegion.save(config.createSection("region.arena"));
        }
        if (lobbyRegion != null) {
            lobbyRegion.save(config.createSection("region.lobby"));
        }
        if (hunterCageRegion != null) {
            hunterCageRegion.save(config.createSection("region.hunter-cage"));
        }

        // Save spawns
        if (lobbySpawn != null) {
            saveLocation(config.createSection("spawns.lobby"), lobbySpawn);
        }

        ConfigurationSection propSpawnSection = config.createSection("spawns.props");
        for (int i = 0; i < propSpawns.size(); i++) {
            saveLocation(propSpawnSection.createSection(String.valueOf(i)), propSpawns.get(i));
        }

        ConfigurationSection hunterSpawnSection = config.createSection("spawns.hunters");
        for (int i = 0; i < hunterSpawns.size(); i++) {
            saveLocation(hunterSpawnSection.createSection(String.valueOf(i)), hunterSpawns.get(i));
        }

        // Save props
        propRegistry.save(config.createSection("props"));

        // Save settings
        saveSettings(config.createSection("settings"));

        config.save(file);
    }

    /**
     * Loads an arena from a file.
     *
     * @param file the file to load from
     * @return the loaded arena, or null if failed
     */
    public static Arena load(File file) {
        if (!file.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String name = config.getString("name");
        if (name == null) {
            name = file.getName().replace(".yml", "");
        }

        Arena arena = new Arena(name);
        arena.enabled = config.getBoolean("enabled", false);

        // Load regions
        arena.arenaRegion = ArenaRegion.load(config.getConfigurationSection("region.arena"));
        arena.lobbyRegion = ArenaRegion.load(config.getConfigurationSection("region.lobby"));
        arena.hunterCageRegion = ArenaRegion.load(config.getConfigurationSection("region.hunter-cage"));

        // Load spawns
        arena.lobbySpawn = loadLocation(config.getConfigurationSection("spawns.lobby"));

        ConfigurationSection propSpawnSection = config.getConfigurationSection("spawns.props");
        if (propSpawnSection != null) {
            for (String key : propSpawnSection.getKeys(false)) {
                Location loc = loadLocation(propSpawnSection.getConfigurationSection(key));
                if (loc != null) {
                    arena.propSpawns.add(loc);
                }
            }
        }

        ConfigurationSection hunterSpawnSection = config.getConfigurationSection("spawns.hunters");
        if (hunterSpawnSection != null) {
            for (String key : hunterSpawnSection.getKeys(false)) {
                Location loc = loadLocation(hunterSpawnSection.getConfigurationSection(key));
                if (loc != null) {
                    arena.hunterSpawns.add(loc);
                }
            }
        }

        // Load props
        arena.propRegistry.load(config.getConfigurationSection("props"));

        // Load settings
        arena.loadSettings(config.getConfigurationSection("settings"));

        return arena;
    }

    private void saveLocation(ConfigurationSection section, Location location) {
        if (section == null || location == null || location.getWorld() == null) return;
        section.set("world", location.getWorld().getName());
        section.set("x", location.getX());
        section.set("y", location.getY());
        section.set("z", location.getZ());
        section.set("yaw", location.getYaw());
        section.set("pitch", location.getPitch());
    }

    private static Location loadLocation(ConfigurationSection section) {
        if (section == null) return null;
        String worldName = section.getString("world");
        if (worldName == null) return null;

        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) return null;

        return new Location(world,
                section.getDouble("x"),
                section.getDouble("y"),
                section.getDouble("z"),
                (float) section.getDouble("yaw"),
                (float) section.getDouble("pitch"));
    }

    private void saveSettings(ConfigurationSection section) {
        section.set("min-players", settings.getMinPlayers());
        section.set("max-players", settings.getMaxPlayers());
        section.set("hide-time", settings.getHideTime());
        section.set("seek-time", settings.getSeekTime());
        section.set("prop-percentage", settings.getPropPercentage());
    }

    private void loadSettings(ConfigurationSection section) {
        if (section == null) return;
        settings.setMinPlayers(section.getInt("min-players", settings.getMinPlayers()));
        settings.setMaxPlayers(section.getInt("max-players", settings.getMaxPlayers()));
        settings.setHideTime(section.getInt("hide-time", settings.getHideTime()));
        settings.setSeekTime(section.getInt("seek-time", settings.getSeekTime()));
        settings.setPropPercentage(section.getDouble("prop-percentage", settings.getPropPercentage()));
    }

    @Override
    public String toString() {
        return "Arena{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", propSpawns=" + propSpawns.size() +
                ", hunterSpawns=" + hunterSpawns.size() +
                ", props=" + propRegistry.getProps().size() +
                '}';
    }
}
