package com.example.prophunt.arena;

import com.example.prophunt.PropHuntPlugin;
import com.example.prophunt.disguise.PropSize;
import com.example.prophunt.disguise.PropType;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.*;

/**
 * Scans arena regions to detect valid prop blocks.
 */
public class ArenaScanner {

    private final PropHuntPlugin plugin;

    // Materials that should never be props
    private static final Set<Material> BLACKLIST = new HashSet<>(Arrays.asList(
            // Air and technical blocks
            Material.AIR, Material.CAVE_AIR, Material.VOID_AIR,
            Material.BARRIER, Material.STRUCTURE_VOID, Material.STRUCTURE_BLOCK,
            Material.COMMAND_BLOCK, Material.CHAIN_COMMAND_BLOCK, Material.REPEATING_COMMAND_BLOCK,
            Material.JIGSAW, Material.LIGHT,

            // Liquids
            Material.WATER, Material.LAVA,

            // Dangerous/problematic blocks
            Material.BEDROCK, Material.END_PORTAL, Material.END_PORTAL_FRAME,
            Material.NETHER_PORTAL, Material.END_GATEWAY,
            Material.SPAWNER, Material.INFESTED_STONE, Material.INFESTED_COBBLESTONE,
            Material.INFESTED_STONE_BRICKS, Material.INFESTED_MOSSY_STONE_BRICKS,
            Material.INFESTED_CRACKED_STONE_BRICKS, Material.INFESTED_CHISELED_STONE_BRICKS,
            Material.INFESTED_DEEPSLATE,

            // Moving/piston blocks
            Material.MOVING_PISTON, Material.PISTON_HEAD,

            // Redstone that's too technical
            Material.REDSTONE_WIRE, Material.COMPARATOR, Material.REPEATER,

            // Fire
            Material.FIRE, Material.SOUL_FIRE,

            // Too big or unusual
            Material.DRAGON_EGG, Material.CHORUS_PLANT, Material.CHORUS_FLOWER
    ));

    // Materials that make good props (even if not commonly found)
    private static final Set<Material> WHITELIST = new HashSet<>(Arrays.asList(
            // Classic hide-and-seek props
            Material.BARREL, Material.CHEST, Material.TRAPPED_CHEST,
            Material.FURNACE, Material.CRAFTING_TABLE, Material.CAULDRON,
            Material.FLOWER_POT, Material.LANTERN, Material.SOUL_LANTERN,
            Material.DECORATED_POT, Material.CAMPFIRE, Material.SOUL_CAMPFIRE,
            Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL,
            Material.BREWING_STAND, Material.ENCHANTING_TABLE,
            Material.LECTERN, Material.COMPOSTER, Material.SMOKER, Material.BLAST_FURNACE,
            Material.HOPPER, Material.DISPENSER, Material.DROPPER,
            Material.BEE_NEST, Material.BEEHIVE,
            Material.BELL, Material.GRINDSTONE, Material.STONECUTTER,
            Material.CARTOGRAPHY_TABLE, Material.FLETCHING_TABLE, Material.LOOM,
            Material.SMITHING_TABLE
    ));

    public ArenaScanner(PropHuntPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Scans an arena and populates its prop registry.
     *
     * @param arena the arena to scan
     * @return scan results
     */
    public ScanResult scan(Arena arena) {
        if (arena.getArenaRegion() == null) {
            return new ScanResult(false, "Arena region not defined");
        }

        ArenaRegion region = arena.getArenaRegion();
        PropRegistry registry = arena.getPropRegistry();

        // Check region size
        int volume = region.getVolume();
        if (volume > 1000000) {
            return new ScanResult(false, "Arena region too large (max 1,000,000 blocks)");
        }

        plugin.debug("Scanning arena '%s' with %d blocks", arena.getName(), volume);

        // Track found materials
        Map<Material, Integer> foundMaterials = new LinkedHashMap<>();
        long startTime = System.currentTimeMillis();

        // Scan all blocks
        region.forEachBlock(block -> {
            Material material = block.getType();
            if (isValidPropMaterial(material)) {
                foundMaterials.merge(material, 1, Integer::sum);
            }
        });

        // Clear and populate registry
        registry.clear();

        // Sort by frequency (most common first)
        List<Map.Entry<Material, Integer>> sorted = new ArrayList<>(foundMaterials.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<Material, Integer> entry : sorted) {
            PropSize size = PropType.determineSizeForMaterial(entry.getKey());
            registry.addProp(entry.getKey(), size);
        }

        long duration = System.currentTimeMillis() - startTime;
        plugin.debug("Scan completed in %dms, found %d unique prop types",
                duration, registry.size());

        return new ScanResult(true, registry.size(), registry.getCountsBySize(), duration);
    }

    /**
     * Checks if a material is valid for use as a prop.
     *
     * @param material the material
     * @return true if valid
     */
    public boolean isValidPropMaterial(Material material) {
        if (material == null) return false;
        if (!material.isBlock()) return false;
        if (BLACKLIST.contains(material)) return false;
        if (material.isAir()) return false;
        if (!material.isSolid() && !isDecorativeBlock(material)) return false;

        return true;
    }

    /**
     * Checks if a material is a decorative block that makes a good prop.
     *
     * @param material the material
     * @return true if decorative
     */
    private boolean isDecorativeBlock(Material material) {
        String name = material.name();

        return name.contains("FLOWER") ||
               name.contains("POTTED") ||
               name.contains("TORCH") ||
               name.contains("CANDLE") ||
               name.contains("LANTERN") ||
               name.contains("CARPET") ||
               name.contains("BUTTON") ||
               name.contains("CORAL") ||
               name.contains("MUSHROOM") ||
               name.contains("FERN") ||
               name.contains("GRASS") ||
               name.contains("SAPLING") ||
               name.contains("SIGN") ||
               WHITELIST.contains(material);
    }

    /**
     * Gets a description of what makes a good prop.
     *
     * @return description string
     */
    public static String getPropGuidelines() {
        return """
            Good props are:
            - Solid decorative blocks (chests, barrels, furnaces)
            - Small items (flower pots, lanterns, candles)
            - Items that blend into typical builds

            Props should NOT be:
            - Technical blocks (command blocks, barriers)
            - Full-size structural blocks (stone, dirt)
            - Dangerous blocks (fire, lava, portals)
            """;
    }

    /**
     * Result of an arena scan.
     */
    public static class ScanResult {
        private final boolean success;
        private final String error;
        private final int totalProps;
        private final Map<PropSize, Integer> propsBySize;
        private final long duration;

        public ScanResult(boolean success, String error) {
            this.success = success;
            this.error = error;
            this.totalProps = 0;
            this.propsBySize = Collections.emptyMap();
            this.duration = 0;
        }

        public ScanResult(boolean success, int totalProps,
                         Map<PropSize, Integer> propsBySize, long duration) {
            this.success = success;
            this.error = null;
            this.totalProps = totalProps;
            this.propsBySize = propsBySize;
            this.duration = duration;
        }

        public boolean isSuccess() { return success; }
        public String getError() { return error; }
        public int getTotalProps() { return totalProps; }
        public Map<PropSize, Integer> getPropsBySize() { return propsBySize; }
        public long getDuration() { return duration; }

        public String getSummary() {
            if (!success) {
                return "Scan failed: " + error;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Found ").append(totalProps).append(" prop types:\n");
            for (PropSize size : PropSize.values()) {
                int count = propsBySize.getOrDefault(size, 0);
                sb.append("  ").append(size.getDisplayName()).append(": ").append(count).append("\n");
            }
            sb.append("Scan completed in ").append(duration).append("ms");
            return sb.toString();
        }
    }
}
